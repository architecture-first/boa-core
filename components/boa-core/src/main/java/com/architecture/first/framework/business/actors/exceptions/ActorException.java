package com.architecture.first.framework.business.actors.exceptions;

import com.architecture.first.framework.business.actors.Actor;
import com.architecture.first.framework.technical.phrases.ArchitectureFirstPhrase;

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
