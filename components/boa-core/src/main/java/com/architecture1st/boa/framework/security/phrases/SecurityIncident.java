package com.architecture1st.boa.framework.security.phrases;

import com.architecture1st.boa.framework.technical.phrases.ArchitectureFirstPhrase;
import com.architecture1st.boa.framework.business.vicinity.phrases.Error;
import com.architecture1st.boa.framework.security.model.UserToken;

import java.util.List;

/**
 * Represents a security incident, such as an expired token
 */
public class SecurityIncident extends ArchitectureFirstPhrase implements Error {

    public static final String TOKEN = "token";

    public SecurityIncident(String from, List<String> to) {
        super(from, to);
    }

    public SecurityIncident(String from, String to) {
        super(from, to);
    }

    public SecurityIncident(String from, String to, ArchitectureFirstPhrase originalEvent) {
        super(from, to, originalEvent);
    }

    public SecurityIncident setCustomerToken(UserToken token) {
        this.payload().put(TOKEN, token);
        return this;
    }

}
