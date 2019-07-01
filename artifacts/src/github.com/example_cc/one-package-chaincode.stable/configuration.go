package main

const (
	maxAllowedDistanceInMeters   = 100 * 1000
	consideredSecondsBeforeEvent = 60 * 60 * 24 // 24 hours
	diffBetweenLocations         = 60 * 10      // 10 minutes
	locationsPerAssessment       = consideredSecondsBeforeEvent / diffBetweenLocations

	clientToCCEncryption = false
	locationEncryption   = false

	userKeyPrefixLength = 5

	allowToSetLocationTimestamp = true

	adjustUserRating = true
)
