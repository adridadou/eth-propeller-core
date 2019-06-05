pragma solidity ^0.5.0;


contract ContractDefaultConstructor {

    string public value;

    constructor () public {
        value = "hello world";
    }
}
