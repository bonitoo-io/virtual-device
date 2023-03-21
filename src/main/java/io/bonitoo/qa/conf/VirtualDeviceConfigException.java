package io.bonitoo.qa.conf;

public class VirtualDeviceConfigException extends IllegalStateException{
    public VirtualDeviceConfigException(String typeName) {
        super(typeName);
    }
}
