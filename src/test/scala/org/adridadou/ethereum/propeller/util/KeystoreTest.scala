package org.adridadou.ethereum.propeller.util

import java.io.File
import java.math.BigInteger

import org.adridadou.ethereum.propeller.keystore.Keystore
import org.junit.Assert.assertEquals
import org.scalatest.check.Checkers
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by davidroon on 09.04.17.
  * This code is released under Apache 2 license
  */
class KeystoreTest extends FlatSpec with Matchers with Checkers {

  "Keystore" should "be able to read the keystore file and decode it" in {
    val ecKey = Keystore.fromKeystore(new File("src/test/resources/keystore.json"), "testpassword")
    assertEquals(new BigInteger("55254095649631781209224057814590225966912998986153936485890744796566334537373"), ecKey.getBigIntPrivateKey)
  }
}
