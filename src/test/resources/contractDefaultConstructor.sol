pragma solidity ^0.7.0;
// SPDX-License-Identifier: MIT

contract ContractDefaultConstructor {

    string public value;

    constructor () {
        value = "hello world";
    }
}
