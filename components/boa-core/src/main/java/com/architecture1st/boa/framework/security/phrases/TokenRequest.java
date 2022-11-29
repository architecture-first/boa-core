package com.architecture1st.boa.framework.security.phrases;

import com.architecture1st.boa.framework.technical.phrases.ArchitectureFirstPhrase;
import com.architecture1st.boa.framework.security.model.Token;

import java.util.List;

/**
 * Represents a request for an access token
 */
public class TokenRequest extends ArchitectureFirstPhrase implements AccessRequest {

    public static final String TOKEN = "token";

    public TokenRequest(String from, List<String> to) {
        this(from, to, null);
    }

    public TokenRequest(String from, List<String> to, ArchitectureFirstPhrase originalEvent) {
        super(from, to, originalEvent);
    }

    public TokenRequest(String from, String to, ArchitectureFirstPhrase originalEvent) {
        super(from, to, originalEvent);
    }

    public TokenRequest(String from, String to) {
        super(from, to);
    }

    public TokenRequest setToken(Token token) {
        this.payload().put(TOKEN, token);
        return this;
    }

    public Token getToken() {
        return (Token) this.payload().get(TOKEN);
    }
}
