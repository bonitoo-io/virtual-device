package io.bonitoo.qa.mqtt;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.bonitoo.qa.util.Config;
import lombok.*;

import java.util.concurrent.CompletableFuture;

@Builder
@AllArgsConstructor
@Setter
@Getter
public class MqttClientAsync implements MqttClient {

    Mqtt5AsyncClient client;

    private MqttClientAsync(){
        super();
    }

    static public MqttClientAsync Client(){
        MqttClientAsync mqc = new MqttClientAsync();

        String deviceID = Config.getDeviceID();

        System.out.println("deviceID " + deviceID);

        mqc.client = Mqtt5Client.builder()
                .identifier(deviceID)
                .serverHost(Config.getProp("broker.host"))
                .serverPort(Integer.parseInt(Config.getProp("broker.port")))
                .buildAsync();

        return mqc;
    }

    @Override
    public MqttClient connect(String username, String password) throws InterruptedException {
        //TODO implement
        return null;
    }

    @Override
    public MqttClientAsync connectAnon() throws InterruptedException {

        System.out.println("CONNECTING");

        CompletableFuture<Mqtt5ConnAck> cf = client.connect().whenComplete((conAck, throwable) -> {
            if(throwable != null){
                System.err.println("Failed to connect to broker");
                System.err.println(throwable);
                System.exit(1);
            }
            System.out.println("Connected to broker");
        });

        System.out.print("connecting.");
        while(!cf.isDone()){
            System.out.print(".");
            Thread.sleep(300);
        }

        return this;

    }

    @Override
    public MqttClientAsync publish(String topic, String payload) throws InterruptedException {

        System.out.println("PUBLISHING");
        if(!client.getState().isConnected()){
            // Todo fix if connection is not anonymous
            connectAnon();
        }

        client.publishWith()
                .topic(topic)
                .payload(payload.getBytes())
                .send()
                .whenComplete((result, throwable) -> {
                    if(throwable != null){
                        System.err.println("Failed to publish to topic " + topic);
                        System.out.println(throwable);
                    }else{
                        System.out.println("Publish Success " + result);
                    }
                });

        return this;
    }

    @Override
    public MqttClientAsync disconnect() throws InterruptedException {

        System.out.println("DISCONNECTING");

        Mqtt5Disconnect disconnect = Mqtt5Disconnect.builder()
                .reasonCode(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION)
                .sessionExpiryInterval(600l)
                .reasonString("Planned Disconnect").build();

        if(client.getState().isConnected()){
            client.disconnect(disconnect)
                    .whenComplete((ack, throwable) -> {
                       if(throwable != null){
                           System.err.println("Failed to disconnect");
                           System.err.println(throwable);
                       }else{
                           System.out.println("Disconnect Success");
                       }
                    });
        }

        return this;
    }

    @Override
    public void shutdown(){

    }

}
