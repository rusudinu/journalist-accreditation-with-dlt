```shell
rm -rf ignition/deployments/
rm -rf artifacts/ cache/ typechain-types/
npx hardhat node
npx hardhat compile
npx hardhat ignition deploy ./ignition/modules/DocumentRegistry.ts --network localhost
```

Generate new sources from .sol

```bash
mvn web3j:generate-sources
```

Remember to add the contract in /resources/contracts
