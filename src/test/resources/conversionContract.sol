pragma solidity ^0.7.0;
// SPDX-License-Identifier: MIT

contract myContract {
    function strFunc(string memory str)  public pure returns(string memory) {
        return str;
    }

    function uintFunc(uint uintValue)  public  pure returns (uint) {
        return uintValue;
    }

    function smallUintFunc(uint8 uintValue) public pure returns (uint8) {
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

    function arrayFunc(int[10] memory value)  public pure  returns (int[10] memory) {
        return value;
    }

    function dynArrayFunc(int[] memory value) public  pure  returns (int) {
        if (value.length > 3) return value[3];
        return 0;
    }

    function mixWithStringFunc(int test1, string memory test2, bool test3, string memory test4) public pure  returns (string memory) {
        if (test1 > 0 && test3 && bytes(test2).length > 23) {

        }
        return test4;
    }

    function complexReturnType(int test1, string memory test2, bool test3, string memory test4)  public  pure returns (int, string memory, bool, string memory) {
        return (test1, test2, test3, test4);
    }

    function bytes32Func(bytes32 value) public pure returns (bytes32) {
        return value;
    }

    function bytesFunc(bytes memory value) public pure returns (bytes memory) {
        return value;
    }

    function signatureFunc(bytes memory value) public pure returns (bytes memory) {
        return value;
    }

    function mixStringAddressFunc(string memory str, address addr) public pure returns (string memory) {
        if (addr == address(0x0)) {

        }
        return str;
    }

    function recoverSimple(bytes32 hash, uint8 v, uint r, uint s) public pure returns (address) {
        // Note: this only verifies that signer is correct.
        // You'll also need to verify that the hash of the data
        // is also correct.
        return ecrecover(hash, v, bytes32(r), bytes32(s));
    }

    function lstFunc(uint[] memory arr) public pure returns (uint[] memory) {
        return arr;
    }

    function recover(bytes32 hash, bytes memory sig) public pure returns (address) {
        bytes32 r;
        bytes32 s;
        uint8 v;

        //Check the signature length
        if (sig.length != 65) {
            return address(0x0);
        }

        // Divide the signature in r, s and v variables
        assembly {
        r := mload(add(sig, 32))
        s := mload(add(sig, 64))
        v := byte(0, mload(add(sig, 96)))
        }

        // Version of signature should be 27 or 28, but 0 and 1 are also possible versions
        if (v < 27) {
            v += 27;
        }

        // If the version is correct return the signer address
        if (v != 27 && v != 28) {
            return address(0x0);
        }
        else {
            return ecrecover(hash, v, r, s);
        }
    }
}
