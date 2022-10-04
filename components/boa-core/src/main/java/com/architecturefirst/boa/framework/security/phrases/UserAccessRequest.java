package com.architecturefirst.boa.framework.security.phrases;

import com.architecturefirst.boa.framework.security.model.Credentials;

import java.util.List;

/**
 * Represents a request for an access token using User credentials
 */
public class UserAccessRequest extends TokenRequest implements AccessRequest {

    public static final String PHRASE_USER_ACCESS_REQUEST = "UserAccessRequest";
    public static final String CREDENTIALS = "credentials";

    public UserAccessRequest(String from, List<String> to) {
        super(PHRASE_USER_ACCESS_REQUEST, from, to, null);
    }

    public UserAccessRequest(String from, String to) {
        super(PHRASE_USER_ACCESS_REQUEST, from, to);
    }

    public UserAccessRequest setCredentials(Credentials credentials) {
        this.payload().put(CREDENTIALS, credentials);
        return this;
    }

    public Credentials getCredentials() {
        return (Credentials) this.payload().get(CREDENTIALS);
    }
}
