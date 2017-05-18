pragma solidity ^0.4.7;


contract ContractConstructor {

    string public value;

    function ContractConstructor(string _value) {
        value = _value;
    }
}
