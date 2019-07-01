package main

import (
	"crypto/rand"
	"crypto/rsa"
	"encoding/json"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestECDSAEncryptLocation(t *testing.T) {
	e := NewECDSAencryptor()
	l := Location{Coordinates: Coordinates{Latitude: 52, Longitude: 10}, Signature: "blub", Timestamp: "123", User: "me"}
	encryptedL, err := e.encryptLocation(l)
	assert.Nil(t, err)
	assert.True(t, encryptedL == l)
	//assert.False(t, encryptedL == l)
}

func TestEncrypt(t *testing.T) {
	sk, _ := rsa.GenerateKey(rand.Reader, 2048)

	encryptor := RSAEncryptor{sk: sk}
	plain := "blub"
	cipher, err := encryptor.encryptString(plain)
	assert.Nil(t, err)

	decrypted, err := encryptor.decryptString(cipher)
	assert.Nil(t, err)
	assert.Equal(t, plain, decrypted)
}

func TestEncryptInterfaceToString(t *testing.T) {
	sk, _ := rsa.GenerateKey(rand.Reader, 2048)

	encryptor := RSAEncryptor{sk: sk}
	l := Location{Coordinates: defaultCoordinates()}
	heString, err := encryptor.EncryptInterfaceToString(l)
	assert.Nil(t, err)
	var he HybridEncrypted
	err = json.Unmarshal([]byte(heString), &he)
	assert.Nil(t, err)

	decrypted, err := encryptor.decryptString(&he)
	assert.Nil(t, err)
	assert.Contains(t, decrypted, "latitude")
}
