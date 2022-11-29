package com.architecture1st.boa.framework.technical.aop;

import com.architecture1st.boa.framework.technical.user.UserInfo;
import com.architecture1st.boa.framework.security.model.Token;
import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Holds context information for a request
 */
@Data
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RequestContext {
    private UserInfo userInfo;
    private String requestId;
    private String tasklist;
    private boolean isAsync;
    private Throwable exception;
    private boolean hasErrors;

    public void setException(Throwable t) {
        this.exception = t;
        hasErrors = true;
    }

    public Long getCustomerId() {return (userInfo != null) ? userInfo.getUserId() : -1;}
    public Token getToken() {return (userInfo != null) ? userInfo.getToken() : new Token();}
    public String getAccessToken() {return getToken().getToken();}

    public String getRequestId() {
        return requestId;
    }
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isAsync() {return isAsync;}

    public String getTasklist() {
        return tasklist;
    }

    public void setTasklist(String tasklist) {
        this.tasklist = tasklist;
    }
}
