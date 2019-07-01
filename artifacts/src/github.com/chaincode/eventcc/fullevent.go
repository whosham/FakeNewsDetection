package eventcc

// FullEvent describes an event with the corresponding assessments
type FullEvent struct {
	Event       Event
	Assessments []Assessment
}
