package channel

import (
	"bytes"
	"github.com/golang/protobuf/proto"
	cb "github.com/hyperledger/fabric-protos-go/common"
	"github.com/hyperledger/fabric-sdk-go/pkg/client/resmgmt"
	"github.com/hyperledger/fabric-sdk-go/pkg/core/config"
	"github.com/hyperledger/fabric-sdk-go/pkg/fabsdk"
	"github.com/pkg/errors"
	log "github.com/sirupsen/logrus"
	"github.com/spf13/cobra"
	"io"
	"io/ioutil"
)

type updateChannelCmd struct {
	configPath  string
	channelName string
	userName    string
	file        string
	mspID       string
	signatures  []string
}

func (c *updateChannelCmd) validate() error {
	return nil
}

func (c *updateChannelCmd) run() error {
	configBackend := config.FromFile(c.configPath)
	sdk, err := fabsdk.New(configBackend)
	if err != nil {
		return err
	}
	org1AdminClientContext := sdk.Context(
		fabsdk.WithUser(c.userName),
		fabsdk.WithOrg(c.mspID),
	)
	resClient, err := resmgmt.New(org1AdminClientContext)
	if err != nil {
		return err
	}
	updateEnvelopeBytes, err := ioutil.ReadFile(c.file)
	if err != nil {
		return err
	}
	configUpdateReader := bytes.NewReader(updateEnvelopeBytes)
	var requestOptions []resmgmt.RequestOption
	var configSignatures []*cb.ConfigSignature
	for _, signatureFile := range c.signatures {
		log.Infof("Reading signature %s", signatureFile)
		signatureBytes, err := ioutil.ReadFile(signatureFile)
		if err != nil {
			return errors.Wrapf(err, "failed to read signatureFile %s", signatureFile)
		}
		configSignature := &cb.ConfigSignature{}
		err = proto.Unmarshal(signatureBytes, configSignature)
		if err != nil {
			return errors.Wrapf(err, "failed unmarshalling signature %s", signatureFile)
		}
		configSignatures = append(configSignatures, configSignature)
	}
	requestOptions = append(requestOptions, resmgmt.WithConfigSignatures(configSignatures...))
	chResponse, err := resClient.SaveChannel(
		resmgmt.SaveChannelRequest{
			ChannelID:     c.channelName,
			ChannelConfig: configUpdateReader,
		},
		requestOptions...,
	)
	if err != nil {
		return err
	}
	log.Infof("channel updated added: %s", chResponse.TransactionID)
	return nil
}
func newUpdateChannelCMD(io.Writer, io.Writer) *cobra.Command {
	c := &updateChannelCmd{}
	cmd := &cobra.Command{
		Use: "update",
		RunE: func(cmd *cobra.Command, args []string) error {
			if err := c.validate(); err != nil {
				return err
			}
			return c.run()
		},
	}
	persistentFlags := cmd.PersistentFlags()
	persistentFlags.StringVarP(&c.mspID, "mspid", "", "", "Organization to use to submit the channel update")
	persistentFlags.StringVarP(&c.channelName, "channel", "", "", "Channel name")
	persistentFlags.StringVarP(&c.configPath, "config", "", "", "Configuration file for the SDK")
	persistentFlags.StringVarP(&c.userName, "user", "", "", "User name for the transaction")
	persistentFlags.StringVarP(&c.file, "file", "f", "", "Config update file")
	persistentFlags.StringSliceVarP(&c.signatures, "signatures", "s", []string{}, "Raw signature of the channel update")
	cmd.MarkPersistentFlagRequired("mspid")
	cmd.MarkPersistentFlagRequired("channel")
	cmd.MarkPersistentFlagRequired("config")
	cmd.MarkPersistentFlagRequired("user")
	cmd.MarkPersistentFlagRequired("file")
	return cmd
}
