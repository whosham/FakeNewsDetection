package main

// FullEvent describes an event with the corresponding assessments
type FullEvent struct {
	Event       Event        `json:"event"`
	Assessments []Assessment `json:"assessments"`
}
