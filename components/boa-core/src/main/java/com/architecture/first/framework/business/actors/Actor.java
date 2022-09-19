package com.architecture.first.framework.business.actors;

import com.architecture.first.framework.business.actors.exceptions.ActorException;
import com.architecture.first.framework.technical.phrases.ArchitectureFirstPhrase;

/**
 * Represents an Actor in the system
 */
public interface Actor {
    void onException(ArchitectureFirstPhrase event, ActorException exception);
    void onException(ActorException exception, String message);
    String name();
    String group();

    boolean isSecurityGuard();

    void onError(String s);

    ArchitectureFirstPhrase announce(ArchitectureFirstPhrase incident);

    ArchitectureFirstPhrase say(ArchitectureFirstPhrase incident);

    void onException(ArchitectureFirstPhrase evt, ActorException e, String s);

    Actor notice(ArchitectureFirstPhrase vicinity);
}
