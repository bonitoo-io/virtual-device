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
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * A utility class to simplify key processing and property encryption.
 */
public class CryptoHelper {

  public static final int DEFAULT_ITERATIONS = 1000;

  public static final int DEFAULT_KEY_LENGTH = 128;

  public static final int DEFAULT_SALT_SIZE = 32;

  public static final String DEFAULT_TRANSFORM = "AES/CBC/PKCS5Padding";

  @NoArgsConstructor
  @AllArgsConstructor
  static class Package {

    byte[] salt;
    byte[] ivLength;

    byte[] iv;

    byte[] data;

    byte[] pack() {
      ByteBuffer packageBuffer = ByteBuffer.allocate(salt.length
          + ivLength.length
          + iv.length
          + data.length);
      packageBuffer.put(salt);
      packageBuffer.put(ivLength);
      packageBuffer.put(iv);
      packageBuffer.put(data);

      return packageBuffer.array();
    }

    void unpack(byte[] bytes) {
      this.salt = Arrays.copyOfRange(bytes, 0, DEFAULT_SALT_SIZE);
      this.ivLength = Arrays.copyOfRange(bytes,
          DEFAULT_SALT_SIZE,
          DEFAULT_SALT_SIZE + Integer.BYTES);
      ByteBuffer buffer = ByteBuffer.allocate(this.ivLength.length).put(this.ivLength);
      buffer.rewind();
      int length = buffer.getInt();
      this.iv = Arrays.copyOfRange(bytes,
        DEFAULT_SALT_SIZE + Integer.BYTES,
        DEFAULT_SALT_SIZE + Integer.BYTES + length);
      this.data = Arrays.copyOfRange(bytes,
        DEFAULT_SALT_SIZE + Integer.BYTES + length,
        bytes.length);
    }

  }

  /**
   * Generate a random salt of given length.
   *
   * @param length - length of byte array for salt values.
   * @return - the array
   * @throws NoSuchAlgorithmException -
   */
  public static byte[] genSalt(int length) throws NoSuchAlgorithmException {
    // TODO - currently default salt is based on local "constants"
    //        review using this and packaging salt into final hash
    byte[] salt = new byte[length];
    SecureRandom drbg = SecureRandom.getInstance("DRBG");
    drbg.nextBytes(salt);
    return salt;
  }

  /**
   * Generates a KeySpec later used in encryption and decryption.
   *
   * @param password - used as bases for encryption key
   * @param salt - size of set of generated potential keys to select from
   * @param iterationCount - number of times to iterate over encryption
   * @param keyLength - length of the key
   * @return - a SecretKeySpec
   * @throws NoSuchAlgorithmException -
   * @throws InvalidKeySpecException -
   */
  public static SecretKeySpec createSecretKey(char[] password,
                                               byte[] salt,
                                               int iterationCount,
                                               int keyLength)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
    PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
    SecretKey keyTmp = keyFactory.generateSecret(keySpec);
    return new SecretKeySpec(keyTmp.getEncoded(), "AES");
  }

  /**
   * Helper method to change char[] to byte[].
   *
   * @param chars - chars to be converted.
   * @return - the new byte[].
   */
  public static byte[] charsToBytes(char[] chars) {
    CharBuffer charBuffer = CharBuffer.wrap(chars);
    ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
    byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
        byteBuffer.position(),
        byteBuffer.limit());
    Arrays.fill(byteBuffer.array(), (byte) 0);
    return bytes;
  }

  /**
   * Helper method to convert byte[] to char[].
   *
   * @param bytes - the byte[] to be converted.
   * @return - the new char[].
   */
  public static char[] bytesToChars(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
    return charBuffer.array();
  }

  /**
   * Encrypt a property using password encryption.
   *
   * @param property - the property to be encrypted.
   * @param password - the password used for creating the encrypted value.
   * @return - a Base64 representation of the encrypted property.
   */
  public static String encrypt(char[] property, char[] password) {
    try {
      byte[] salt = genSalt(DEFAULT_SALT_SIZE);
      SecretKeySpec keySpec = createSecretKey(password,
          salt,
          DEFAULT_ITERATIONS,
          DEFAULT_KEY_LENGTH);

      Cipher pbeCipher = Cipher.getInstance(DEFAULT_TRANSFORM);
      pbeCipher.init(Cipher.ENCRYPT_MODE, keySpec);
      AlgorithmParameters parameters = pbeCipher.getParameters();
      IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
      // encrypt the property data
      byte[] cryptoData = pbeCipher.doFinal(charsToBytes(property));
      byte[] iv = ivParameterSpec.getIV();
      byte[] ivLength = ByteBuffer.allocate(Integer.BYTES).putInt(iv.length).array();

      Package p = new Package(salt, ivLength, iv, cryptoData);

      // Now encode to Base64 string
      return Base64.getEncoder().encodeToString(p.pack());

    } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
             | InvalidParameterSpecException | InvalidKeyException | IllegalBlockSizeException
             | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Decrypts a Base64 hashString containing an encrypted value using password based encryption.
   *
   * @param hashString - the Base64 hashString to be decrypted.
   * @param password - the password used during encryption.
   * @return - the decrypted property as a char[].
   */
  public static char[] decrypt(String hashString, char[] password) {
    byte[] packageArray = Base64.getDecoder().decode(hashString);
    Package p = new Package();
    p.unpack(packageArray);

    try {
      SecretKeySpec keySpec = createSecretKey(password,
          p.salt,
          DEFAULT_ITERATIONS,
          DEFAULT_KEY_LENGTH);

      Cipher pbeCipher = Cipher.getInstance(DEFAULT_TRANSFORM);
      pbeCipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(p.iv));

      return bytesToChars(pbeCipher.doFinal(p.data));

    } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
             | InvalidAlgorithmParameterException | InvalidKeyException
             | IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }

  }

}
