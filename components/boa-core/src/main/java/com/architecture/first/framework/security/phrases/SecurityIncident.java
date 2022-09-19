package com.architecture.first.framework.security.phrases;

import com.architecture.first.framework.business.vicinity.phrases.Error;
import com.architecture.first.framework.security.model.UserToken;
import com.architecture.first.framework.technical.phrases.ArchitectureFirstPhrase;

import java.util.List;

/**
 * Represents a security incident, such as an expired token
 */
public class SecurityIncident extends ArchitectureFirstPhrase implements Error {

    public static final String PHRASE_SECURITY_INCIDENT = "SecurityIncident";
    public static final String TOKEN = "token";

    public SecurityIncident(Object source, String from, List<String> to) {
        super(source, PHRASE_SECURITY_INCIDENT, from, to);
    }

    public SecurityIncident(Object source, String from, String to) {
        super(source, PHRASE_SECURITY_INCIDENT, from, to);
    }

    public SecurityIncident(Object source, String from, String to, ArchitectureFirstPhrase originalEvent) {
        super(source, PHRASE_SECURITY_INCIDENT, from, to, originalEvent);
    }

    public SecurityIncident setCustomerToken(UserToken token) {
        this.payload().put(TOKEN, token);
        return this;
    }

}
