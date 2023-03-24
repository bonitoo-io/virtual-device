package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class AuthConfigDeserializer extends StdDeserializer<AuthConfig> {

    public AuthConfigDeserializer(){
        this(null);
    }

    protected AuthConfigDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public AuthConfig deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException, JacksonException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String username = node.get("username").asText();
        String password = node.get("password").asText();
        return new AuthConfig(username, password);

    }
}