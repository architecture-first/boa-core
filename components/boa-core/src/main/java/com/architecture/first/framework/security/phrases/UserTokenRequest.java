package com.architecture.first.framework.security.phrases;

import com.architecture.first.framework.security.model.UserToken;

import java.util.List;
import java.util.Map;

/**
 * Represents a user token request for internal processing
 */
public class UserTokenRequest extends TokenRequest implements AccessRequest {

    public static final String PHRASE_USER_TOKEN_REQUEST = "UserTokenRequest";
    public static final String TOKEN = "token";

    public UserTokenRequest(Object source, String from, List<String> to) {
        super(source, PHRASE_USER_TOKEN_REQUEST, from, to, null);
    }

    public UserTokenRequest(Object source, String from, String to) {
        super(source, PHRASE_USER_TOKEN_REQUEST, from, to);
    }

    public UserTokenRequest setUserToken(UserToken token) {
        this.payload().put(TOKEN, token);
        return this;
    }

    public UserToken getCustomerToken() {
        return UserToken.from((Map<String,Object>) this.payload().get(TOKEN));
    }
}
