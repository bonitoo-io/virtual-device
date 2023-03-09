package io.bonitoo.qa.mqtt;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import io.bonitoo.qa.util.Config;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class MqttClientBlocking implements MqttClient{

    Mqtt5BlockingClient client;

    private MqttClientBlocking(){
        super();
    }

    static public MqttClientBlocking Client(){
        MqttClientBlocking mcb = new MqttClientBlocking();
        String deviceID = Config.getDeviceID();
        mcb.client = Mqtt5Client.builder()
                .identifier(deviceID)
                .serverHost(Config.getProp("broker.host"))
                .serverPort(Integer.parseInt(Config.getProp("broker.port")))
                .buildBlocking();
        return mcb;
    }

    @Override
    public MqttClient connect(String username, String password) throws InterruptedException {
        return null;
    }

    @Override
    public MqttClientBlocking connectAnon() throws InterruptedException {
        System.out.println("BLOCKING CONNECTING");

        Mqtt5ConnAck ack = client.connect();

        System.out.println("DEBUG ack " + ack);
        System.out.println("DEBUG client state " + client.getState());

        return this;
    }

    @Override
    public MqttClientBlocking publish(String topic, String payload) throws InterruptedException {

        System.out.println("BLOCKING PUBLISHING");

        client.publishWith()
                .topic(topic)
                .payload(payload.getBytes())
                .send();

        return this;

    }

    @Override
    public MqttClientBlocking disconnect() {

        System.out.println("BLOCKING DISCONNECTING");

        client.disconnect();

        return this;
    }

    @Override
    public void shutdown() {

    }
}
