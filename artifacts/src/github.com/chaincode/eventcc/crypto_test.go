package eventcc

import (
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
