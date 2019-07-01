package main

type encryptor interface {
	encryptLocation(Location) (Location, error)
}

func NewDefaultEncryptor(s string) (encryptor, error) {
	return &ECDSAencryptor{}, nil
}

type ECDSAencryptor struct {
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
