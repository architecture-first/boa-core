package com.architecturefirst.boa.framework.business.vicinity;

import com.architecturefirst.boa.framework.business.actors.Actor;
import com.architecturefirst.boa.framework.business.vicinity.messages.VicinityMessage;
import com.architecturefirst.boa.framework.technical.phrases.ArchitectureFirstPhrase;

import java.util.List;
import java.util.function.BiFunction;

public interface Vicinity {
    void onApplicationPhrase(ArchitectureFirstPhrase event);
    VicinityMessage generateMessage(ArchitectureFirstPhrase event, String to);
    void publishMessage(String to, String contents);
    void subscribe(Actor owner, String target, BiFunction<Actor, ArchitectureFirstPhrase, Void> fnCallback);
    void unsubscribe(String target);
    boolean areConnectionsOk(String target, int numberOfConnections);
    String findActor(String type, String area, String project);
    String findActor(String type, String area);

    List<String> findActiveActors(java.lang.String area, java.lang.String project);
    boolean actorIsAvailable(String name, String area);

}
