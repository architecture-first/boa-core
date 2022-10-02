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

    public TokenRequest(Object source, String from, List<String> to) {
        this(source, from, to, null);
    }

    public TokenRequest(Object source, String from, List<String> to, ArchitectureFirstPhrase originalEvent) {
        super(source, PHRASE_TOKEN_REQUEST_EVENT, from, to, originalEvent);
    }

    public TokenRequest(Object source, String phraseName, String from, List<String> to, ArchitectureFirstPhrase originalEvent) {
        super(source, phraseName, from, to, originalEvent);
    }

    public TokenRequest(Object source, String from, String to) {
        this(source, PHRASE_TOKEN_REQUEST_EVENT, from, to);
    }

    public TokenRequest(Object source, String from, String to, ArchitectureFirstPhrase originalEvent) {
        this(source, PHRASE_TOKEN_REQUEST_EVENT, from, to, originalEvent);
    }

    public TokenRequest(Object source, String phraseName, String from, String to, ArchitectureFirstPhrase originalEvent) {
        super(source, phraseName, from, to, originalEvent);
    }

    public TokenRequest(Object source, String phraseName, String from, String to) {
        super(source, phraseName, from, to);
    }

    public TokenRequest setToken(Token token) {
        this.payload().put(TOKEN, token);
        return this;
    }

    public Token getToken() {
        return (Token) this.payload().get(TOKEN);
    }
}
