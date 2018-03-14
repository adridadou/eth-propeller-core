pragma solidity ^0.4.7;


contract contractEvents {

    event MyEvent(address indexed from, address indexed to, string value, bytes indexed valuesInBytes, bytes signature);

    function createEvent(string value) public {
        emit MyEvent(0x398484838, 0x939585883, value, bytes(value), bytes(value));
    }
}
