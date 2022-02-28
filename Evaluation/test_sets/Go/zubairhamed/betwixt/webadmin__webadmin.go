package main

import (
	"github.com/kylelemons/go-gypsy/yaml"
	"github.com/zubairhamed/betwixt"
	"github.com/zubairhamed/betwixt/webadmin/app"
)

func main() {
	store := betwixt.NewInMemoryStore()
	webApp := app.NewWebApp(store, parseConfig())

	registry := betwixt.NewDefaultObjectRegistry()

	webApp.UseRegistry(registry)

	webApp.Serve()
}

func parseConfig() betwixt.ServerConfig {
	cfg := map[string]string{}
	if file, err := yaml.ReadFile("./config.yaml"); err == nil {
		m := file.Root.(yaml.Map)

		cfg["name"] = m.Key("name").(yaml.Scalar).String()
		cfg["http-port"] = m.Key("http").(yaml.Map).Key("port").(yaml.Scalar).String()

		return cfg
	}
	return cfg
}
