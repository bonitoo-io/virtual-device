package io.bonitoo.qa.conf.mqtt.broker;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = AuthConfigDeserializer.class)
public class AuthConfig {

    String username;
    String password;

    @Override
    public String toString(){
        return String.format("name=%s,password=%s", username, password); //password.replaceAll("/\\pL\\pN/", "*"));
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            return false;

        if(!(obj instanceof AuthConfig))
            return false;

        final AuthConfig conf = (AuthConfig)obj;

        return conf.getUsername().equals(username) &&
                conf.getPassword().equals(password);
    }

}
