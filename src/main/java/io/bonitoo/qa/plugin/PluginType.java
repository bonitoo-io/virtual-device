package io.bonitoo.qa.plugin;

/**
 * The type of plugin implemented.
 * <ul>
 *   <li>{@link PluginType#Item} - An item data generator that returns a single
 *   generated value.</li>
 *   <li>{@link PluginType#Sample} - A sample data generator that returns a
 *   set of items and their values as a JSON string.</li>
 * </ul>
 */
public enum PluginType {

  Sample,
  Item

}
