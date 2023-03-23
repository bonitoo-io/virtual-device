package io.bonitoo.qa.device;

import io.bonitoo.qa.conf.device.DeviceConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@Getter
@Setter
@ToString
public abstract class Device extends Thread {

    static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    DeviceConfig config;

}
