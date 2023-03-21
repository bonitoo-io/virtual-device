package io.bonitoo.qa.data;

import lombok.*;

/*
Example from CNT

{
  "appId": "AIR_QUAL",
  "data": 26.0,
  "messageType": "DATA",
  "ts": 1677874340000,
  "timestamp": "3/3/3333 3:33:33 PM"
}
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TemperatureSample{

    public String id;

    public long timestamp;

    public double tmpr;
}
