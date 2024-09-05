Generate new sources from .sol

```bash
mvn web3j:generate-sources
```

Remember to add the contract in /resources/contracts

Generate RSA Key

```bash
keytool -genkeypair -keyalg RSA -storetype JKS -validity 365 -keystore ministry_key.jks -alias ministry_key
```
