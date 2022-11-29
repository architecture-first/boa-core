package com.architecture1st.boa.framework.technical.phrases;

/**
 * An event used to start communication from an non-Actor
 */
public class DefaultLocal extends ArchitectureFirstPhrase implements Local {

    /**
     * Create a default event
     * @param requestId
     */
    public DefaultLocal(String requestId) {
        super("default", "default");
        setRequestId(requestId);
    }

    /**
     * Sets the event as local, so it is not sent out into the Vicinity
     * @param status
     * @return
     */
    public DefaultLocal setAsLocal(boolean status) {
        // do not allow overriding
        return this;
    }
}
