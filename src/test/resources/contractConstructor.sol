pragma solidity ^0.4.7;


contract ContractConstructor {

    string public value;

    constructor (string _value) public {
        value = _value;
    }
}
