package io.bonitoo.qa.plugin.util;

import io.bonitoo.qa.util.CryptoHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CryptoHelperTest {

  @Test
  public void encryptDecrypt(){

    String sampleEncPass = "yDrq7WbATHUPeemxmFM5KllxutMjw+gweSW8flkK/TsAAAAQ5UEQ7SX2NH9Ee/mS+acf1+SazFImMdBYKpXOcPre34I=";
    String source = "helloWorld";

    String encPass = CryptoHelper.encrypt(source.toCharArray(), "changeit".toCharArray());

    char[] decProperty = CryptoHelper.decrypt(encPass, "changeit".toCharArray());

    assertEquals(source, new String(decProperty));

   char[] decSample = CryptoHelper.decrypt(sampleEncPass, "changeit".toCharArray());

   assertEquals(source, new String(decSample));

  }

}
