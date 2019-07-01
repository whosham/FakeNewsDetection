package main

import (
	"crypto/rand"
	"crypto/rsa"
	"encoding/json"
	"testing"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/stretchr/testify/assert"
)

func TestTrackLocation(t *testing.T) {
	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")

	response, err := trackLocation(stub, []string{`{"latitude":52.264149,"longitude":10.526420}`}, testUser)
	assert.Nil(t, err)

	location := Location{}
	json.Unmarshal([]byte(response), &location)
	assert.Equal(t, defaultCoordinates(), location.Coordinates)
}

func TestEncryptLocation(t *testing.T) {
	sk, _ := rsa.GenerateKey(rand.Reader, 2048)
	encryptor := RSAEncryptor{sk: sk}

	l := Location{Coordinates: defaultCoordinates()}

	s, err := encryptor.EncryptInterfaceToString(l)
	assert.Nil(t, err)

	stub := shim.NewMockStub("test", &Eventcc{})
	stub.MockTransactionStart("test123")
	encryptedLoc, err := trackEncryptedLocation(stub, []string{s}, testUser)
	assert.Nil(t, err)

	var he HybridEncrypted
	err = json.Unmarshal([]byte(encryptedLoc), &he)
	assert.Nil(t, err)

	decrypted, err := encryptor.decryptString(&he)
	assert.Nil(t, err)

	assert.Contains(t, decrypted, "latitude")
}
