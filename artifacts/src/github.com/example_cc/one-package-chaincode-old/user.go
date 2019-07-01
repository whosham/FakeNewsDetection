package main

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

const keyPrefixLength = 5

func getKeyPrefixForUserID(id string) string {
	return id[0:keyPrefixLength]
}
