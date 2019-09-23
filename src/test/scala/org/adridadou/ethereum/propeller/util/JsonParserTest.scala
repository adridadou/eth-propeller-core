package org.adridadou.ethereum.propeller.util

import org.adridadou.ethereum.propeller.solidity.abi.AbiEntry
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.check.Checkers

class JsonParserTest extends FlatSpec with Matchers with Checkers {
	"Json parser" should "ignore unknown parameters" in {
		val json =
			"""[
				|	{
				|		"constant": true,
				|		"inputs": [],
				|		"name": "getName",
				|		"outputs": [
				|			{
				|				"internalType": "string",
				|				"name": "",
				|				"type": "string"
				|			}
				|		],
				|		"payable": false,
				|		"stateMutability": "view",
				|		"type": "function"
				|	},
				|	{
				|		"constant": false,
				|		"inputs": [
				|			{
				|				"internalType": "string",
				|				"name": "_name",
				|				"type": "string"
				|			}
				|		],
				|		"name": "recordTest",
				|		"outputs": [],
				|		"payable": false,
				|		"stateMutability": "nonpayable",
				|		"type": "function"
				|	}
				|]""".stripMargin

		AbiEntry.parse(json)
	}
}

