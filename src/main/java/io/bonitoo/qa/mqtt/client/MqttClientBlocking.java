package io.bonitoo.qa.mqtt.client;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.conf.Config;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class MqttClientBlocking extends AbstractMqttClient implements MqttClient {

    Mqtt5BlockingClient client;

    private MqttClientBlocking(){
        super();
    }

    static public MqttClientBlocking Client(BrokerConfig broker){
        MqttClientBlocking mcb = new MqttClientBlocking();
        mcb.broker = broker;
        String deviceID = Config.getDeviceID();
        mcb.client = Mqtt5Client.builder()
                .identifier(deviceID)
                .serverHost(broker.getHost())
                .serverPort(broker.getPort())
                .buildBlocking();
        return mcb;
    }

    @Override
    public MqttClient connect() throws InterruptedException {
        if(broker.getAuth().getUsername() != null){
            System.out.println("BLOCKING CLIENT CONNECTING WITH AUTH");
            return connectSimple(broker.getAuth().getUsername(),broker.getAuth().getPassword());
        }else{
            System.out.println("BLOCKING CLIENT CONNECTING ANON");
            return connectAnon();
        }
    }

    @Override
    public MqttClientBlocking connectSimple(String username, String password) throws InterruptedException {
        System.out.println("Authenticated BLOCKING CONNECTING");

        Mqtt5ConnAck ack = client.connectWith()
                .simpleAuth()
                .username(username)
                .password(password.getBytes())
                .applySimpleAuth()
                .willPublish()
                .topic("home/will")
                .payload(String.format("device %s gone", Config.getDeviceID()).getBytes())
                .applyWillPublish()
                .send();

        System.out.println("DEBUG ack " + ack);
        System.out.println("DEBUG client state " + client.getState());

        return this;
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
