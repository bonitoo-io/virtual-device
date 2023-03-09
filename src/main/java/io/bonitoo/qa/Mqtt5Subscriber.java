package io.bonitoo.qa;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Mqtt5Subscriber {

    static public void main(String[] args){
        Mqtt5BlockingClient client = Mqtt5Client.builder()
          //      .identifier(UUID.randomUUID().toString())
                .identifier("testClient")
                .serverHost("localhost")
                .buildBlocking();

        Mqtt5ConnAck ack = client.connectWith().cleanStart(false).send();

        System.out.println("DEBUG connect ack " + ack);

        CompletableFuture<Mqtt5SubAck> futureSubAck = client.toAsync().subscribeWith()
                .topicFilter("test/wumpus")
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    String content = StandardCharsets.UTF_8.decode(publish.getPayload().get()).toString();
                    System.out.println("Callback on publish " + publish);
                    System.out.println("Payload " + content);
                })
                .send();

        System.out.println("DEBUG connect futureSubAck " + futureSubAck);
        try {
            System.out.println("DEBUG future.get() " + futureSubAck.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
