package com.architecture.first.framework.security.phrases;

import com.architecture.first.framework.security.model.UserToken;
import com.architecture.first.framework.technical.phrases.ArchitectureFirstPhrase;

import java.util.List;
import java.util.Map;

/**
 * Represents the reply for a user token request
 */
public class UserTokenReply extends TokenRequest implements AccessRequest {

    public static final String PHRASE_USER_TOKEN_REPLY = "UserTokenReply";
    public static final String TOKEN = "token";

    public UserTokenReply(Object source, String from, List<String> to) {
        this(source, from, to, null);
    }

    public UserTokenReply(Object source, String from, List<String> to, ArchitectureFirstPhrase originalEvent) {
        super(source, PHRASE_USER_TOKEN_REPLY, from, to, originalEvent);
    }

    public UserTokenReply(Object source, String from, String to) {
        this(source, from, to, null);
    }

    public UserTokenReply(Object source, String from, String to, ArchitectureFirstPhrase originalEvent) {
        super(source, PHRASE_USER_TOKEN_REPLY, from, to, originalEvent);
    }


    public UserTokenReply setCustomerToken(UserToken token) {
        this.payload().put(TOKEN, token);
        return this;
    }

    public UserToken getCustomerToken() {
        return UserToken.from((Map<String, Object>) this.payload().get(TOKEN));
    }
}
