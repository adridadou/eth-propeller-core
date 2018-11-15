pragma solidity ^0.5.0;

contract myContract2 {
    string public i1;
    string public i2;
    address public owner;

    constructor() public {
        owner = msg.sender;
    }

    function  myMethod(string memory value) public returns (uint) {
        i1 = value;
        return 12;
    }

    function  myMethod2(string memory value) public returns (bool success) {
        i2 = value;
        return true;
    }

    function  myMethod3(string memory value) public payable returns (bool success) {
        i2 = value;
        return true;
    }

    function  getEnumValue() public pure returns (uint) {return 1;}
    function  getI1() public view returns (string memory) {return i1;}
    function  getI2() public view returns (string memory) {return i2;}
    function  getT() public pure returns (bool) {return true;}
    function  getM() public pure returns (bool,string memory,uint) {return (true,"hello",34);}
    function  getOwner() public view returns (address) {return owner;}
    function  getArray() public pure returns (uint[10] memory arr) {
        for(uint i = 0; i < 10; i++) {
            arr[i] = i;
        }
    }

    function  getSet() public pure returns (uint[10] memory arr) {
        for(uint i = 0; i < 10; i++) {
            arr[i] = i;
        }
    }

    function throwMe() public pure {
        revert();
    }

    function  getInitTime(uint time) public pure returns(uint) {
        return time;
    }

    function  getAccountAddress(address addr) public pure returns (address) {
        return addr;
    }
}