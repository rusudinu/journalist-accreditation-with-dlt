# Journalist accreditation with dlt

Swagger can be found [here](http://localhost:8080/swagger-ui/index.html) and [here for the Hyperledger microservice](http://localhost:3000/api)

## Users

journalist: journalist, journalist

ministry: ministry, ministry

juridic: juridic, juridic

director: director, director

## Auth in swagger

Click on Authorize, then in client_id add journalist-accreditation then press on Authorize.

Start the hyperledger chain by running server-boot.sh found in the hyperledger folder.

Remember after changing the contract to run npm run build in its folder.

When running the core backend set either ethereum or hyperledger as profile.

# Block explorer:

https://github.com/web3labs/chainlens-free/blob/master/docker-compose/README.md
NODE_ENDPOINT=http://host.docker.internal:8545 docker compose up
