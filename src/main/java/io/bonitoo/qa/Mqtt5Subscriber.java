package io.bonitoo.qa;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Mqtt5Subscriber {

    static final String defaultTopic = "test/#";

    // todo pass in topicFilter string as arg or in config
    static public void main(String[] args){

        String topic = System.getProperty("sub.topic") == null ?
                defaultTopic :
                System.getProperty("sub.topic");

        for(String arg : args){
            System.out.println(arg);
            if(arg.matches("^topic=.*")){
                topic = arg.split("=")[1];
            }
        }

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .identifier("testClient")
                .serverHost("localhost")
                .buildBlocking();

        Mqtt5ConnAck ack = client.connectWith().cleanStart(false).send();

        System.out.println("DEBUG connect ack " + ack);

        System.out.println("DEBUG subscribing to topic " + topic);

        CompletableFuture<Mqtt5SubAck> futureSubAck = client.toAsync().subscribeWith()
                .topicFilter(topic)
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
