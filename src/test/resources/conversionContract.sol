pragma solidity ^0.4.10;


contract myContract {
    function strFunc(string str) returns (string) {
        return str;
    }

    function intFunc(uint uintValue) returns (uint) {
        return uintValue;
    }

    function addressFunc(address addr) returns (address) {
        return addr;
    }

    function dateFunc(uint dateValue) returns (uint) {
        return dateValue;
    }

    function boolFunc(bool boolValue) returns (bool) {
        return boolValue;
    }
}
