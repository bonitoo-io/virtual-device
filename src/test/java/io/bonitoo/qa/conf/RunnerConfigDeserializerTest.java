package io.bonitoo.qa.conf;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
public class RunnerConfigDeserializerTest {

  @Test
  public void parseModeBlockingTest(){
    assertEquals(Mode.BLOCKING, RunnerConfigDeserializer.parseMode("blocking"));
    assertEquals(Mode.BLOCKING, RunnerConfigDeserializer.parseMode("Blocking"));
    assertEquals(Mode.BLOCKING, RunnerConfigDeserializer.parseMode("BLOCKING"));
    assertEquals(Mode.BLOCKING, RunnerConfigDeserializer.parseMode("block"));
    assertEquals(Mode.BLOCKING, RunnerConfigDeserializer.parseMode("Block"));
    assertEquals(Mode.BLOCKING, RunnerConfigDeserializer.parseMode("BLOCK"));
  }

  @Test
  public void parseModeReactiveTest(){
    assertEquals(Mode.REACTIVE, RunnerConfigDeserializer.parseMode("reactive"));
    assertEquals(Mode.REACTIVE, RunnerConfigDeserializer.parseMode("Reactive"));
    assertEquals(Mode.REACTIVE, RunnerConfigDeserializer.parseMode("REACTIVE"));
    assertEquals(Mode.REACTIVE, RunnerConfigDeserializer.parseMode("reactivex"));
    assertEquals(Mode.REACTIVE, RunnerConfigDeserializer.parseMode("Reactivex"));
    assertEquals(Mode.REACTIVE, RunnerConfigDeserializer.parseMode("REACTIVEX"));
    assertEquals(Mode.REACTIVE, RunnerConfigDeserializer.parseMode("rx"));
    assertEquals(Mode.REACTIVE, RunnerConfigDeserializer.parseMode("Rx"));
    assertEquals(Mode.REACTIVE, RunnerConfigDeserializer.parseMode("RX"));
  }

  @Test
  public void parseModeAsyncTest(){
    assertEquals(Mode.ASYNC, RunnerConfigDeserializer.parseMode("asynchronous"));
    assertEquals(Mode.ASYNC, RunnerConfigDeserializer.parseMode("Asynchronous"));
    assertEquals(Mode.ASYNC, RunnerConfigDeserializer.parseMode("ASYNCHRONOUS"));
    assertEquals(Mode.ASYNC, RunnerConfigDeserializer.parseMode("async"));
    assertEquals(Mode.ASYNC, RunnerConfigDeserializer.parseMode("Async"));
    assertEquals(Mode.ASYNC, RunnerConfigDeserializer.parseMode("ASYNC"));
  }

  @Test
  public void parseModeInvalidTest(){
    assertThrows(VirDevConfigException.class, () -> RunnerConfigDeserializer.parseMode("SpongeBob"));
  }

}
