package io.bonitoo.qa.plugin.sample;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for coupling a SamplePlugin class with a custom
 * SamplePluginConfig file.  Used in deserializing configurations.
 *
 * <p><emphasis>N.B.</emphasis> When this annotation is omitted
 * with a custom SamplePlugin the default <code>SamplePluginConfig</code> class
 * is used.  In this case any special fields of the plugin, will likely not be
 * initialized properly.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SamplePluginConfigClass {

  /**
   * Gets the declared custom configuration class, used
   * in deserializing a YAML config file.
   *
   * @return - a custom configuration class
   */
  Class<? extends SamplePluginConfig> conf() default SamplePluginConfig.class;

}
