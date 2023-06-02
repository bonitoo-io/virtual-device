package io.bonitoo.qa.conf.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.bonitoo.qa.data.ItemType;
import io.bonitoo.qa.data.generator.NumGenerator;
import java.util.Arrays;
import java.util.Vector;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration for an Item based on random number generation.
 */
@NoArgsConstructor
@Getter
@Setter
public class ItemNumConfig extends ItemConfig {

  double max;
  double min;
  long period;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  Integer prec; // can be null

  /**
   * Basic all args constructor.
   *
   * @param name - name of the ItemConfig instance.
   * @param type - type to be handled.
   * @param min - minimum random value.
   * @param max - maximum random value.
   * @param period - sinusoid period used in calculating random value.
   */
  public ItemNumConfig(String name,
                       String label,
                       ItemType type,
                       double min,
                       double max,
                       long period) {
    super(name, label, type, NumGenerator.class.getName());
    this.max = max;
    this.min = min;
    this.period = period;
    ItemConfigRegistry.add(this.name, this);
  }

  public ItemNumConfig(String name,
                       String label,
                       ItemType type,
                       double min,
                       double max,
                       long period,
                       Integer prec) {
    this(name, label, type, min, max, period);
    this.prec = prec;
  }

  /**
   * Copy constructor user primarily when coupling configs to new item instances.
   *
   * @param orig - the original configuration.
   */
  public ItemNumConfig(ItemNumConfig orig) {
    super(orig);
    this.max = orig.max;
    this.min = orig.min;
    this.period = orig.period;
    if (orig.prec != null) {
      this.prec = orig.prec;
    }
  }

  @Override
  public String toString() {
    return String.format("name=%s,label=%s,max=%.2f,min=%.2f,"
        + "period=%d,type=%s,prec=%s\n",
      name, label, max, min, period, type, prec);
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    final ItemNumConfig conf = (ItemNumConfig) obj;
    if ((prec == null && conf.prec != null)
        || (prec != null && conf.prec == null)) {
      return false;
    } else if (prec != null) {
      if (!prec.equals(conf.prec)) {
        return false;
      }
    }
    return max == conf.max
      && min == conf.min
      && period == conf.period;
  }
}
