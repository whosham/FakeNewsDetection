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
	// TODO: remove it later, only for debugging
	Description string `json:"description"`
	Image       string `json:"image"`
}

// TODO: should only be a value between -1 and 1
type rating float64

type locationReceiver func(timestamp string) ([]Location, error)
type locationConverter func(value []byte, keyNumber int) (Location, error)

func plainConverter(value []byte, i int) (Location, error) {
	currentLocation := Location{}
	err := json.Unmarshal(value, &currentLocation)
	if err != nil {
		return currentLocation, errors.Wrap(err, "failed to unmarshal location")
	}
	return currentLocation, nil
}

// NewAssessment creates an default Assessment object
func NewAssessment() Assessment {
	return Assessment{ObjectType: "assessment"}
}

func composeAssessmentID(eventID string, userID string) string {
	return "assessment-" + eventID[len("event-"):] + "-" + getKeyPrefixForUserID(userID)
}

func judgeEvent(stub shim.ChaincodeStubInterface, args []string, clientID string) (string, error) {

	if len(args) != 1 {
		logrus.Infof("JudgeEvent with incorrect arguments: %s", args)
		//TODO: improve error msg
		return "", fmt.Errorf("incorrect arguments: expecting assessment object as: " +
			`{"event":"event-id","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]}`)
	}

	locationReceiver := func(timestamp string) ([]Location, error) {
		return getLocationsFromLedgerBefore(stub, timestamp, clientID, plainConverter)
	}

	return judgeEventForLocationsFromReceiver(stub, args, clientID, locationReceiver)
}

func judgeEventEncryptedLocation(stub shim.ChaincodeStubInterface, args []string, clientID string) (string, error) {

	if len(args) != 2 {
		logrus.Infof("JudgeEvent with incorrect arguments: %s", args)
		//TODO: improve error msg
		return "", fmt.Errorf("incorrect arguments: expecting assessment object as: " +
			`{"event":"event-id","rating":1.0,"resources":[{"name":"TestBild","type":"Img","content":"bild"}]},"["key1Base64","key2Base64"]"`)
	}

	var keys [][]byte
	e := json.Unmarshal([]byte(args[1]), &keys)
	if e != nil {
		return "", errors.Wrap(e, "failed to unmarshal keys")
	}

	encryptedConverter := func(value []byte, keyNumber int) (Location, error) {
		currentLocation := Location{}

		var he HybridEncrypted
		err := json.Unmarshal(value, &he)
		if err != nil {
			return currentLocation, errors.Wrap(err, "failed to unmarshal hybridEncrypted location")
		}

		loc, err := decryptWithAesKey(keys[keyNumber], he.Cipher)
		if err != nil {
			return currentLocation, errors.Wrap(err, "failed to decrypt location")
		}

		err = json.Unmarshal(loc, &currentLocation)
		if err != nil {
			return currentLocation, errors.Wrap(err, "failed to unmarshal location")
		}

		return currentLocation, nil
	}

	locationReceiver := func(timestamp string) ([]Location, error) {
		return getLocationsFromLedgerBefore(stub, timestamp, clientID, encryptedConverter)
	}

	return judgeEventForLocationsFromReceiver(stub, args, clientID, locationReceiver)

}

// the event trustworthy increase is defined by 2 values: the rating of the assessor and his location accuracy
// the event-creator trustworthy increase is defined only by the rating of the assessor
func judgeEventForLocationsFromReceiver(stub shim.ChaincodeStubInterface, args []string, clientID string, getLocations locationReceiver) (string, error) {
	assessment := Assessment{}
	err := json.Unmarshal([]byte(args[0]), &assessment)
	if err != nil {
		return "", err
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

	locations, err := getLocations(event.Timestamp)
	if err != nil {
		return "", errors.Wrap(err, "failed to get locations:")
	}
	logrus.Info("found locations:", locations)

	closeness := getClosenessForLocations(locations, event.Location)

	time, err := stub.GetTxTimestamp()
	if err != nil {
		return "", err
	}

	assessorRating, err := getAssessorRating(stub, clientID)
	if err != nil {
		return "", err
	}

	trustDelta := getTrustDelta(assessorRating, closeness)

	assessment.ID = composeAssessmentID(event.ID, clientID)
	assessment.ObjectType = "assessment"
	assessment.Timestamp = getTimeStamp(time.Seconds)
	assessment.Creator = clientID
	assessment.Trustworthiness = trustDelta * float64(assessment.Rating)
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

	if adjustUserRating {
		_, err = changeUserScoreAndCreateUserIfNecessary(stub, event.Creator, assessorRating*float64(assessment.Rating))
		if err != nil {
			return "", err
		}
	}

	return string(assessmentAsBytes), nil
}

func getTrustDelta(assessorRating float64, closeness float64) float64 {
	trustDelta := assessorRating * closeness * 5
	return math.Max(trustDelta, 0.5)
}

func getAssessorRating(stub shim.ChaincodeStubInterface, clientID string) (float64, error) {
	user, err := getAndCreateUserIfNecessary(stub, clientID)
	if err != nil {
		return 0, err
	}

	return calculateRatingForTrustworthiness(user.Trustworthiness), nil
}

func calculateRatingForTrustworthiness(trust float64) float64 {
	if trust < 1 {
		return 0
	} else if trust < 10 {
		return 1
	} else if trust < 30 {
		return 2
	} else if trust < 100 {
		return 3
	} else if trust < 500 {
		return 4
	} else {
		return 5
	}
}

// deprecated
func calculateUsersCloseness(stub shim.ChaincodeStubInterface, event Event, clientID string,
	providedLocations []Location) (float64, error) {

	logrus.Infof("calculate user %s closeness to event %s", clientID, event.ID)

	ledgerLocations, err := getLocationsFromLedgerBefore(stub, event.Timestamp, clientID, plainConverter)
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

// deprecated
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

func containsFlaws(locations []Location, startTime int) bool {

	flawCounter := 0

	for _, l := range locations {
		flawCounter += int(l.Coordinates.Longitude)
	}

	return false
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
