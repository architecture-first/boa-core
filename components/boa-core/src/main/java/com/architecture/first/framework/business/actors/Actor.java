package com.architecture.first.framework.business.actors;

import com.architecture.first.framework.business.actors.exceptions.ActorException;
import com.architecture.first.framework.business.vicinity.events.UnhandledExceptionEvent;
import com.architecture.first.framework.security.events.SecurityIncidentEvent;
import com.architecture.first.framework.technical.events.ArchitectureFirstEvent;

/**
 * Represents an Actor in the system
 */
public interface Actor {
    void onException(ArchitectureFirstEvent event, ActorException exception);
    void onException(ActorException exception, String message);
    String name();
    String group();

    boolean isSecurityGuard();

    void onError(String s);

    ArchitectureFirstEvent announce(ArchitectureFirstEvent incident);

    ArchitectureFirstEvent say(ArchitectureFirstEvent incident);

    void onException(ArchitectureFirstEvent evt, ActorException e, String s);

    Actor notice(ArchitectureFirstEvent vicinity);
}
