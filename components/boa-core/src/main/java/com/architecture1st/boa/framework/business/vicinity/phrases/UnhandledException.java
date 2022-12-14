package com.architecture1st.boa.framework.business.vicinity.phrases;

import com.architecture1st.boa.framework.technical.phrases.ArchitectureFirstPhrase;

/**
 * Represents a phrase sent when an exception was not handled by a participant
 */
public class UnhandledException extends ArchitectureFirstPhrase implements Error {

    public UnhandledException(String from, String to) {
        super(from, to);
    }

    public void setException(Throwable t) {
        this.payload().put("exception", t);
    }

    public Throwable getException() {
        return (Throwable) this.payload().get("exception");
    }

}
