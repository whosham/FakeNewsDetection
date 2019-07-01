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

	response, err := addEvent(stub, []string{defaultEventArg}, testUser)
	assert.Nil(t, err)
	responseEvent := Event{}
	json.Unmarshal([]byte(response), &responseEvent)

	response, err = getEvent(stub, []string{responseEvent.ID})
	assert.Nil(t, err)

	actualEvent := Event{}
	json.Unmarshal([]byte(response), &actualEvent)
	assert.Equal(t, defaultCoordinates(), actualEvent.Location)
}
