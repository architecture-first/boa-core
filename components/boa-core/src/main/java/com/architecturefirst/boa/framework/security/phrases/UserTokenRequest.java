package com.architecturefirst.boa.framework.security.phrases;

import com.architecturefirst.boa.framework.security.model.UserToken;

import java.util.List;
import java.util.Map;

/**
 * Represents a user token request for internal processing
 */
public class UserTokenRequest extends TokenRequest implements AccessRequest {

    public static final String TOKEN = "token";

    public UserTokenRequest(String from, List<String> to) {
        super(from, to, null);
    }

    public UserTokenRequest(String from, String to) {
        super(from, to);
    }

    public UserTokenRequest setUserToken(UserToken token) {
        this.payload().put(TOKEN, token);
        return this;
    }

    public UserToken getCustomerToken() {
        return UserToken.from((Map<String,Object>) this.payload().get(TOKEN));
    }
}
