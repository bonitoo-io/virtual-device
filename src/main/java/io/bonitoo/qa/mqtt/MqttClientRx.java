package io.bonitoo.qa.mqtt;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.bonitoo.qa.util.Config;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.*;

import java.util.concurrent.TimeUnit;

@Builder
@AllArgsConstructor
@Setter
@Getter
public class MqttClientRx implements MqttClient {

    Mqtt5RxClient client;

    private MqttClientRx(){
        super();
    }

    static public MqttClientRx Client(){
        MqttClientRx mcr = new MqttClientRx();
        String deviceID = Config.getDeviceID();
        mcr.client = Mqtt5Client.builder()
                .identifier(deviceID)
                .serverHost(Config.getProp("broker.host"))
                .serverPort(Integer.parseInt(Config.getProp("broker.port")))
                .buildRx();
        return mcr;
    }

    @Override
    public MqttClient connect(String username, String password) throws InterruptedException {
        // TODO implement
        return null;
    }

    @Override
    public MqttClient connectAnon() throws InterruptedException {

        Single<Mqtt5ConnAck> sAck = client.connect();

        Completable connectScenario = sAck
                .doAfterSuccess(connAck -> System.out.println("Rx CONNECTED " + connAck ))
                .doOnError(throwable -> System.out.println("Rx Failed to connect " + throwable))
                .ignoreElement();

        connectScenario.blockingAwait(5000, TimeUnit.MILLISECONDS);

        return this;
    }

    @Override
    public MqttClient publish(String topic, String payload) throws InterruptedException {

        Flowable<Mqtt5Publish> msg2Publish = Flowable.just(
                Mqtt5Publish.builder()
                        .topic(topic)
                        .qos(MqttQos.EXACTLY_ONCE)
                        .payload(payload.getBytes())
                        .build());

        Completable pubScenario = client.publish(msg2Publish)
                .doOnNext(publishResult -> System.out.println(
                    "RX Publish Acknowledged " + new String(publishResult.getPublish().getPayloadAsBytes())
                ))
                .doOnError(throwable -> System.out.println("RX failed to publish " + throwable))
                .ignoreElements();

        pubScenario.blockingAwait(5000, TimeUnit.MILLISECONDS);
        return this;
    }

    @Override
    public MqttClient disconnect() {
        Completable completable = client.disconnect()
                .doOnComplete(() -> System.out.println("Rx Disconnected"))
                .doOnError(throwable -> System.err.println("Failed to disconnect " + throwable));

        completable.blockingAwait(5000, TimeUnit.MILLISECONDS);

        return this;
    }

    @Override
    public void shutdown() {

    }
}
