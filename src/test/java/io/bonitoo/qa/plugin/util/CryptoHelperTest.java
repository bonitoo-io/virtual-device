package io.bonitoo.qa.plugin.util;

import io.bonitoo.qa.util.CryptoHelper;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CryptoHelperTest {

  static String TEST_PBE_PASS = "foobar";
  static String TEST_PROPERTY = "superPassword";
  static byte[] TEST_SALT = "0123456789AB".getBytes();

  static int TEST_ITER_COUNT = 500;

  static int TEST_KEY_LENGTH = 128;

  /*
  @Test
  public void encryptDecrypt() throws GeneralSecurityException, IOException {

    System.out.println("DEBUG default property " + TEST_PROPERTY);

    SecretKeySpec keySpec = KeyHelper.createSecretKey(
      KeyHelperTest.class.getPackage().getName().toCharArray(),
      KeyHelperTest.class.getSimpleName().getBytes(),
      TEST_ITER_COUNT,
      TEST_KEY_LENGTH
    );

    String encPass = KeyHelper.encrypt(TEST_PROPERTY, keySpec);

    System.out.println("DEBUG encPass " + encPass);

    String decPass = KeyHelper.decrypt(encPass, keySpec);

    System.out.println("DEBUG decPass " + decPass);

  }*/

  @Test
  public void encryptDecryptAlt()
    throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
    UnsupportedEncodingException, IllegalBlockSizeException, InvalidParameterSpecException,
    BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

    System.out.println("DEBUG default property " + TEST_PROPERTY);

    SecretKeySpec keySpec = CryptoHelper.createSecretKey(
      CryptoHelperTest.class.getPackage().getName().toCharArray(), // PBE key from package
      CryptoHelperTest.class.getSimpleName().getBytes(), // Salt from class name
      TEST_ITER_COUNT,
      TEST_KEY_LENGTH
    );

    String encPass = CryptoHelper.encrypt(TEST_PROPERTY.toCharArray(), keySpec);

    System.out.println("DEBUG encPass " + encPass);

    char[] decProperty = CryptoHelper.decrypt(encPass, keySpec);

    System.out.println("DEBUG decProperty " + new String(decProperty));

    assertEquals(TEST_PROPERTY, new String(decProperty));

  }

}
