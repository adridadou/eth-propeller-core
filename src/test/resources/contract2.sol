pragma solidity ^0.4.6;

contract myContract2 {
    address owner;
    function getI1() public pure returns (string) {return "hello";}
    function getT() public pure returns (bool) {return true;}
    function getM() public pure returns (bool,string,uint) {return (true,"hello",34);}
    function getArray() public pure returns (uint[10] arr) {
        for(uint i = 0; i < 10; i++) {
            arr[i] = i;
        }
    }

    function getArray2() public pure returns (uint[10] arr) {
        for(uint i = 0; i < 10; i++) {
            arr[i] = i;
        }
    }

    function getOwner() public view returns (address) {
        return owner;
    }
}