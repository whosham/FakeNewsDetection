package main

import (
	"crypto/md5"
	"crypto/sha1"
	"encoding/hex"
	"fmt"
	"strconv"

	"github.com/pkg/errors"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/core/chaincode/shim/ext/cid"
	"github.com/hyperledger/fabric/protos/peer"
	"github.com/sirupsen/logrus"
)

// Eventcc is the event chaincode
type Eventcc struct {
}

// Init initializes the chaincode
func (t *Eventcc) Init(stub shim.ChaincodeStubInterface) peer.Response {
	logrus.Info("Init Chaincode")
	return shim.Success(nil)
}

// Invoke is called when a new transaction proposal arrives
func (t *Eventcc) Invoke(stub shim.ChaincodeStubInterface) peer.Response {
	// Extract the function and args from the transaction proposal
	fn, args := stub.GetFunctionAndParameters()
	var result string
	var err error
	clientID, err := cid.GetID(stub)
	if err != nil {
		return shim.Error(err.Error())
	}
	logrus.Infof("ClientID: ", clientID)
	hasher := md5.New()
	_, err = hasher.Write([]byte(clientID))
	if err != nil {
		return shim.Error(err.Error())
	}
	clientID = hex.EncodeToString(hasher.Sum(nil))
	logrus.Infof("Hashed ClientID: ", clientID)
	logrus.Infof("EventCC called with args: %s", args)
	if fn == "addEvent" {
		// args: Event-Object as
		// `{ "title": "foo", "location": { "latitude": 52.264149, "longitude": 10.526420 }, "description": "bar" }`
		result, err = addEvent(stub, args, clientID)
	} else if fn == "judgeEvent" {
		if locationEncryption {
			// args: Assessment-Object + aesKeys as
			// `[{"event":"event-id","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]},["key1","key2"]]`
			result, err = judgeEventEncryptedLocation(stub, args, clientID)
		} else {
			// args: Assessment-Object
			// `{"event":"event-id","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]}`
			result, err = judgeEvent(stub, args, clientID)
		}
	} else if fn == "trackLocation" {
		if locationEncryption {
			result, err = trackEncryptedLocation(stub, args, clientID)
		} else {
			// args: Coordinates-Object as `{"latitude":52,"longitude":10}`
			result, err = trackLocation(stub, args, clientID)
		}
	} else if fn == "getLocation" {
		// args: "user-id"
		result, err = getLocation(stub, args)
		err = errors.New("not implemented yet")
	} else if fn == "getLocations" {
		// args: "endTime"
		result, err = getLocations(stub, args, clientID)
	} else if fn == "getFullEvent" {
		// args: "event-id"
		result, err = getFullEvent(stub, args)
	} else if fn == "getEvent" {
		// args: "event-id"
		result, err = getEvent(stub, args)
	} else if fn == "getEvents" {
		// no-args
		result, err = getEvents(stub)
	} else if fn == "queryEvents" {
		// args: query-string
		result, err = queryEvents(stub, args)
	} else if fn == "getUser" {
		// args: user-id
		result, err = getUser(stub, args)
	} else {
		errMsg := "Incorrect arguments. Dont know function: " + fn
		logrus.Errorf(errMsg)
		shim.Error(errMsg)
	}
	if err != nil {
		return shim.Error(err.Error())
	}

	// Return the result as success payload
	return shim.Success([]byte(result))
}

func getTimeStamp(seconds int64) string {
	return strconv.FormatInt(seconds, 10)
}

func createIDForStrings(strings []string) string {
	h := sha1.New()
	for _, s := range strings {
		_, err := h.Write([]byte(s))
		if err != nil {
			panic(err)
		}
	}
	hash := h.Sum(nil)
	return fmt.Sprintf("%x", hash)[:10]
}
