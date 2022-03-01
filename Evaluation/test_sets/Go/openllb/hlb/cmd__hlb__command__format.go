package command

import (
	"context"
	"fmt"
	"io"
	"io/ioutil"
	"os"

	"github.com/alecthomas/participle/lexer"
	"github.com/openllb/hlb/parser"
	cli "github.com/urfave/cli/v2"
)

var formatCommand = &cli.Command{
	Name:      "format",
	Aliases:   []string{"fmt"},
	Usage:     "formats hlb programs",
	ArgsUsage: "[ <*.hlb> ... ]",
	Flags: []cli.Flag{
		&cli.BoolFlag{
			Name:    "write",
			Aliases: []string{"w"},
			Usage:   "write result to (source) file instead of stdout",
		},
	},
	Action: func(c *cli.Context) error {
		rs, cleanup, err := collectReaders(c)
		if err != nil {
			return err
		}
		defer func() {
			err := cleanup()
			if err != nil {
				fmt.Fprint(os.Stderr, err.Error())
			}
		}()

		return Format(Context(), rs, FormatInfo{
			Write: c.Bool("write"),
		})
	},
}

type FormatInfo struct {
	Write bool
}

func Format(ctx context.Context, rs []io.Reader, info FormatInfo) error {
	modules, err := parser.ParseMultiple(ctx, rs)
	if err != nil {
		return err
	}

	if info.Write {
		for i, mod := range modules {
			filename := lexer.NameOfReader(rs[i])
			if filename == "" {
				return fmt.Errorf("Unable to write, file name unavailable")
			}
			info, err := os.Stat(filename)
			if err != nil {
				return err
			}

			err = ioutil.WriteFile(filename, []byte(mod.String()), info.Mode())
			if err != nil {
				return err
			}
		}
	} else {
		for _, mod := range modules {
			fmt.Printf("%s", mod)
		}
	}

	return nil
}
