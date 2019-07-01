package main

import (
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/pkg/errors"
	"github.com/sirupsen/logrus"
)

// Event represents the event objects we save and assess in the ledger
type Event struct {
	ObjectType      string      `json:"docType"`
	ID              string      `json:"id"`
	Title           string      `json:"title"`
	Image           string      `json:"image"`
	Location        Coordinates `json:"location"`
	Timestamp       string      `json:"timestamp"`
	Description     string      `json:"description"`
	Creator         string      `json:"creator"`
	Trustworthiness float64     `json:"trustworthiness"`
}

// NewEvent creates the default event
func NewEvent(id string, latitude float64, longitude float64, creator string, timestamp string) Event {
	return Event{ObjectType: "event", ID: id, Location: Coordinates{latitude, longitude},
		Creator: creator, Timestamp: timestamp}
}

func addEvent(stub shim.ChaincodeStubInterface, args []string, clientID string) (string, error) {
	if len(args) != 1 {
		return "", fmt.Errorf("incorrect arguments: expecting a event as: " +
			`{ "title": "foo", "location": { "latitude": 52.264149, "longitude": 10.526420 }, "description": "bar" }`)
	}

	time, err := stub.GetTxTimestamp()
	argsForID := append(args, time.String())
	id := "event-" + createIDForStrings(argsForID)
	if err != nil {
		return "", err
	}

	event := Event{}
	// set timestamp before unmarshalling to give a default value
	event.Timestamp = getTimeStamp(time.GetSeconds())
	err = json.Unmarshal([]byte(args[0]), &event)
	event.ObjectType = "event"
	event.ID = id
	event.Creator = clientID
	event.Trustworthiness = 0

	eventAsBytes, _ := json.Marshal(event)
	err = stub.PutState(id, eventAsBytes)
	if err != nil {
		return "", fmt.Errorf("Failed to set event: %s", args)
	}
	logrus.Info("Add event: ", string(eventAsBytes))

	if adjustUserRating {
		_, err = changeUserScoreAndCreateUserIfNecessary(stub, clientID, 1)
		if err != nil {
			return "", err
		}
	}
	return string(eventAsBytes), nil
}

// Get returns the value of the specified asset key
func getEvent(stub shim.ChaincodeStubInterface, args []string) (string, error) {
	if len(args) != 1 {
		return "", fmt.Errorf("Incorrect arguments. Expecting a key")
	}

	value, err := stub.GetState(args[0])
	if err != nil {
		return "", fmt.Errorf("Failed to getEvent: %s with error: %s", args[0], err)
	}
	if value == nil {
		return "", fmt.Errorf("event not found: %s", args[0])
	}
	logrus.Info("Get event: ", string(value))
	return string(value), nil
}

func getEvents(stub shim.ChaincodeStubInterface) (string, error) {
	queryIterator, err := stub.GetStateByRange("event-00000", "event-zzzzz")
	if err != nil {
		return "", err
	}
	events := []Event{}
	for queryIterator.HasNext() {
		kv, err := queryIterator.Next()
		if err != nil {
			return "", err
		}
		logrus.Debugf("Add event %s to requested event list", kv.Key)
		var event *Event
		err = json.Unmarshal(kv.Value, &event)
		if err != nil {
			return "", err
		}
		events = append(events, *event)
	}

	eventAsBytes, err := json.Marshal(events)
	if err != nil {
		return "", err
	}
	return string(eventAsBytes), nil
}

func getFullEvent(stub shim.ChaincodeStubInterface, args []string) (string, error) {
	fullevent := FullEvent{Assessments: []Assessment{}}
	if len(args) != 1 {
		return "", fmt.Errorf("Incorrect arguments. Expecting a key")
	}

	eventID := args[0]
	response, err := stub.GetState(eventID)
	if err != nil {
		return "", fmt.Errorf("Failed to getEvent: %s with error: %s", eventID, err)
	}
	if response == nil {
		return "", fmt.Errorf("event not found: %s", eventID)
	}
	event := Event{}
	err = json.Unmarshal([]byte(response), &event)
	if err != nil {
		return "", err
	}
	fullevent.Event = event

	queryString := fmt.Sprintf("{\"selector\":{\"docType\":\"assessment\",\"event\":\"%s\"}}", eventID)
	queryResults, err := stub.GetQueryResult(queryString)
	if err != nil {
		return "", err
	}

	for queryResults.HasNext() {
		qr, err := queryResults.Next()
		if err != nil {
			return "", err
		}
		assessment := Assessment{}
		err = json.Unmarshal(qr.Value, &assessment)
		if err != nil {
			return "", err
		}
		fullevent.Assessments = append(fullevent.Assessments, assessment)
	}

	fulleventAsBytes, err := json.Marshal(fullevent)
	if err != nil {
		return "", err
	}

	return string(fulleventAsBytes), nil
}

func queryEvents(stub shim.ChaincodeStubInterface, args []string) (string, error) {
	logrus.Infof("in queryEvents")
	lat := 52.0
	long := 10.0
	defaultQueryString := fmt.Sprintf(`{"selector":{"docType":"event","location.latitude":{"$gte":%f,"$lte":%f},"location.longitude":{"$gte":%f,"$lte":%f}}}`,
		lat-maxDecimalDiff, lat+maxDecimalDiff, long-maxDecimalDiff, long+maxDecimalDiff)
	queryString := defaultQueryString
	if len(args) > 1 {
		return "", errors.New("Incorrect arguments. Expecting a querystring like: " + defaultQueryString)
	} else if len(args) == 1 {
		queryString = args[0]
	}

	logrus.Infof("invoke get query result with queryString: %s", queryString)
	queryResults, err := stub.GetQueryResult(queryString)
	if err != nil {
		return "", err
	}

	events := []Event{}
	for queryResults.HasNext() {
		qr, err := queryResults.Next()
		if err != nil {
			return "", err
		}
		event := Event{}
		err = json.Unmarshal(qr.Value, &event)
		if err != nil {
			return "", err
		}
		events = append(events, event)
	}

	eventsAsBytes, err := json.Marshal(events)
	if err != nil {
		return "", err
	}
	return string(eventsAsBytes), nil
}
