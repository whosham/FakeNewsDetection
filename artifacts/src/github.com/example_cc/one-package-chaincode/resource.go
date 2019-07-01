package main

// Resource represents resources saved in the ledger
type Resource struct {
	Name    string `json:"name"`
	Type    string `json:"type"`
	Content string `json:"content"`
}
