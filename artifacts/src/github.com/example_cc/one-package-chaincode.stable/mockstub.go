package main

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
)

type CustomMockStub struct {
	*shim.MockStub
}

// This is a hack to make GetQueryResult mockable without a query engine
// Only assessments are returned
func (stub *CustomMockStub) GetQueryResult(query string) (shim.StateQueryIteratorInterface, error) {
	iter := new(shim.MockStateRangeQueryIterator)
	iter.Closed = false
	iter.Stub = stub.MockStub
	iter.StartKey = "assessment-00000"
	iter.EndKey = "assessment-zzzzz"
	iter.Current = stub.MockStub.Keys.Front()

	iter.Print()
	return iter, nil
}
