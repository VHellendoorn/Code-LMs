package main

import (
	"bufio"
	"flag"
	"fmt"
	"log"
	"net"
	"net/url"
	"os"

	"github.com/tomsteele/go-shodan"
)

func shodanIPsFromShodanNetSearch(client *shodan.Client, netblock string) ([]string, error) {
	ips := []string{}
	result, err := client.HostSearch("net:"+netblock, []string{}, url.Values{})
	if err != nil {
		return ips, err
	}
	for _, m := range result.Matches {
		ips = append(ips, m.IPStr)
	}
	return ips, nil
}

func main() {
	var filename string
	switch len(flag.Args()) {
	case 1:
		filename = flag.Arg(0)
	default:
		log.Fatal("Fatal: Missing required argument")
	}
	shodanKey := os.Getenv("SHODAN_KEY")
	if shodanKey == "" {
		log.Fatal("Fatal: Missing SHODAN_KEY environment variable")
	}
	sclient := shodan.New(shodanKey)
	ips := []string{}
	file, err := os.Open(filename)
	if err != nil {
		log.Fatalf("Fatal: Could not open file. Error %s", err.Error())
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		_, _, err := net.ParseCIDR(line)
		if err != nil {
			continue
		} else {
			fmt.Printf("Getting %s\n", line)
			if netIPs, err := shodanIPsFromShodanNetSearch(sclient, line); err != nil {
				log.Fatalf("Fatal: Error returned from shodan. Error %s", err.Error())
			} else {
				ips = append(ips, netIPs...)
			}
		}
	}

	fmt.Printf("Shodan found %d live hosts\n", len(ips))
}
