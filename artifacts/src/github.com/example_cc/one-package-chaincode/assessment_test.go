package main

import (
	"crypto/rand"
	"crypto/rsa"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"strconv"
	"testing"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/stretchr/testify/assert"
)

func TestJudgeEvent(t *testing.T) {
	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")

	_, err := trackLocation(stub, []string{`{"latitude":52.264149,"longitude":10.526420}`}, testUser2)
	assert.Nil(t, err)

	response, err := addEvent(stub, []string{defaultEventArg}, testUser)
	assert.Nil(t, err)
	responseEvent := Event{}
	json.Unmarshal([]byte(response), &responseEvent)

	response, err = judgeEvent(stub, []string{
		fmt.Sprintf(`{"event":"%s","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]}`,
			responseEvent.ID)}, testUser2)
	assert.Nil(t, err)

	assessment := Assessment{}
	json.Unmarshal([]byte(response), &assessment)

	assert.Equal(t, responseEvent.ID, assessment.Event)

	response, err = getEvent(stub, []string{responseEvent.ID})
	assert.Nil(t, err)
	json.Unmarshal([]byte(response), &responseEvent)
	oldT := responseEvent.Trustworthiness

	response, err = judgeEvent(stub, []string{
		fmt.Sprintf(`{"event":"%s","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]}`,
			responseEvent.ID)}, testUser2)
	assert.Nil(t, err)

	response, err = getEvent(stub, []string{responseEvent.ID})
	assert.Nil(t, err)
	json.Unmarshal([]byte(response), &responseEvent)
	newT := responseEvent.Trustworthiness
	assert.Equal(t, oldT, newT)
}

func TestJudgeEventEncryptedLocation(t *testing.T) {
	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")

	sk, _ := rsa.GenerateKey(rand.Reader, 2048)
	encryptor := RSAEncryptor{sk: sk}

	l := Location{Coordinates: defaultCoordinates()}

	s, err := encryptor.EncryptInterfaceToString(l)
	assert.Nil(t, err)

	encLoc, err := trackEncryptedLocation(stub, []string{s}, testUser2)
	assert.Nil(t, err)
	var he HybridEncrypted
	err = json.Unmarshal([]byte(encLoc), &he)
	assert.Nil(t, err)
	key := he.Key
	plainKey, err := encryptor.Decrypt([]byte(key))
	plainKeyB64 := base64.StdEncoding.EncodeToString(plainKey)
	assert.Nil(t, err)

	response, err := addEvent(stub, []string{defaultEventArg}, testUser)
	assert.Nil(t, err)
	responseEvent := Event{}
	json.Unmarshal([]byte(response), &responseEvent)

	response, err = judgeEventEncryptedLocation(stub, []string{
		fmt.Sprintf(`{"event":"%s","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]}`,
			responseEvent.ID), fmt.Sprintf("[\"%s\"]", plainKeyB64)}, testUser2)
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

func TestContainsFlaws(t *testing.T) {
	user := "user123456"
	timestamp := 10000000
	locations := []Location{}
	for i := 0; i < 24; i++ {
		timestamp += 60 * 60
		locations = append(locations, NewLocation(user, 52, 10, strconv.Itoa(timestamp)))
	}
	flaws := containsFlaws(locations, timestamp)
	assert.False(t, flaws)
}

func TestContainsFlawsWithFlaw(t *testing.T) {
	user := "user123456"
	timestamp := 10000000
	locations := []Location{}
	for i := 0; i < 24; i++ {
		timestamp += 60 * 60
		locations = append(locations, NewLocation(user, 52, 10, strconv.Itoa(timestamp)))
	}
	flawedLocation := locations[10]
	flawedLocation.Coordinates.Longitude = -10
	locations[10] = flawedLocation

	flaws := containsFlaws(locations, timestamp)
	assert.False(t, flaws)
}

func TestContainsFlawsWithMissing1(t *testing.T) {
	user := "user123456"
	timestamp := 10000000
	locations := []Location{}
	for i := 0; i < 23; i++ {
		timestamp += 60 * 60
		locations = append(locations, NewLocation(user, 52, 10, strconv.Itoa(timestamp)))
	}
	flaws := containsFlaws(locations, timestamp)
	assert.False(t, flaws)
}

func TestContainsFlawsForEmpty(t *testing.T) {
	flaws := containsFlaws([]Location{}, 1)
	assert.True(t, flaws)
}
