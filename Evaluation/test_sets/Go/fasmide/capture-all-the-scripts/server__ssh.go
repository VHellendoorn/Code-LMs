package server

import (
	"fmt"
	"io/ioutil"
	"log"
	"net"
	"sync"
	"time"

	humanize "github.com/dustin/go-humanize"
	"golang.org/x/crypto/ssh"
)

type SSH struct {
	Path   string
	Events chan string

	banner string

	lock             sync.RWMutex
	connections      map[net.Conn]*Connection
	totalconnections int
	bytessent        int
}

type State struct {
	Connections      []*Connection
	TotalConnections int
	BytesSent        int
}

func (s *SSH) State() State {
	s.lock.RLock()
	defer s.lock.RUnlock()

	state := State{BytesSent: s.bytessent, TotalConnections: s.totalconnections}
	for _, conn := range s.connections {
		state.Connections = append(state.Connections, conn)
	}
	return state
}

func (s *SSH) Listen() {

	s.lock.Lock()
	s.connections = make(map[net.Conn]*Connection)
	s.lock.Unlock()

	err := s.preparebook("ebook.txt")
	if err != nil {
		log.Fatal("Failed to prepare book: ", err)
	}

	privateBytes, err := ioutil.ReadFile("id_rsa")
	if err != nil {
		log.Fatal("Failed to load private key: ", err)
	}

	hostkey, err := ssh.ParsePrivateKey(privateBytes)
	if err != nil {
		log.Fatal("Failed to parse private key: ", err)
	}

	config := &ssh.ServerConfig{
		PasswordCallback: func(c ssh.ConnMetadata, pass []byte) (*ssh.Permissions, error) {
			return nil, fmt.Errorf("password rejected for %q", c.User())
		},

		PublicKeyCallback: func(c ssh.ConnMetadata, pubKey ssh.PublicKey) (*ssh.Permissions, error) {
			return nil, fmt.Errorf("unknown public key for %q", c.User())
		},
		Banner:        s.banner,
		ServerVersion: "SSH-2.0-OpenSSH_7.6p1 Ubuntu-4",
	}

	config.AddHostKey(hostkey)

	listener, err := net.Listen("tcp", s.Path)
	if err != nil {
		log.Fatal("failed to listen for connection: ", err)
	}

	for {
		nConn, err := listener.Accept()
		if err != nil {
			log.Fatal("failed to accept incoming connection: ", err)
			continue
		}
		go s.acceptSSH(nConn, config)
	}

}

func (s *SSH) acceptSSH(nConn net.Conn, config *ssh.ServerConfig) {

	stateConn := Connection{Conn: nConn, Remote: nConn.RemoteAddr().String(), Started: time.Now()}
	s.lock.Lock()
	s.totalconnections++
	s.connections[nConn] = &stateConn
	s.lock.Unlock()
	bannerStarted := make(chan bool)

	var wg sync.WaitGroup
	var err error
	wg.Add(1)
	go func() {
		_, _, _, err = ssh.NewServerConn(&stateConn, config, bannerStarted)
		wg.Done()
	}()

	wg.Add(1)
	go func() {
		// Timeout if the banner haven't started after
		// 5 minuttes
		select {
		case <-bannerStarted:
			close(bannerStarted)
		case <-time.After(time.Minute * 5):
			stateConn.Close()
		}
		wg.Done()
	}()

	wg.Wait()

	stateConn.Close()

	s.lock.Lock()
	delete(s.connections, nConn)
	s.bytessent += stateConn.Written()
	s.lock.Unlock()
	host, _, _ := net.SplitHostPort(stateConn.Remote)
	s.Events <- fmt.Sprintf("%15s, %11s: %7s: %s", host, time.Now().Sub(stateConn.Started).Truncate(time.Second), humanize.Bytes(uint64(stateConn.Written())), err)
}

func (s *SSH) preparebook(path string) error {

	data, err := ioutil.ReadFile("ebook.txt")
	if err != nil {
		return err
	}
	s.banner = string(data)
	return nil
}
