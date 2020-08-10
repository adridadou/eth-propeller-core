pragma solidity ^0.7.0;
// SPDX-License-Identifier: MIT

contract ContractConstructor {

    string public value;

    constructor (string memory _value) {
        value = _value;
    }
}
