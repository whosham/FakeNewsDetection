package main

import "math"

// Coordinates object to describe the position on earth
type Coordinates struct {
	Latitude  float64 `json:"latitude"`  // between -90 and 90
	Longitude float64 `json:"longitude"` // between -180 and 180
}

// Brunswick
func defaultCoordinates() Coordinates {
	return Coordinates{52.264149, 10.526420}
}

const maxDecimalDiff = 0.1
const earthRadiusInMeters = 6371009

// from https://en.wikipedia.org/wiki/Geographical_distance
func distanceInMeters(a Coordinates, b Coordinates) float64 {
	// use Spherical Earth projected to a plane method
	diffLat := (a.Latitude - b.Latitude) * (math.Pi / 180)
	diffLong := (a.Longitude - b.Longitude) * (math.Pi / 180)
	meanLat := ((a.Latitude + b.Latitude) / 2) * (math.Pi / 180)
	distance := earthRadiusInMeters * math.Sqrt(math.Pow(diffLat, 2)+math.Pow(math.Cos(meanLat)*diffLong, 2))
	return distance
}
