package main

import (
	"encoding/json"
	"strconv"
	"testing"
	"time"

	"fmt"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/stretchr/testify/assert"
)

const testUser = "testUser"
const testUser2 = "test2User"
const defaultEventArg = `{ "title": "foo", "location": { "latitude": 52.264149, "longitude": 10.526420 }, "description": "bar" }`

var pastEventArg = `{ "title": "foo", "location": { "latitude": 52.264149, "longitude": 10.526420 }, "description": "bar","timestamp": "` + strconv.Itoa(int(time.Now().Unix()-1)) + `"}`

func TestGetFullEvent(t *testing.T) {
	stub := &CustomMockStub{shim.NewMockStub("test", &Eventcc{})}
	stub.MockTransactionStart("test123")

	_, err := trackLocation(stub, []string{`{"latitute":52.264149,"longitude":10.526420}`}, testUser2)
	assert.Nil(t, err)

	response, err := addEvent(stub, []string{defaultEventArg}, testUser)
	assert.Nil(t, err)
	responseEvent := Event{}
	json.Unmarshal([]byte(response), &responseEvent)

	_, err = judgeEvent(stub, []string{
		fmt.Sprintf(`{"event":"%s","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]}`,
			responseEvent.ID)}, testUser2)
	assert.Nil(t, err)

	response, err = getFullEvent(stub, []string{responseEvent.ID})
	assert.Nil(t, err)

	fullEvent := FullEvent{}
	json.Unmarshal([]byte(response), &fullEvent)
	assert.Equal(t, defaultCoordinates(), fullEvent.Event.Location)
	assert.Len(t, fullEvent.Assessments, 1)
	assert.Equal(t, testUser2, fullEvent.Assessments[0].Creator)
}

/* TODO: adjust to new datamodel
func TestGetLocation(t *testing.T) {
	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")

	_, err := trackLocation(stub, []string{`{"latitute":52.264149,"longitude":10.526420}`}, testUser)
	assert.Nil(t, err)

	response, err := getLocation(stub, []string{testUser})
	assert.Nil(t, err)
	location := Location{}
	json.Unmarshal([]byte(response), &location)
	assert.Equal(t, DefaultCoordinates(), location.Coordinates)
}*/

func TestGetClosenessForLocations(t *testing.T) {
	l := []Location{{Coordinates: Coordinates{Longitude: 52, Latitude: 10}}}
	c := getClosenessForLocations(l, Coordinates{10, 52}, 0)
	expected := minLocationFactor
	if ignoreFlawsInLocTrace {
		expected = 1
	}
	assert.Equal(t, expected, c)
}
