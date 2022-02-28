package main

import (
	"flag"
	"fmt"
	"io"
	"log"
	"os"
	"os/exec"
	"regexp"
	"sort"
	"strings"

	"golang.org/x/mod/modfile"

	"github.com/walterwanderley/sqlc-grpc/metadata"
)

var (
	module        string
	ignoreQueries string
	appendMode    bool
	showVersion   bool
	help          bool
)

func main() {
	flag.BoolVar(&help, "h", false, "Help for this program")
	flag.BoolVar(&showVersion, "v", false, "Show version")
	flag.BoolVar(&appendMode, "append", false, "Enable append mode. Don't rewrite editable files")
	flag.StringVar(&module, "m", "my-project", "Go module name if there are no go.mod")
	flag.StringVar(&ignoreQueries, "i", "", "Comma separated list (regex) of queries to ignore")
	flag.Parse()

	if help {
		flag.PrintDefaults()
		fmt.Println("\nFor more information, please visit https://github.com/walterwanderley/sqlc-grpc")
		return
	}

	if showVersion {
		fmt.Println(version)
		return
	}

	cfg, err := readConfig()
	if err != nil {
		log.Fatal(err)
	}

	if len(cfg.Packages) == 0 {
		log.Fatal("no packages")
	}

	queriesToIgnore := make([]*regexp.Regexp, 0)
	for _, queryName := range strings.Split(ignoreQueries, ",") {
		s := strings.TrimSpace(queryName)
		if s == "" {
			continue
		}
		queriesToIgnore = append(queriesToIgnore, regexp.MustCompile(s))
	}

	if m := moduleFromGoMod(); m != "" {
		fmt.Println("Using module path from go.mod:", m)
		module = m
	}

	args := strings.Join(os.Args, " ")
	if !strings.Contains(args, " -append") {
		args += " -append"
	}

	def := metadata.Definition{
		Args:     args,
		GoModule: module,
		Packages: make([]*metadata.Package, 0),
	}

	for _, p := range cfg.Packages {
		pkg, err := metadata.ParsePackage(metadata.PackageOpts{
			Path:               p.Path,
			EmitInterface:      p.EmitInterface,
			EmitParamsPointers: p.EmitParamsStructPointers,
			EmitResultPointers: p.EmitResultStructPointers,
			EmitDbArgument:     p.EmitMethodsWithDBArgument,
		}, queriesToIgnore)
		if err != nil {
			log.Fatal("parser error:", err.Error())
		}
		pkg.GoModule = module
		pkg.Engine = p.Engine

		if len(pkg.Services) == 0 {
			log.Println("No services on package", pkg.Package)
			continue
		}

		def.Packages = append(def.Packages, pkg)
	}
	sort.SliceStable(def.Packages, func(i, j int) bool {
		return strings.Compare(def.Packages[i].Package, def.Packages[j].Package) < 0
	})

	if len(def.Packages) == 0 {
		log.Fatal("No services found, verify the -i parameter")
	}

	wd, err := os.Getwd()
	if err != nil {
		log.Fatal("unable to get working directory:", err.Error())
	}

	err = process(&def, wd, appendMode)
	if err != nil {
		log.Fatal("unable to process templates:", err.Error())
	}

	postProcess(&def, wd)
}

func moduleFromGoMod() string {
	f, err := os.Open("go.mod")
	if err != nil {
		return ""
	}
	defer f.Close()

	b, err := io.ReadAll(f)
	if err != nil {
		return ""
	}

	return modfile.ModulePath(b)
}

func postProcess(def *metadata.Definition, workingDirectory string) {
	fmt.Printf("Configuring project %s...\n", def.GoModule)
	execCommand("go mod init " + def.GoModule)
	execCommand("go mod tidy")
	execCommand("go install github.com/grpc-ecosystem/grpc-gateway/v2/protoc-gen-grpc-gateway " +
		"github.com/grpc-ecosystem/grpc-gateway/v2/protoc-gen-openapiv2 " +
		"google.golang.org/protobuf/cmd/protoc-gen-go " +
		"google.golang.org/grpc/cmd/protoc-gen-go-grpc " +
		"github.com/bufbuild/buf/cmd/buf")
	fmt.Println("Compiling protocol buffers...")
	if err := os.Chdir("proto"); err != nil {
		panic(err)
	}
	execCommand("buf mod update")
	if err := os.Chdir(workingDirectory); err != nil {
		panic(err)
	}
	execCommand("buf generate")
	execCommand("go mod tidy")
	fmt.Println("Finished!")
}

func execCommand(command string) error {
	line := strings.Split(command, " ")
	cmd := exec.Command(line[0], line[1:]...)
	cmd.Stderr = os.Stderr
	cmd.Stdout = os.Stdout
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("[error] %q: %w", command, err)
	}
	return nil
}
