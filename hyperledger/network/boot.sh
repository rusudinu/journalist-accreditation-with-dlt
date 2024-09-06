#!/bin/bash
./network.sh down
# ./network.sh up createChannel -c mychannel -ca
./network.sh up createChannel
./network.sh deployCC -ccn prescription -ccp ../contracts/prescription -ccl typescript
./network.sh deployCC -ccn vitaldata -ccp ../contracts/vital-data -ccl typescript


# to add the org3
cd addOrg3
./addOrg3.sh up
