pragma solidity ^0.5.0;


contract contractEvents {

    event MyEvent(address indexed from, address indexed to, string value, bytes indexed valuesInBytes, bytes signature);

    function createEvent(string memory value) public {
        emit MyEvent(address(0x398484838), address(0x939585883), value, bytes(value), bytes(value));
    }
}
