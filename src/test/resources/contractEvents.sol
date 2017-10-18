pragma solidity ^0.4.7;


contract contractEvents {

    event MyEvent(address indexed from, address indexed to, string value, bytes indexed valuesInBytes);

    function createEvent(string value) public {
        MyEvent(0x398484838, 0x939585883, value, bytes(value));
    }
}
