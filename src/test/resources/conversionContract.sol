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

    function dateFunc(int dateValue) returns (int) {
        return dateValue;
    }

    function boolFunc(bool boolValue) returns (bool) {
        return boolValue;
    }

    function arrayFunc(int[10] value) returns (int[10]) {
        return value;
    }

    function dynArrayFunc(int[] value) returns (int) {
        if (value.length > 3) return value[3];
        return 0;
    }

    function mixWithStringFunc(int test1, string test2, bool test3, string test4) returns (string) {
        if (test1 > 0 && test3 && bytes(test2).length > 23) {

        }
        return test4;
    }

    function bytes32Func(bytes32 value) returns (bytes32) {
        return value;
    }

    function bytesFunc(bytes value) returns (bytes) {
        return value;
    }

    function mixStringAddressFunc(string str, address addr) returns (string) {
        if (addr == 0x0) {

        }
        return str;
    }
}
