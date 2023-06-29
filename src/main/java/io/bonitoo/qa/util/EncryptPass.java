package io.bonitoo.qa.util;

import io.bonitoo.qa.conf.mqtt.broker.TlsConfig;

import java.lang.invoke.MethodHandles;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptPass {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String DEFAULT_PBE_PASS = TlsConfig.class.getPackage().getName();

  private static String ENCODE_HEADER = "ENC";

  public static void main(String[] args) {

    char[] password = System.console().readPassword("Enter truststore password: ");
    String hashedPass = encryptTrustPass(DEFAULT_PBE_PASS, password);

    System.out.println("result:\n" + hashedPass);
  }

  public static String encryptTrustPass(String encPass, char[] trustPass) {

    if (passIsEncoded(trustPass)) {
      logger.info(LogHelper.buildMsg("0000", "Encrypt Password", "Pass is already encrypted. Nothing to do."));
      return new String(trustPass);
    }

    String base64 = CryptoHelper.encrypt(trustPass, encPass.toCharArray());

    return String.format("%s%s", ENCODE_HEADER, base64);
  }

  public static char[] decryptTrustPass(String encPass, String trustHash) {
    if (!passIsEncoded(trustHash.toCharArray())) {
      logger.info(LogHelper.buildMsg("0000", "Decrypt Password", "password not encoded.  Nothing to do."));
      return trustHash.toCharArray();
    }

    String base64 = trustHash.substring(ENCODE_HEADER.length());

    return CryptoHelper.decrypt(base64, encPass.toCharArray());
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
