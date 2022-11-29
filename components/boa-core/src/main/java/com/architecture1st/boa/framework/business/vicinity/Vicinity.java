package com.architecture1st.boa.framework.business.vicinity;

import com.architecture1st.boa.framework.business.actors.Actor;
import com.architecture1st.boa.framework.business.vicinity.area.ActorInArea;
import com.architecture1st.boa.framework.business.vicinity.messages.VicinityMessage;
import com.architecture1st.boa.framework.technical.phrases.ArchitectureFirstPhrase;

import java.util.List;
import java.util.function.BiFunction;

public interface Vicinity {
    String name();
    void onApplicationPhrase(ArchitectureFirstPhrase event);
    VicinityMessage generateMessage(ArchitectureFirstPhrase event, String to);
    void publishMessage(String to, String contents);
    void subscribe(Actor owner, String target, BiFunction<Actor, ArchitectureFirstPhrase, Void> fnCallback);
    void unsubscribe(String target);
    boolean areConnectionsOk(String target, int numberOfConnections);
    ActorInArea findActor(String type, String area, String project);
    ActorInArea findActor(String type, String area);

    List<ActorInArea> findActiveActors(java.lang.String area, java.lang.String project);
    boolean actorIsAvailable(String name, String area);

}
