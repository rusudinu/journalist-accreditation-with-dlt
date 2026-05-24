# Installation & Setup Guide

## Prerequisites

Make sure you have the following installed on your machine:

- **Docker** & **Docker Compose**
- **Java 17** (JDK)
- **Maven** (or use the included Maven wrapper `./mvnw`)
- **Node.js** (LTS) & **npm**
- **Flutter SDK** (>= 3.3.3, < 4.0.0) — only if running the wallet app

---

## 1. Start Infrastructure (PostgreSQL & Keycloak)

From the project root, start the required services:

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 16.2** on port `5432` (user: `root`, password: `root`, database: `journalist_accreditation`)
- **Keycloak 24.0.4** on port `9001` (admin credentials: `admin` / `admin`)

### Import Keycloak Realm

Copy the realm configuration into the Keycloak container and import it:

```bash
docker cp journalist-accreditation-realm.json journalist-accreditation-keycloak:/tmp/journalist-accreditation-realm.json
docker exec -it journalist-accreditation-keycloak /opt/keycloak/bin/kc.sh import --file /tmp/journalist-accreditation-realm.json
```

Then restart Keycloak so the realm is loaded:

```bash
docker compose restart journalist-accreditation-keycloak
```

---

## 2. Ethereum (Hardhat) Setup

Choose this path if you want to use the **Ethereum** DLT backend.

```bash
cd eth
npm install
```

### Start a Local Hardhat Node

```bash
npx hardhat node
```

> Keep this terminal open. The local node runs on `http://localhost:8545` with chain ID `1337`.

### Deploy the Smart Contract

In a new terminal:

```bash
cd eth
npx hardhat ignition deploy ignition/modules/DocumentRegistry.ts --network localhost
```

The default contract address used by the backend is `0x5FbDB2315678afecb367f032d93F642f64180aa3`.

---

## 3. Hyperledger Fabric Setup (Alternative to Ethereum)

Choose this path if you want to use the **Hyperledger Fabric** DLT backend instead.

### Start the Network & Deploy Chaincode

```bash
cd hyperledger
chmod +x server-boot.sh
./server-boot.sh
```

This will bring up the Fabric network, create a channel, and deploy the `registry` chaincode.

### Start the Hyperledger Microservice

```bash
cd hyperledger/backend
npm install
npm run start
```

The Hyperledger microservice will be available at `http://localhost:3000`. Swagger docs at `http://localhost:3000/api`.

---

## 4. Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=ethereum
```

Replace `ethereum` with `hyperledger` if you set up the Hyperledger Fabric network instead.

The backend runs on `http://localhost:8080`.  
Swagger UI is available at: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Swagger Authentication

1. Click **Authorize** in Swagger UI.
2. Enter `journalist-accreditation` as the `client_id`.
3. Click **Authorize**.

---

## 5. Frontend (React + Vite)

```bash
cd frontend
npm install
npm run dev
```

The frontend dev server will start (default: `http://localhost:5173`).

---

## 6. Wallet (Flutter) — Optional

```bash
cd wallet
flutter pub get
flutter run
```

> Requires a connected device or emulator.

---

## Default Users

| Username   | Password   | Role      |
|------------|------------|-----------|
| journalist | journalist | Journalist|
| ministry   | ministry   | Ministry  |
| juridic    | juridic    | Juridic   |
| director   | director   | Director  |
| deputy1    | deputy1    | Deputy    |
| deputy2    | deputy2    | Deputy    |
| deputy3    | deputy3    | Deputy    |
| admin      | admin      | Admin     |

---

## Quick Start Summary

```bash
# 1. Infrastructure
docker compose up -d

# 2. Ethereum local node (in its own terminal)
cd eth && npm install && npx hardhat node

# 3. Deploy contract (in a new terminal)
cd eth && npx hardhat ignition deploy ignition/modules/DocumentRegistry.ts --network localhost

# 4. Backend
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=ethereum

# 5. Frontend
cd frontend && npm install && npm run dev
```
