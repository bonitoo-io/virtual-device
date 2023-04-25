package io.bonitoo.qa.conf.data;

import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.data.generator.NumGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Configuration for an Item based on random number generation.
 */
@Getter
@Setter
public class ItemNumConfig extends ItemConfig {

  double max;
  double min;
  long period;

  /**
   * Basic all args constructor.
   *
   * @param name - name of the ItemConfig instance.
   * @param type - type to be handled.
   * @param min - minimum random value.
   * @param max - maximum random value.
   * @param period - sinusoid period used in calculating random value.
   */
  public ItemNumConfig(String name, String label, ItemType type, double min, double max, long period) {
    super(name, label, type, NumGenerator.class.getName(), new Vector<>(Arrays.asList("period","min","max","time")));
    this.max = max;
    this.min = min;
    this.period = period;

    ItemConfigRegistry.add(this.name, this);
  }

  @Override
  public String toString() {
    return String.format("name=%s,label=%s,max=%.2f,min=%.2f,period=%d,type=%s\n",
      name, label, max, min, period, type);
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    final ItemNumConfig conf = (ItemNumConfig) obj;
    return max == conf.max && min == conf.min && period == conf.period;
  }
}
