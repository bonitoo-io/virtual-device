package io.bonitoo.qa.device;

public class TestDevice extends Device {

  boolean called = false;

  @Override
  public void run(){
    called = true;
  }

  public boolean isCalled() {
    return called;
  }
}
