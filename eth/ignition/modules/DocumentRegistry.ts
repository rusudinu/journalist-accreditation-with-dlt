import {buildModule} from '@nomicfoundation/hardhat-ignition/modules';

const DocumentRegistryModule = buildModule('DocumentRegistryModule', (m: any) => {
    const documentRegistryRecord = m.contract('DocumentRegistry');
    return {documentRegistryRecord};
});

export default DocumentRegistryModule;
