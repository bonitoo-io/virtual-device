package io.bonitoo.qa.util;

import io.bonitoo.qa.conf.mqtt.broker.TLSConfig;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptPass {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String DEFAULT_PBE_PASS = TLSConfig.class.getPackage().getName();
  private static final String DEFAULT_PBE_SALT = TLSConfig.class.getSimpleName();

  private static String ENCODE_HEADER = "ENC";

  public static void main(String[] args) {

    char[] password = System.console().readPassword("Enter truststore password: ");
    try {
      String hashedPass = encryptTrustPass(DEFAULT_PBE_PASS, DEFAULT_PBE_SALT, password);

      System.out.println("result:\n" + hashedPass);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
             | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException
             | BadPaddingException | UnsupportedEncodingException
             | InvalidParameterSpecException e) {
      throw new RuntimeException(e);
    }

  }

  public static String encryptTrustPass(String encPass, String encSalt, char[] trustPass) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidParameterSpecException {

    if (passIsEncoded(trustPass)) {
      logger.info(LogHelper.buildMsg("0000", "Parse TLS Config", "Pass is already encrypted. Nothing to do."));
      return new String(trustPass);
    }

    // 1. use pbe encryption to encrypt - for now use package name as key
    // System.out.println("DEBUG pbeKey " + pbeKey);
    SecretKeySpec keySpec = CryptoHelper.createSecretKey(
      encPass.toCharArray(),
      encSalt.getBytes(),
      CryptoHelper.DEFAULT_ITERATIONS,
      CryptoHelper.DEFAULT_KEY_LENGTH
    );

    String base64 = CryptoHelper.encrypt(trustPass, keySpec);

    // 2. convert encrypted value to base64
    // 3. prepend ENCODE_HEADER

    return String.format("%s%s", ENCODE_HEADER, base64);
  }

  public static char[] decryptTrustPass(String encPass, String encSalt, String trustHash) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    if (!passIsEncoded(trustHash.toCharArray())) {
      logger.info(LogHelper.buildMsg("0000", "Parse TLS Config", "password not encoded.  Nothing to do."));
      return trustHash.toCharArray();
    }

    System.out.print("\nDECRYPT\n");

    SecretKeySpec keySpec = CryptoHelper.createSecretKey(
        encPass.toCharArray(),
        encSalt.getBytes(),
        CryptoHelper.DEFAULT_ITERATIONS,
        CryptoHelper.DEFAULT_KEY_LENGTH
    );

    String base64 = trustHash.substring(ENCODE_HEADER.length());

    System.out.println("DEBUG base64 " + base64);

    return CryptoHelper.decrypt(base64, keySpec);
  }


  public static boolean passIsEncoded(char[] trustHash) {

    final Pattern base64Pattern = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");

    String trustHashString = new String(trustHash);
    if (!trustHashString.startsWith(ENCODE_HEADER)) {
      return false;
    }

    return base64Pattern.matcher(trustHashString.substring(ENCODE_HEADER.length())).find();

  }



}
