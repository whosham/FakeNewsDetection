package eventcc

import (
	"encoding/json"
	"fmt"
	"testing"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/stretchr/testify/assert"
)

func TestJudgeEvent(t *testing.T) {
	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")

	location, err := trackLocation(stub, []string{`{"latitute":52.264149,"longitude":10.526420}`}, testUser2)
	assert.Nil(t, err)

	response, err := addEvent(stub, []string{defaultEventArg}, testUser)
	assert.Nil(t, err)
	responseEvent := Event{}
	json.Unmarshal([]byte(response), &responseEvent)

	response, err = judgeEvent(stub, []string{
		fmt.Sprintf(`{"event":"%s","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]}`,
			responseEvent.ID), fmt.Sprintf("[%s]", location)}, testUser2)
	assert.Nil(t, err)

	assessment := Assessment{}
	json.Unmarshal([]byte(response), &assessment)

	assert.Equal(t, responseEvent.ID, assessment.Event)

	response, err = getEvent(stub, []string{responseEvent.ID})
	assert.Nil(t, err)
	json.Unmarshal([]byte(response), &responseEvent)
	assert.True(t, responseEvent.Trustworthiness > 0)
}

func TestEarliestConsideredTime(t *testing.T) {
	time := "1000086400"
	earlyTime, err := getEarliestConsideredTime(time)
	assert.Nil(t, err)
	assert.Equal(t, "1000000000", earlyTime)
}

func TestJudgeEventTrackLocationAfterEvent(t *testing.T) {
	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")

	response, err := addEvent(stub, []string{pastEventArg}, testUser)
	assert.Nil(t, err)
	responseEvent := Event{}
	json.Unmarshal([]byte(response), &responseEvent)

	location, err := trackLocation(stub, []string{`{"latitute":52.264149,"longitude":10.526420}`}, testUser2)
	assert.Nil(t, err)

	response, err = judgeEvent(stub, []string{
		fmt.Sprintf(`{"event":"%s","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]}`,
			responseEvent.ID), fmt.Sprintf("[%s]", location)}, testUser2)
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "ledger locations")
}
