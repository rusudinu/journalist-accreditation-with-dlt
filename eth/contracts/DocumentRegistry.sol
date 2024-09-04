// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.24;

// Uncomment this line to use console.log
// import "hardhat/console.sol";

contract DocumentRegistry {
    address public owner;
    mapping(address => bool) public issuers;
    mapping(string => string) public documents; // document id from postgres mapped to document hash

    constructor() {
        owner = msg.sender;
    }

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this function");
        _;
    }

    modifier onlyIssuer() {
        require(issuers[msg.sender], "Only issuer can call this function");
        _;
    }

    function addIssuer(address _issuer) public onlyOwner {
        issuers[_issuer] = true;
    }

    function removeIssuer(address _issuer) public onlyOwner {
        issuers[_issuer] = false;
    }

    function addDocument(string memory _documentId, string memory _documentHash) public onlyIssuer {
        documents[_documentId] = _documentHash;
    }
}
