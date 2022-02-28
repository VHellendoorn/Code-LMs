package plugins

import (
	"fmt"
	"log"
	"net/rpc"
	"net/rpc/jsonrpc"
	"os"

	"github.com/FogCreek/victor"
	"github.com/natefinch/pie"
)

type rpcPlugin struct {
	name string
	path string

	cmdName        string
	cmdDescription string
	cmdUsage       []string

	handlers *[]victor.HandlerDocPair

	client *rpc.Client
}

/*
 NewRPCPlugin creates an rpc plugin and returns a Registrator
 For an RPC plugin to work, it must offer the following methods:

 Name() string
 Description() string
 Usage() []string
 Handle(args) string

 The args in Handle(args) will be of the following struct type:
type Args struct {
	// Chat user calling the plugin.
	User string
	// The arguments a user passes to the bot.
	Fields []string
}
*/

func NewRPCPlugin(name, path string) Registrator {
	plugin := &rpcPlugin{name: name, path: path + "/" + name}
	err := plugin.newClient()
	if err != nil {
		log.Fatal(err)
	}
	defer plugin.client.Close()

	err = plugin.setCmdName()
	if err != nil {
		log.Fatal(err)
	}
	err = plugin.setCmdDescription()
	if err != nil {
		log.Fatal(err)
	}
	err = plugin.setCmdUsage()
	if err != nil {
		log.Fatal(err)
	}
	plugin.createHandlers()

	return plugin
}

func (p *rpcPlugin) newClient() error {
	client, err := pie.StartProviderCodec(jsonrpc.NewClientCodec, os.Stderr, p.path)
	if err != nil {
		log.Fatalf("Error running plugin: %s", err)
	}
	p.client = client
	return nil
}

func (p rpcPlugin) Register() []victor.HandlerDocPair {
	return *p.handlers
}

func (p *rpcPlugin) setCmdName() error {
	return p.client.Call(fmt.Sprintf("%v.Name", p.name), nil, &p.cmdName)
}

func (p *rpcPlugin) setCmdDescription() error {
	return p.client.Call(fmt.Sprintf("%v.Description", p.name), nil, &p.cmdDescription)
}

func (p *rpcPlugin) setCmdUsage() error {
	return p.client.Call(fmt.Sprintf("%v.Usage", p.name), nil, &p.cmdUsage)
}

func (p *rpcPlugin) createHandlers() {
	handlers := &[]victor.HandlerDocPair{
		&victor.HandlerDoc{
			CmdHandler:     p.handleFunc,
			CmdName:        p.cmdName,
			CmdDescription: p.cmdDescription,
			CmdUsage:       p.cmdUsage,
		},
	}
	p.handlers = handlers
}

func (p rpcPlugin) handleFunc(s victor.State) {
	// args is the argument sent to the plugin.
	type Args struct {
		// Chat user calling the plugin.
		User string
		// The arguments a user passes to the bot.
		Fields []string
	}
	args := &Args{User: s.Message().User().Name(), Fields: s.Fields()}

	var msg string
	// start a new client.
	err := p.newClient()
	if err != nil {
		log.Fatal(err)
	}
	defer p.client.Close()
	err = p.client.Call(fmt.Sprintf("%v.Handle", p.name), args, &msg)
	if err != nil {
		log.Println(err)
		msg = fmt.Sprintf("Plugin encountered an error, %v", err)
	}
	s.Reply(string(msg))
}
