# Ethereum Propeller

[![CircleCI](https://circleci.com/gh/adridadou/eth-contract-api/tree/develop.svg?style=svg)](https://circleci.com/gh/adridadou/eth-propeller-core/tree/develop)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f625f0b84618480d84f90ae4a3ff8536)](https://www.codacy.com/app/Adridadou/eth-propeller-core?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=adridadou/eth-propeller-core&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/f625f0b84618480d84f90ae4a3ff8536)](https://www.codacy.com/app/Adridadou/eth-propeller-core?utm_source=github.com&utm_medium=referral&utm_content=adridadou/eth-propeller-core&utm_campaign=Badge_Coverage)

Ethereum Framework for JVM developers

#Introduction
The name Ethereum Propeller comes from the idea that this framework should give your project the push it needs to reach success.

The goal of this project is to give JVM developers the tools to build applications on top of Ethereum.

The main concepts behind Ethereum propeller are:

* Ethereum abstraction & easy integration. It should feel natural to work with ethereum and especially to work with smart contracts. The code should never be written for a particular backend, it should work regardless of your application working with RPC or an embedded client.
* Ethereum testing. Testing is crucial for smart contracts. Any bug can be fatal and that's why writing tests should be easy
* Type safety. Types are here to help developers write better code and can be used to sanitize inputs and document interfaces. 

This is the core library for Ethereum Propeller.
You still need to decide which backend you want to use. There are two possibilities right now:
* If you want to use Ethereumj with a embedded full node, go to eth-propeller-ethj
* If you want to use web3j to connect to a remote RPC API, go to eth-propeller-core

#getting started

**Add the dependency**
```
<dependency>
    <groupId>org.adridadou</groupId>
    <artifactId>eth-propeller-{ethj or core}</artifactId>
    <version>{latest version}</version>
</dependency>
```

Simply add the backend you want to use as a dependency. Right you, have the choice between ethereumj (ethj) and web3j

**Create an EthereumFacade**
Each backend has its own EthereumFacadeProvider
- RpcEthereumFacadeProvider for web3j
- EthjEthereumFacadeProvider for ethereumj

**Quick introduction to EthereumFacade**
EthereumFacade is your interface to Ethereum. This is how you interact with it:
- Compile & deploy a smart contract
- Send ether
- Listen to Ethereum events (new block, new transactions)
- Listen to Smart contract events
- Interface with a smart contract


#Testing
In order to run unit tests on a mocked Ethereum network, please use eth-propeller-ethj and create a test


**Note**: Ethereumj's license is LPGL3. So make sure that your license is compatible with LPGL3 if you want to use ethereumJ for something else than testing
