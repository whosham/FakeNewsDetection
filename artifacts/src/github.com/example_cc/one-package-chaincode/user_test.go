package main

import (
	"encoding/json"
	"testing"

	"github.com/hyperledger/fabric/core/chaincode/shim"

	"github.com/stretchr/testify/assert"
)

func TestGetKeyforUserID(t *testing.T) {
	userID := "jimknopf123"
	key := getKeyforUserID(userID)
	assert.Equal(t, "user-jimkn", key)
}

func TestAddUserAndGetUser(t *testing.T) {
	stub := &CustomMockStub{shim.NewMockStub("test", &Eventcc{})}
	stub.MockTransactionStart("test123")
	userID := "jimknopf123"
	_, err := addUser(stub, []string{userID})
	assert.Nil(t, err)

	res, err := getUser(stub, []string{userID})
	assert.Nil(t, err)

	var user User
	err = json.Unmarshal([]byte(res), &user)
	assert.Equal(t, float64(1), user.Trustworthiness)
	assert.Equal(t, userID, user.ID)
}

func TestGetUserMissing(t *testing.T) {
	stub := &CustomMockStub{shim.NewMockStub("test", &Eventcc{})}
	stub.MockTransactionStart("test123")
	userID := "jimknopf123"
	_, err := getUser(stub, []string{userID})
	assert.Contains(t, err.Error(), "user not found")
}

func TestChangeUserScoreAndCreateUserIfNecessary(t *testing.T) {
	stub := &CustomMockStub{shim.NewMockStub("test", &Eventcc{})}
	stub.MockTransactionStart("test123")
	userID := "jimknopf123"

	_, err := changeUserScoreAndCreateUserIfNecessary(stub, userID, 2)
	assert.Nil(t, err)

	res, err := getUser(stub, []string{userID})
	assert.Nil(t, err)

	var user User
	err = json.Unmarshal([]byte(res), &user)
	assert.Equal(t, float64(3), user.Trustworthiness)
	assert.Equal(t, userID, user.ID)

}
