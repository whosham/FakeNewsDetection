package main

import (
	"encoding/json"
	"fmt"
	"math"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/pkg/errors"
	"github.com/sirupsen/logrus"
)

// Assessment is used to assess an event
type Assessment struct {
	ObjectType      string     `json:"docType"`
	ID              string     `json:"id"`
	Timestamp       string     `json:"timestamp"`
	Event           string     `json:"event"`
	Creator         string     `json:"creator"`
	Resources       []Resource `json:"resources"`
	Rating          rating     `json:"rating"`
	Trustworthiness float64    `json:"trustworthiness"`
}

// TODO: should only be a value between -1 and 1
type rating float64

// NewAssessment creates an default Assessment object
func NewAssessment() Assessment {
	return Assessment{ObjectType: "assessment"}
}

func composeAssessmentID(eventID string, userID string) string {
	return "assessment-" + eventID[len("event-"):] + "-" + getKeyPrefixForUserID(userID)
}

func judgeEvent(stub shim.ChaincodeStubInterface, args []string, clientID string) (string, error) {

	if len(args) != 2 {
		logrus.Infof("JudgeEvent with incorrect arguments: %s", args)
		//TODO: improve error msg
		return "", fmt.Errorf("incorrect arguments: expecting assessment object as: " +
			`{"event":"event-id","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]},"[{"docType":"location","user":"eDUwOTo6Q049QWRtaW5Ab3JnMS5leGFtcGxlLmNvbSxMPVNhbiBGcmFuY2lzY28sU1Q9Q2FsaWZvcm5pYSxDPVVTOjpDTj1jYS5vcmcxLmV4YW1wbGUuY29tLE89b3JnMS5leGFtcGxlLmNvbSxMPVNhbiBGcmFuY2lzY28sU1Q9Q2FsaWZvcm5pYSxDPVVT","coordinates":{"latitude":52,"longitude":0},"timestamp":"1556445221","signature":""}]"`)
	}

	assessment := Assessment{}
	err := json.Unmarshal([]byte(args[0]), &assessment)
	if err != nil {
		return "", err
	}

	var locations = []Location{}
	err = json.Unmarshal([]byte(args[1]), &locations)
	if err != nil {
		return "", errors.Wrap(err, "failed to unmarshal locations")
	}

	value, err := stub.GetState(assessment.Event)
	if err != nil {
		return "", fmt.Errorf("failed to getEvent event: %s with error: %s", assessment.Event, err)
	}
	if value == nil {
		return "", fmt.Errorf("event not found: %s", assessment.Event)
	}

	event := Event{}
	err = json.Unmarshal(value, &event)
	if err != nil {
		return "", err
	}
	closeness, err := calculateUsersCloseness(stub, event, clientID, locations)
	if err != nil {
		return "", err
	}

	time, err := stub.GetTxTimestamp()
	if err != nil {
		return "", err
	}

	assessment.ID = composeAssessmentID(event.ID, clientID)
	assessment.ObjectType = "assessment"
	assessment.Timestamp = getTimeStamp(time.Seconds)
	assessment.Creator = clientID
	assessment.Trustworthiness = closeness * float64(assessment.Rating)
	assessmentAsBytes, err := json.Marshal(assessment)
	if err != nil {
		return "", errors.Wrap(err, "failed to marshal assessment")
	}
	err = stub.PutState(assessment.ID, assessmentAsBytes)
	if err != nil {
		return "", fmt.Errorf("failed to create assessment: %s", args)
	}
	logrus.Infof("Create assessment : %s", string(assessmentAsBytes))

	// TODO: if this is the second assessment of the user for this event, we have to reduce the value before
	event.Trustworthiness += assessment.Trustworthiness
	eventAsBytes, err := json.Marshal(event)
	if err != nil {
		return "", errors.Wrap(err, "failed to marshal event")
	}
	err = stub.PutState(event.ID, eventAsBytes)
	if err != nil {
		return "", fmt.Errorf("failed to update event: %s", args)
	}
	logrus.Infof("Update event : %s", string(eventAsBytes))

	return string(assessmentAsBytes), nil
}

func calculateUsersCloseness(stub shim.ChaincodeStubInterface, event Event, clientID string,
	providedLocations []Location) (float64, error) {

	logrus.Infof("calculate user %s closeness to event %s", clientID, event.ID)

	ledgerLocations, err := getLocationsFromLedgerBefore(stub, event.Timestamp, clientID)
	if err != nil {
		return 0, err
	}

	// TODO: implement encryption
	e, err := NewDefaultEncryptor(clientID)
	if err != nil {
		return 0, errors.Wrap(err, "failed to create encryptor")
	}

	same, err := compareLedgerAndPlainLocations(providedLocations, ledgerLocations, e)
	if err != nil {
		return 0, err
	}
	if !same {
		return 0, errors.New("provided and ledger locations are not the same")
	}

	return getClosenessForLocations(providedLocations, event.Location), nil

}

func compareLedgerAndPlainLocations(provided []Location, ledger []Location, e encryptor) (bool, error) {
	if ledger == nil {
		return false, errors.New("ledger locations should not be nil")
	}
	if provided == nil {
		return false, errors.New("provided locations should not be nil")
	}
	logrus.Infof("length ledger locations: %d", len(ledger))
	logrus.Infof("length provided locations: %d", len(provided))
	if len(provided) != len(ledger) {
		return false, errors.New("ledger and provided locations differ in length")
	}
	for i := 0; i < len(provided); i++ {
		encryptedLocation, err := e.encryptLocation(provided[i])
		if err != nil {
			return false, err
		}
		logrus.Debugf("compare location %s with location %s", ledger[i].Timestamp, encryptedLocation.Timestamp)
		if encryptedLocation != ledger[i] {
			return false, nil
		}
	}
	return true, nil
}

func getClosenessForLocations(locations []Location, c Coordinates) float64 {
	closeness := 0.0
	for _, l := range locations {
		distToEvent := distanceInMeters(l.Coordinates, c)
		// TODO: calculate something more accurate
		gap := 1.0 / locationsPerAssessment
		closeness += math.Min(gap/distToEvent, gap)
	}

	logrus.Infof("calculated closeness value of %f", closeness)
	return closeness
}

func getEarliestConsideredTime(time string) (string, error) {
	t, err := strconv.Atoi(time)
	if err != nil {
		return "", errors.Wrap(err, "failed to parse timestamp")
	}
	et := t - consideredSecondsBeforeEvent
	return strconv.Itoa(et), nil
}

func getLatestConsideredTime(time string) (string, error) {
	t, err := strconv.Atoi(time)
	if err != nil {
		return "", errors.Wrap(err, "failed to parse timestamp")
	}
	et := t + 1
	return strconv.Itoa(et), nil
}
