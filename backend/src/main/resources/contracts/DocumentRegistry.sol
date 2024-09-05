// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.24;

// Uncomment this line to use console.log
// import "hardhat/console.sol";

contract DocumentRegistry {
    address public owner;
    mapping(string => string) public requestToDocuments; // request id from postgres mapped to document hash

    constructor() {
        owner = msg.sender;
    }

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this function");
        _;
    }

    function addDocument(string memory _requestId, string memory _documentHash) public onlyOwner {
        requestToDocuments[_requestId] = _documentHash;
    }

    function getDocumentsForRequest(string memory _documentId) public view returns (string memory) {
        return requestToDocuments[_documentId];
    }
}
