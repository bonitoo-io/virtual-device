package io.bonitoo.qa.data;

import lombok.*;

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
