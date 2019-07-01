package eventcc

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestDistanceInMeters(t *testing.T) {
	brunswick := defaultCoordinates()
	hannover := Coordinates{52.375893, 9.732010}

	distance := distanceInMeters(brunswick, hannover)

	assert.InDelta(t, 55467, int(distance), 100)
}
