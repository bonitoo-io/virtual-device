# Bonitoo.io Virtual Device

Virtual devices for testing IoT frameworks.

## Configuration

Configuration is currently handled in the `src/main/resources/device.conf` file.

An alternate location can be defined by the environment variable `VIRTUAL_DEVICE_CONFIG`.

# Build

```shell
$ mvn clean compile
```

## Run Subscriber

Useful to verify published messages from terminal. 

```sh
$ mvn exec:java -Dmain.class="io.bonitoo.qa.Mqtt5Subscriber" 
```

## Run Default Publisher

```shell
$ mvn exec:java
```
