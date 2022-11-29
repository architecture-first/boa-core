package com.architecture1st.boa.framework.business.actors.exceptions;

import com.architecture1st.boa.framework.technical.phrases.ArchitectureFirstPhrase;
import com.architecture1st.boa.framework.business.actors.Actor;

/**
 * The base exception for an Actor
 */
public class ActorException extends RuntimeException {
    private final Actor actor;
    private ArchitectureFirstPhrase errorPhrase;

    public ActorException(Actor actor, Exception e) {
        super(e);
        this.actor = actor;
    }

    public ActorException(Actor actor, ArchitectureFirstPhrase errorPhrase) {
        super(new RuntimeException());
        this.actor = actor;
        this.errorPhrase = errorPhrase;
    }

    public ActorException(Actor actor, String message) {
        super(message);
        this.actor = actor;
    }

    public Actor getActor() {return this.actor;}

    public ArchitectureFirstPhrase getErrorPhrase() {
        return errorPhrase;
    }
}
