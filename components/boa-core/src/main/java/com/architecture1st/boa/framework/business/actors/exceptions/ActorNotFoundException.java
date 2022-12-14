package com.architecture1st.boa.framework.business.actors.exceptions;

import com.architecture1st.boa.framework.technical.phrases.ArchitectureFirstPhrase;

/**
 * Exception thrown when no Actor can be found to send a phrase to
 */
public class ActorNotFoundException extends ActorException {
    private ArchitectureFirstPhrase phrase;

    public ActorNotFoundException(String message) {
        super(null, message);
    }

    public ArchitectureFirstPhrase getPhrase() {
        return phrase;
    }

    public ActorNotFoundException setPhrase(ArchitectureFirstPhrase phrase) {
        this.phrase = phrase;
        return this;
    }
}
