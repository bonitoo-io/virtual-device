package io.bonitoo.qa.util;

import io.bonitoo.qa.conf.mqtt.broker.TlsConfig;
import java.lang.invoke.MethodHandles;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for encrypting and decrypting passwords.
 */
public class EncryptPass {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String ENCODE_HEADER = "ENC";

  /**
   * Helper util for generating passwords.
   *
   * @param args - ignored.
   */
  public static void main(String[] args) {

    char[] password = System.console().readPassword("Enter truststore password: ");
    String hashedPass = encryptTrustPass(
        TlsConfig.class.getPackage().getName().toCharArray(),
        password
    );

    System.out.println("result:\n" + hashedPass);
  }

  /**
   * Method for encrypting passwords.
   *
   * @param encPass - password used for encryption algorithm.
   * @param trustPass - password to be encrypted.
   * @return - a base64 encrypted string.
   */
  public static String encryptTrustPass(char[] encPass, char[] trustPass) {

    if (passIsEncoded(trustPass)) {
      logger.info(LogHelper.buildMsg(
          "0000",
          "Encrypt Password",
          "Pass is already encrypted. Nothing to do.")
      );
      return new String(trustPass);
    }

    String base64 = CryptoHelper.encrypt(trustPass, encPass);

    return String.format("%s%s", ENCODE_HEADER, base64);
  }

  /**
   * Decrypts the hashed property into a char[].
   *
   * @param encPass - password used during encryption.
   * @param trustHash - value to be decrypted.
   * @return - the decrypted value.
   */
  public static char[] decryptTrustPass(char[] encPass, String trustHash) {
    if (!passIsEncoded(trustHash.toCharArray())) {
      logger.info(LogHelper.buildMsg(
          "0000",
          "Decrypt Password",
          "password not encoded.  Nothing to do.")
      );
      return trustHash.toCharArray();
    }

    String base64 = trustHash.substring(ENCODE_HEADER.length());

    return CryptoHelper.decrypt(base64, encPass);
  }


  /**
   * Tests whether the trustHash is actually base64 and matches patterns for encrypted properties.
   *
   * @param trustHash - hashed property to be evaluated.
   * @return - whether is hash or not.
   */
  public static boolean passIsEncoded(char[] trustHash) {

    final Pattern base64Pattern = Pattern
        .compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");

    String trustHashString = new String(trustHash);
    if (!trustHashString.startsWith(ENCODE_HEADER)) {
      return false;
    }

    return base64Pattern.matcher(trustHashString.substring(ENCODE_HEADER.length())).find();

  }



}
