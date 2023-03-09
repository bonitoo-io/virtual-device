package io.bonitoo.qa.device;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractDevice implements Device {

    String topic;

    String id;

    String name;

    String description;

    Long interval;

}
