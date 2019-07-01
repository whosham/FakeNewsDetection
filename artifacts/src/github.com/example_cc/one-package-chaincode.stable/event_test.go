package main

import (
	"encoding/json"
	"testing"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/stretchr/testify/assert"
)

func TestAddEvent(t *testing.T) {
	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")

	response, err := addEvent(stub,
		[]string{defaultEventArg}, testUser)
	assert.Nil(t, err)
	actualEvent := Event{}
	json.Unmarshal([]byte(response), &actualEvent)

	assert.Equal(t, defaultCoordinates(), actualEvent.Location)
}

func TestAddEventWithWrongData(t *testing.T) {
	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")

	wrongEvent := `{ "title": "foo", "location": { "latitute": 52.264149, "longitude": 10.526420 }, "description": "bar" }`

	response, err := addEvent(stub,
		[]string{wrongEvent}, testUser)
	assert.Nil(t, err)
	actualEvent := Event{}
	json.Unmarshal([]byte(response), &actualEvent)
	c := defaultCoordinates()
	c.Latitude = 0

	assert.Equal(t, c, actualEvent.Location)
}

func TestGetEvent(t *testing.T) {
	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")

	response, err := addEvent(stub, []string{`{ "title": "foo","image": "abc", "location": { "latitude": 52.264149, "longitude": 10.526420 }, "description": "bar" }`}, testUser)
	assert.Nil(t, err)
	responseEvent := Event{}
	json.Unmarshal([]byte(response), &responseEvent)

	response, err = getEvent(stub, []string{responseEvent.ID})
	assert.Nil(t, err)

	actualEvent := Event{}
	json.Unmarshal([]byte(response), &actualEvent)
	assert.Equal(t, defaultCoordinates(), actualEvent.Location)
	assert.Equal(t, "abc", actualEvent.Image)
}

func TestGetEvents(t *testing.T) {
	stub := &CustomMockStub{shim.NewMockStub("test", &Eventcc{})}
	stub.MockTransactionStart("test123")

	_, err := addEvent(stub, []string{defaultEventArg}, testUser)
	assert.Nil(t, err)

	_, err = addEvent(stub, []string{`{ "location": { "latitute": 42, "longitude": 8}}`}, testUser)
	assert.Nil(t, err)

	response, err := getEvents(stub)
	assert.Nil(t, err)

	events := []Event{}
	json.Unmarshal([]byte(response), &events)
	assert.Len(t, events, 2)
}

/*
func TestQueryEvents(t *testing.T) {
	stub := &CustomMockStub{shim.NewMockStub("test", &Eventcc{})}
	stub.MockTransactionStart("test123")

	_, err := addEvent(stub, []string{defaultEventArg}, testUser)
	assert.Nil(t, err)

	_, err = addEvent(stub, []string{`{ "location": { "latitute": 42, "longitude": 8}}`}, testUser)
	assert.Nil(t, err)

	defaultQueryString := fmt.Sprintf(`{"selector":{"docType":"event","location.latitude":{"$gte":%f,"$lte":%f},"location.longitude":{"$gte":%f,"$lte":%f}}}`,
		43.0, 100.0, 0.0, 100.0)
	response, err := queryEvents(stub, []string{defaultQueryString})
	assert.Nil(t, err)

	events := []Event{}
	json.Unmarshal([]byte(response), &events)
	assert.Len(t, events, 1)
}*/
