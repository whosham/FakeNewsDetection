package main

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"./eventcc"
	"github.com/sirupsen/logrus"
)

func main() {
	logrus.SetLevel(logrus.InfoLevel)
	if err := shim.Start(new(eventcc.Eventcc)); err != nil {
		logrus.Errorf("Error starting eventcc chaincode: %s", err)
	}
}
