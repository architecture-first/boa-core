package com.architecturefirst.boa.framework.technical.threading.exceptions;

import com.architecturefirst.boa.framework.business.actors.Actor;
import com.architecturefirst.boa.framework.business.vicinity.phrases.UnhandledException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handles uncaught exceptions and notifies an Actor
 */
@Slf4j
public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Autowired
    private Actor actor;

    /**
     * Handle an uncaught exception and notify the related Actor
     * @param t the thread
     * @param e the exception
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Error in thread: ", e);

        actor.notice(new UnhandledException(this, "vicinity", actor.name()));
    }
}
