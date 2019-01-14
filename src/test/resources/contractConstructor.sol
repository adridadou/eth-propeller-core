pragma solidity ^0.5.0;


contract ContractConstructor {

    string public value;

    constructor (string memory _value) public {
        value = _value;
    }
}
