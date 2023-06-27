package io.bonitoo.qa.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A utility class to simplify
 */
public class CryptoHelper {

  public static final int DEFAULT_ITERATIONS = 1000;

  public static final int DEFAULT_KEY_LENGTH = 128;

  public static byte[] genSalt(int length) throws NoSuchAlgorithmException {
    byte[] salt = new byte[length];
    SecureRandom drbg = SecureRandom.getInstance("DRBG");
    drbg.nextBytes(salt);
    return salt;
  }

  public static SecretKeySpec createSecretKey(char[] password,
                                               byte[] salt,
                                               int iterationCount,
                                               int keyLength)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    System.out.println("DEBUG createSecretKey salt " + Base64.getEncoder().encodeToString(salt));
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
    PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
    SecretKey keyTmp = keyFactory.generateSecret(keySpec);
    return new SecretKeySpec(keyTmp.getEncoded(), "AES");
  }

  public static String encrypt(char[] property, SecretKeySpec keySpec)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
      InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException {
    Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    pbeCipher.init(Cipher.ENCRYPT_MODE, keySpec);
    AlgorithmParameters parameters = pbeCipher.getParameters();
    IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);

    // encrypt the property data
    byte[] cryptoData = pbeCipher.doFinal(charsToBytes(property));
    byte[] iv = ivParameterSpec.getIV();
    byte[] ivLength = ByteBuffer.allocate(Integer.BYTES).putInt(iv.length).array();

    // package to contain
    //  1. length of initialization vector (16 or 128) = Integer.BYTES (should be 4)
    //  2. the initialization vector (should be 16 or 128 bytes)
    //  3. the encrypted data

    ByteBuffer packageBuffer = ByteBuffer.allocate(ivLength.length + iv.length + cryptoData.length);
    packageBuffer.put(ivLength);
    packageBuffer.put(iv);
    packageBuffer.put(cryptoData);

    // Now encode to Base64 string
    return Base64.getEncoder().encodeToString(packageBuffer.array());
  }

  public static char[] decrypt(String string, SecretKeySpec key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
      InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    byte[] packageArray = Base64.getDecoder().decode(string);

    // Unpack the package - see comments in encrypt above
    byte[] ivLengthHolder = Arrays.copyOfRange(packageArray, 0, Integer.BYTES);
    ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES).put(ivLengthHolder);
    lengthBuffer.rewind();
    int ivLength = lengthBuffer.getInt();
    byte[] iv = Arrays.copyOfRange(packageArray, Integer.BYTES, ivLength + Integer.BYTES);
    byte[] cryptoData = Arrays.copyOfRange(packageArray, Integer.BYTES + iv.length, packageArray.length);

    // With Initialization Vector (iv) and encrypted data in hand, try and decrypt
    Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
    return bytesToChars(pbeCipher.doFinal(cryptoData));
  }

  public static byte[] charsToBytes(char[] chars) {
    CharBuffer charBuffer = CharBuffer.wrap(chars);
    ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
    byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
    Arrays.fill(byteBuffer.array(), (byte) 0);
    return bytes;
  }

  public static char[] bytesToChars(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
    return charBuffer.array();
  }

}
