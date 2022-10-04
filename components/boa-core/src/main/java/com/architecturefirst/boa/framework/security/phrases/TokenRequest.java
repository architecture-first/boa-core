package com.architecturefirst.boa.framework.security.phrases;

import com.architecturefirst.boa.framework.security.model.Token;
import com.architecturefirst.boa.framework.technical.phrases.ArchitectureFirstPhrase;

import java.util.List;

/**
 * Represents a request for an access token
 */
public class TokenRequest extends ArchitectureFirstPhrase implements AccessRequest {

    public static final String PHRASE_TOKEN_REQUEST_EVENT = "TokenRequest";
    public static final String TOKEN = "token";

    public TokenRequest(String from, List<String> to) {
        this(from, to, null);
    }

    public TokenRequest(String from, List<String> to, ArchitectureFirstPhrase originalEvent) {
        super(PHRASE_TOKEN_REQUEST_EVENT, from, to, originalEvent);
    }

    public TokenRequest(String phraseName, String from, List<String> to, ArchitectureFirstPhrase originalEvent) {
        super(phraseName, from, to, originalEvent);
    }

    public TokenRequest(String from, String to) {
        this(PHRASE_TOKEN_REQUEST_EVENT, from, to);
    }

    public TokenRequest(String from, String to, ArchitectureFirstPhrase originalEvent) {
        this(PHRASE_TOKEN_REQUEST_EVENT, from, to, originalEvent);
    }

    public TokenRequest(String phraseName, String from, String to, ArchitectureFirstPhrase originalEvent) {
        super(phraseName, from, to, originalEvent);
    }

    public TokenRequest(String phraseName, String from, String to) {
        super(phraseName, from, to);
    }

    public TokenRequest setToken(Token token) {
        this.payload().put(TOKEN, token);
        return this;
    }

    public Token getToken() {
        return (Token) this.payload().get(TOKEN);
    }
}
