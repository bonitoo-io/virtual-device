package io.bonitoo.qa.mqtt.client;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import io.bonitoo.qa.conf.device.DeviceConfig;
import io.bonitoo.qa.conf.mqtt.broker.BrokerConfig;
import io.bonitoo.qa.conf.Config;
import io.bonitoo.qa.util.LogHelper;
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

    static public MqttClientBlocking Client(BrokerConfig broker, String id){
        MqttClientBlocking mcb = new MqttClientBlocking();
        mcb.broker = broker;
        mcb.id = id;
        mcb.client = Mqtt5Client.builder()
                .identifier(id)
                .serverHost(broker.getHost())
                .serverPort(broker.getPort())
                .buildBlocking();
        return mcb;
    }

    @Override
    public MqttClient connect() throws InterruptedException {
        if(broker.getAuth() == null || broker.getAuth().getUsername() == null){
            return connectAnon();
        }else{
            return connectSimple(broker.getAuth().getUsername(),broker.getAuth().getPassword());
        }
    }

    @Override
    public MqttClientBlocking connectSimple(String username, String password) throws InterruptedException {
        logger.info(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(), "Connect Simple", broker.getAuth().getUsername()));

        Mqtt5ConnAck ack = client.connectWith()
                .simpleAuth()
                .username(username)
                .password(password.getBytes())
                .applySimpleAuth()
                .willPublish()
                .topic("home/will")
                .payload(String.format("device %s gone", id).getBytes())
                .applyWillPublish()
                .send();

        logger.debug(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
                "ACK Connect",
                ack.toString()));
        logger.debug(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
                "Current state",
                client.getState().toString()));

        return this;
    }

    @Override
    public MqttClientBlocking connectAnon() throws InterruptedException {
        logger.info(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(), "Connect Anonymous", ""));

        Mqtt5ConnAck ack = client.connect();

        logger.debug(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
                "ACK Connect",
                ack.toString()));
        logger.debug(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
                "Current state",
                client.getState().toString()));

        return this;
    }

    @Override
    public MqttClientBlocking publish(String topic, String payload) throws InterruptedException {

        logger.info(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(),
                "Publishing",
                String.format("[%s] - %s", topic, payload)));

        client.publishWith()
                .topic(topic)
                .payload(payload.getBytes())
                .send();

        return this;

    }

    @Override
    public MqttClientBlocking disconnect() {

        client.disconnect();

        logger.info(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(), "Disconnected", ""));

        return this;
    }

    @Override
    public void shutdown() {
        logger.info(LogHelper.buildMsg(client.getConfig().getClientIdentifier().get().toString(), "Shutting down", ""));
    }
}
