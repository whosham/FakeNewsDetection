package main

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"encoding/json"
)

type encryptor interface {
	encryptLocation(Location) (Location, error)
}

func NewDefaultEncryptor(s string) (encryptor, error) {
	return &ECDSAencryptor{}, nil
}

type ECDSAencryptor struct {
}

type HybridEncrypted struct {
	Key    []byte `json:"key"`
	Cipher []byte `json:"cipher"`
}

type HybridEncryptedB64 struct {
	Key    string `json:"key"`
	Cipher string `json:"cipher"`
}

type RSAEncryptor struct {
	sk *rsa.PrivateKey
}

func NewRSAEncryptor(sk *rsa.PrivateKey) RSAEncryptor {
	return RSAEncryptor{sk: sk}
}

func (e *RSAEncryptor) EncryptInterfaceToString(i interface{}) (string, error) {
	he, err := e.encryptInterface(i)
	if err != nil {
		return "", err
	}

	bytes, err := json.Marshal(he)
	if err != nil {
		return "", err
	}

	return string(bytes), nil
}

func (e *RSAEncryptor) encryptInterface(i interface{}) (*HybridEncrypted, error) {
	j, err := json.Marshal(i)
	if err != nil {
		return &HybridEncrypted{}, err
	}
	return e.encryptBytes(j)
}

func (e *RSAEncryptor) encryptString(s string) (*HybridEncrypted, error) {
	return e.encryptBytes([]byte(s))
}

func (e *RSAEncryptor) encryptBytes(message []byte) (*HybridEncrypted, error) {
	aesKey := make([]byte, 16)
	_, err := rand.Read(aesKey)
	if err != nil {
		return nil, err
	}

	block, err := aes.NewCipher(aesKey)
	if err != nil {
		panic(err.Error())
	}

	nonce := []byte{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}

	aesgcm, err := cipher.NewGCM(block)
	if err != nil {
		panic(err.Error())
	}

	ciphertext := aesgcm.Seal(nil, nonce, message, nil)

	label := []byte("aes key")
	hash := sha256.New()
	encryptedAesKey, err := rsa.EncryptOAEP(
		hash,
		rand.Reader,
		&e.sk.PublicKey,
		aesKey,
		label,
	)
	if err != nil {
		return nil, err
	}

	return &HybridEncrypted{
		Key:    encryptedAesKey,
		Cipher: ciphertext,
	}, nil
}

func (e *RSAEncryptor) Decrypt(c []byte) ([]byte, error) {
	label := []byte("aes key")
	hash := sha256.New()
	p, err := rsa.DecryptOAEP(hash, rand.Reader, e.sk, c, label)
	if err != nil {
		return nil, err
	}
	return p, nil
}

func (e *RSAEncryptor) decryptString(he *HybridEncrypted) (string, error) {
	aesKey, err := e.Decrypt(he.Key)
	if err != nil {
		return "", err
	}

	plaintext, err := decryptWithAesKey(aesKey, he.Cipher)
	if err != nil {
		return "", err
	}

	return string(plaintext), nil
}

func decryptWithAesKey(key []byte, ciphertext []byte) ([]byte, error) {
	block, err := aes.NewCipher(key)
	if err != nil {
		panic(err.Error())
	}

	nonce := []byte{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}

	aesgcm, err := cipher.NewGCM(block)
	if err != nil {
		panic(err.Error())
	}

	plaintext, err := aesgcm.Open(nil, nonce, []byte(ciphertext), nil)
	if err != nil {
		return nil, err
	}
	return plaintext, nil
}

func NewECDSAencryptor() *ECDSAencryptor {
	return &ECDSAencryptor{}
}

func (e *ECDSAencryptor) encryptString(s string) (string, error) {
	return s, nil
}

func (e *ECDSAencryptor) encryptLocation(l Location) (Location, error) {
	return l, nil
}
