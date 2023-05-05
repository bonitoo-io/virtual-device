package io.bonitoo.qa.device;

import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.data.GenericSample;
import java.lang.invoke.MethodHandles;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for a device, which should be a thread with a device config.
 *
 * <p>Specific device behavior must be specified in the run() function of
 * extensions of this class.
 */
@Getter
@Setter
@ToString
public abstract class Device extends Thread {

  static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  DeviceConfig config;

  List<GenericSample> sampleList;

}
