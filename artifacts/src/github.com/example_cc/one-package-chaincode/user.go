package main

import (
	"encoding/json"
	"fmt"

	"github.com/pkg/errors"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/sirupsen/logrus"
)

// User represents users who interact with the chaincode
type User struct {
	ObjectType      string  `json:"docType"`
	ID              string  `json:"id"`
	Trustworthiness float64 `json:"trustworthiness"`
}

// NewUser creates the default user
func NewUser(id string) User {
	return User{ObjectType: "user", ID: id, Trustworthiness: 1}
}

func getKeyforUserID(id string) string {
	return "user-" + getKeyPrefixForUserID(id)
}

func getKeyPrefixForUserID(id string) string {
	return id[0:userKeyPrefixLength]
}

func changeUserScoreAndCreateUserIfNecessary(stub shim.ChaincodeStubInterface, userId string, change float64) (*User, error) {
	key := getKeyforUserID(userId)
	user, err := getAndCreateUserIfNecessary(stub, userId)
	if err != nil {
		return nil, err
	}
	user.Trustworthiness += change

	userAsBytes, err := json.Marshal(user)
	if err != nil {
		return nil, err
	}

	err = stub.PutState(key, userAsBytes)
	if err != nil {
		return nil, err
	}

	return user, nil
}

func getAndCreateUserIfNecessary(stub shim.ChaincodeStubInterface, userId string) (*User, error) {
	key := getKeyforUserID(userId)
	value, err := stub.GetState(key)
	if err != nil {
		return nil, errors.Wrap(err, "failed to get user")
	}
	var user User
	if value == nil {
		user = NewUser(userId)
	} else {
		err = json.Unmarshal(value, &user)
		if err != nil {
			return nil, err
		}
	}
	return &user, nil
}

func addUser(stub shim.ChaincodeStubInterface, args []string) (string, error) {
	if len(args) != 1 {
		return "", fmt.Errorf("Incorrect arguments. Expecting a user id")
	}

	user := NewUser(args[0])
	bytes, err := json.Marshal(user)
	if err != nil {
		return "", err
	}
	key := getKeyforUserID(args[0])
	err = stub.PutState(key, bytes)
	if err != nil {
		return "", fmt.Errorf("Failed to addUser: %s with error: %s", args[0], err)
	}
	logrus.Info("Add user: ", string(bytes))
	return string(bytes), nil
}

func getUser(stub shim.ChaincodeStubInterface, args []string) (string, error) {
	if len(args) != 1 {
		return "", fmt.Errorf("Incorrect arguments. Expecting a user id")
	}

	key := getKeyforUserID(args[0])
	value, err := stub.GetState(key)
	if err != nil {
		return "", fmt.Errorf("Failed to getUser: %s with error: %s", args[0], err)
	}
	if value == nil {
		return "", fmt.Errorf("user not found: %s", args[0])
	}
	logrus.Info("Get user: ", string(value))
	return string(value), nil
}
