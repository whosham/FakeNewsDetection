package main

import (
	"crypto/sha1"
	"fmt"
	"strconv"

	"github.com/pkg/errors"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/core/chaincode/shim/ext/cid"
	"github.com/hyperledger/fabric/protos/peer"
	"github.com/sirupsen/logrus"
)

const (
	maxAllowedDistanceInMeters   = 100 * 1000
	consideredSecondsBeforeEvent = 60 * 60 * 24 // 24 hours
	diffBetweenLocations         = 60 * 10      // 10 minutes
	locationsPerAssessment       = consideredSecondsBeforeEvent / diffBetweenLocations
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

	if fn == "addEvent" {
		// args: Event-Object as
		// `{ "title": "foo", "location": { "latitude": 52.264149, "longitude": 10.526420 }, "description": "bar" }`
		result, err = addEvent(stub, args, clientID)
	} else if fn == "judgeEvent" {
		// args: Assessment-Object + Locations as
		// `[{"event":"event-id","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]},"List of locations from getLocations"]`
		result, err = judgeEvent(stub, args, clientID)
	} else if fn == "trackLocation" {
		// args: Coordinates-Object as `{"latitude":52,"longitude":10}`
		result, err = trackLocation(stub, args, clientID)
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
		// no-args
		result, err = queryEvents(stub)
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
	return fmt.Sprintf("%x", hash)[:5]
}
