package main

const (
	maxAllowedDistanceInMeters   = 100 * 1000
	consideredSecondsBeforeEvent = 60 * 60 * 24 // 24 hours
	diffBetweenLocations         = 60 * 10      // 10 minutes
	locationsPerAssessment       = consideredSecondsBeforeEvent / diffBetweenLocations

	minLocationFactor     = 0.1
	clientToCCEncryption  = false
	locationEncryption    = false
	ignoreFlawsInLocTrace = true

	userKeyPrefixLength = 5

	allowToSetLocationTimestamp = true

	adjustUserRating = true

	// 256 meters per second is the speed of a Boeing 747. If someone travelled faster than that we assume he cheated
	// with his location
	maxAllowedSpeedInMeterPerSecond = 256
)
