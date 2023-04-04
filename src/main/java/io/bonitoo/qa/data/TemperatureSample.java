package io.bonitoo.qa.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A standard sample based on temperature.
 *
 * <p>Note this type was used in the very first POC iteration of this project and
 * should be removed or repurposed.
 */

// TODO refactor with inheritance from Sample type
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TemperatureSample {

  public String id;

  public long timestamp;

  public double tmpr;
}
