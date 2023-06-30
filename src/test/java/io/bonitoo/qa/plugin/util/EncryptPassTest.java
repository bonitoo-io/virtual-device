package io.bonitoo.qa.plugin.util;

import io.bonitoo.qa.util.EncryptPass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class EncryptPassTest {

  @Test
  public void checkTLSEncodedPassword(){

    assertTrue(EncryptPass.passIsEncoded("ENCABCD".toCharArray()));
    assertTrue(EncryptPass.passIsEncoded("ENCABCD123=".toCharArray()));
    assertTrue(EncryptPass.passIsEncoded("ENCAbcd1234ef==".toCharArray()));
    assertFalse(EncryptPass.passIsEncoded("ABCD".toCharArray()));
    assertFalse(EncryptPass.passIsEncoded("ENCA12".toCharArray()));

  }

  @Test
  public void verifyEncryptionRandomSalt(){
    final String testPass1 = "changeit";
    final String preHash = "ENCRJiU44WpvE/gP3lKWAdY5kcAFfRCjAsmYEE821eN5HoAAAAQpRThOlweXkMG2ieDtiTXf9sjy4o1FRgi0JCi44gOHDI=";

    String testHash1 = EncryptPass.encryptTrustPass(
      this.getClass().getPackage().getName().toCharArray(),
      testPass1.toCharArray()
    );

    char[] unHashed1 = EncryptPass.decryptTrustPass(
      this.getClass().getPackage().getName().toCharArray(),
      testHash1
    );

    assertEquals(testPass1, new String(unHashed1));

    char[] preHashResult = EncryptPass.decryptTrustPass(
      this.getClass().getPackage().getName().toCharArray(),
      preHash
    );

    assertEquals(testPass1, new String(preHashResult));
  }

  // Do not remove - leave commented
  // for verifying results of EncryptPass.main.
  // Copy and paste results to hashedPass variable below...
  // to be used in dev environment ONLY
  /* @Test
  public void decrypt() {
    final String hashedPass = "ENCqTJZQarWDANjbiKQRH1R5/Dw3jNtSIYq12fIt67sIPEAAAAQmi6eCz/B3DynfmBHkC30s9n9/ynDhlcNo2yDA7ma90k=";

    char[] pass = io.bonitoo.qa.util.EncryptPass.decryptTrustPass(TLSConfig.class.getPackage().getName(),
        hashedPass);
    System.out.println("result:\n" + new String(pass));

  } */

}
