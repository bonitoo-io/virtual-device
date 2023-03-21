package io.bonitoo.qa.device;

import io.bonitoo.qa.conf.device.DeviceConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class Device extends Thread {

    DeviceConfig config;

}
