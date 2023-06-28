package io.bonitoo.qa.plugin.util;

import io.bonitoo.qa.conf.mqtt.broker.TLSConfig;
import io.bonitoo.qa.util.EncryptPass;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
  public void verifyEncryption() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException {

    final String testPass1 = "changeit";
    final String testHash = "ENCAAAAEAcyP+R4LI+ivazT1rDAtz0aRzNQyM8P99rgBA7SElWb";

    System.out.println("DEBUG testPass1 " + testPass1);

    String passHash = EncryptPass.encryptTrustPass(
      this.getClass().getPackage().getName(),
      this.getClass().getSimpleName(), testPass1.toCharArray());

    System.out.println("DEBUG pass " + passHash);

    // char[] decrypted = TLSConfig.decryptTrustPass(this.getClass().getPackage().getName(), pass);
    char [] password = EncryptPass.decryptTrustPass(
      this.getClass().getPackage().getName(),
      this.getClass().getSimpleName(),
      passHash
    );

    System.out.println("DEBUG password " + new String(password));
    assertEquals(testPass1, new String(password));

    char[] testHashPass = EncryptPass.decryptTrustPass(
      this.getClass().getPackage().getName(),
      this.getClass().getSimpleName(),
      testHash
    );

    System.out.println("DEBUG testHashPass " + new String(testHashPass));
    assertEquals(testPass1, new String(testHashPass));

  }

  // for verifying results of EncryptPass.main.
  // Copy and paste results to hashedPass variable below...
  // to be used in dev environment ONLY
 /* @Test
  public void decrypt() {
    final String hashedPass = "ENCAAAAEPPs74IFMIGBkjtPwUND0fSJTIBiFm0J8YHuPNbYYS67";
    try {
      char[] pass = io.bonitoo.qa.util.EncryptPass.decryptTrustPass(TLSConfig.class.getPackage().getName(),
        TLSConfig.class.getSimpleName(), hashedPass);
      System.out.println("result:\n" + new String(pass));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
             | InvalidAlgorithmParameterException | InvalidKeyException
             | IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  } */

}
