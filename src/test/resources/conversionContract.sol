pragma solidity ^0.4.10;


contract myContract {
    function strFunc(string str)  public pure returns(string) {
        return str;
    }

    function uintFunc(uint uintValue)  public  pure returns (uint) {
        return uintValue;
    }

    function intFunc(int intValue) public  pure  returns (int) {
        return intValue;
    }

    function addressFunc(address addr)  public pure  returns (address) {
        return addr;
    }

    function addressPayableFunc(address addr)  public payable returns (address) {
        return addr;
    }

    function dateFunc(int dateValue)  public  pure returns (int) {
        return dateValue;
    }

    function boolFunc(bool boolValue) public  pure  returns (bool) {
        return boolValue;
    }

    function arrayFunc(int[10] value)  public pure  returns (int[10]) {
        return value;
    }

    function dynArrayFunc(int[] value) public  pure  returns (int) {
        if (value.length > 3) return value[3];
        return 0;
    }

    function mixWithStringFunc(int test1, string test2, bool test3, string test4) public pure  returns (string) {
        if (test1 > 0 && test3 && bytes(test2).length > 23) {

        }
        return test4;
    }

    function complexReturnType(int test1, string test2, bool test3, string test4)  public  pure returns (int, string, bool, string) {
        return (test1, test2, test3, test4);
    }

    function bytes32Func(bytes32 value)  public  pure returns (bytes32) {
        return value;
    }

    function bytesFunc(bytes value) public   pure returns (bytes) {
        return value;
    }

    function mixStringAddressFunc(string str, address addr)  public pure  returns (string) {
        if (addr == 0x0) {

        }
        return str;
    }
}
