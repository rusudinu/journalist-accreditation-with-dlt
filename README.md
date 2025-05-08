# Journalist accreditation with dlt

Swagger can be found [here](http://localhost:8080/swagger-ui/index.html) and [here for the Hyperledger microservice](http://localhost:3000/api)

## Users

journalist: journalist, journalist

ministry: ministry, ministry

juridic: juridic, juridic

director: director, director

deputy1: deputy1, deputy1

deputy2: deputy2, deputy2

deputy3: deputy3, deputy3

admin: admin, admin

## Auth in swagger

Click on Authorize, then in client_id add journalist-accreditation then press on Authorize.

Start the hyperledger chain by running server-boot.sh found in the hyperledger folder.

Remember after changing the contract to run npm run build in its folder.

When running the core backend set either ethereum or hyperledger as profile.

# Block explorer:

https://github.com/web3labs/chainlens-free/blob/master/docker-compose/README.md
NODE_ENDPOINT=http://host.docker.internal:8545 docker compose up

## System description
Documents (bills) have to be approved. An approval process is defined by an admin. The approval must pass multiple steps. Each step requires a minimum number of reviewers and can be a step that requires approval or can be a step in which only comments are left. These reviewers are assigned to each step by the system.


We need to extend the project to also handle bills. New bills can be created by anyone. After creation, a bill (document) must be put on an approval process that is created by an admin.

Admins can see the bills that need to be assigned to an approval process.

The admin can configure the phases of the approval process. For each phase he will be presented with a randomly chosen set of reviewers. He can choose the reviewers he wants to assign to that phase if he is not satisfied with that list.


# Export keycloak realm

```bash
docker exec -it journalist-accreditation-keycloak /bin/sh
```

```bash
/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/import --realm journalist-accreditation --users realm_file
```
