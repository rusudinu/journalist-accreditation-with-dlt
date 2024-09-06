#!/bin/bash
echo "[BOOT] ---STARTING PROJECT---"
echo "[BOOT] ---NETWORK---"
cd network
./network.sh down
./network.sh up createChannel
sleep 2
./network.sh deployCC -ccn registry -ccp ../contracts/registry -ccl typescript
sleep 2
echo "[BOOT] ---CHAINCODE DEPLOYED---"
