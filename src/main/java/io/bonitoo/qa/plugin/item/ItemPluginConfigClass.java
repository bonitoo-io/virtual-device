package io.bonitoo.qa.plugin.item;

import io.bonitoo.qa.conf.data.ItemPluginConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping an ItemPlugin subclass to its configuration class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ItemPluginConfigClass {

  /**
   * Identifier for the configuration class of an ItemConfig subclass.
   *
   * @return - The Class
   */
  Class<? extends ItemPluginConfig> conf() default ItemPluginConfig.class;

}
