package com.architecture.first.framework.business.actors;

import com.architecture.first.framework.business.actors.exceptions.ActorException;
import com.architecture.first.framework.technical.events.ArchitectureFirstEvent;

/**
 * Represents an Actor in the system
 */
public interface Actor {
    void onException(ArchitectureFirstEvent event, ActorException exception);
    String name();
}
