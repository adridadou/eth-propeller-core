pragma solidity ^0.7.0;

// SPDX-License-Identifier: MIT

contract contractEvents {

    event MyEvent(address indexed from, address indexed to, string value, bytes indexed valuesInBytes, bytes signature);

    function createEvent(string memory value) public {
        emit MyEvent(address(0x398484838), address(0x939585883), value, bytes(value), bytes(value));
    }
}
