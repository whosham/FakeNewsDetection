## Making a connection to fabric network using API 

based on the balance-transfer in fabric-samples. 


```
cd fabric-samples/balance-transfer/
```
NOTE: Bug fixed that prevents chaincode from being instantiated and it exits with Timeout error. 
update timeout on app/instantiate-chaincode.js from 60000 to 120000 

```
//              let results = await channel.sendInstantiateProposal(request, 60000); //instantiate takes much longer
                let results = await channel.sendInstantiateProposal(request, 120000); //instantiate takes much longer 
```

Once you have completed the above setup, you will have provisioned a local network with the following docker container configuration:

* 2 CAs
* A SOLO orderer
* 4 peers (2 peers per Org)
* 2 couch dbs 

#### Artifacts
* Crypto material has been generated using the **cryptogen** tool from Hyperledger Fabric and mounted to all peers, the orderering node and CA containers. More details regarding the cryptogen tool are available [here](http://hyperledger-fabric.readthedocs.io/en/latest/build_network.html#crypto-generator).
* An Orderer genesis block (genesis.block) and channel configuration transaction (mychannel.tx) has been pre generated using the **configtxgen** tool from Hyperledger Fabric and placed within the artifacts folder. More details regarding the configtxgen tool are available [here](http://hyperledger-fabric.readthedocs.io/en/latest/build_network.html#configuration-transaction-generator).

## Running the sample program

There are two options available for running the balance-transfer sample
For each of these options, you may choose to run with chaincode written in golang or in node.js.

```
cd fabric-samples/balance-transfer

./runApp.sh

```

* This launches the required network on your local machine
* Installs the fabric-client and fabric-ca-client node modules
* And, starts the node app on PORT 4000

##### Terminal Window 2


With the application started in terminal 1, next, test the APIs by executing the script - **testAPIs.sh**:
```
cd fabric-samples/balance-transfer

## To use golang chaincode execute the following command

./testAPIs.sh -l golang

## OR use node.js chaincode

./testAPIs.sh -l node
```

* This will create channel and make the peers joining it, install and instantiate the chaincode. 


## Sample REST APIs Requests

* This api called from android side and return json web token used to query and update the chaincode. 

### Login Request

* Register and enroll new users in Organization - **Org1**:

`curl -s -X POST http://localhost:4000/users -H "content-type: application/x-www-form-urlencoded" -d 'username=Jim&orgName=Org1'`

**OUTPUT:**

```
{
  "success": true,
  "secret": "RaxhMgevgJcm",
  "message": "Jim enrolled Successfully",
  "token": "<put JSON Web Token here>"
}
```

The response contains the success/failure status, an **enrollment Secret** and a **JSON Web Token (JWT)** that is a required string in the Request Headers for subsequent requests.

### Invoke request

This invoke request is signed by peers from both orgs, *org1* & *org2*.
```
curl -s -X POST \
  http://localhost:4000/channels/mychannel/chaincodes/mycc \
  -H "authorization: Bearer <put JSON Web Token here>" \
  -H "content-type: application/json" \
  -d '{
	"peers": ["peer0.org1.example.com","peer0.org2.example.com"],
	"fcn":"move",
	"args":["a","b","10"]
}'
```




### Chaincode Query

```
curl -s -X GET \
  "http://localhost:4000/channels/mychannel/chaincodes/mycc?peer=peer0.org1.example.com&fcn=query&args=%5B%22a%22%5D" \
  -H "authorization: Bearer <put JSON Web Token here>" \
  -H "content-type: application/json"
``` 



### Clean the network

The network will still be running at this point. Before starting the network manually again, here are the commands which cleans the containers and artifacts.
./tearDown.sh 

