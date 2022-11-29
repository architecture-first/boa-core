package com.architecture1st.boa.framework.business.vicinity.conversation;

import com.architecture1st.boa.framework.business.vicinity.info.VicinityInfo;
import com.architecture1st.boa.framework.technical.cache.JedisHCursor;
import com.architecture1st.boa.framework.technical.phrases.ArchitectureFirstPhrase;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a conversation between two or more Actors
 */
@Slf4j
@Repository
public class Conversation {

    public enum Status {
        Starting ("Starting"),
        Replying ("Replying"),
        SendingViaVicinity ("SendingViaVicinity"),
        ReceivedInVicinity ("ReceivedInVicinity"),
        ReceivedByActor ("ReceivedInVicinity"),
        AcknowledgedByActor ("AcknowledgedByActor"),
        ErrorAfterReceivedByActor ("ErrorAfterReceivedByActor");

        private final String status;

        Status(String status) {
            this.status = status;
        }
    }

    protected class Entry {
        private final String subject;
        private final String from;
        private final String to;
        private final String index;
        private Status status;

        Entry(String subject, String from, String to, String index) {
            this.subject = subject;
            this.from = from;
            this.to = to;
            this.index = index;
        }

        public String toString() {var json = gson.toJson(this); return json;}
        public static Entry fromString(String json) {return gson.fromJson(json, Entry.class);}
    }

    private long expirationSeconds = 3600; //default
    private static final Gson gson = new Gson();
    public static String CONVO_TEMPLATE = "boa.%s/%s/Conversation";

    @Autowired
    private JedisPooled jedis;

    @Autowired
    private VicinityInfo vicinityInfo;

    private final String convoConnectionId = UUID.randomUUID().toString();

    @PostConstruct
    public void init() {
        log.info("conversationConnectionId: " + convoConnectionId);
        expirationSeconds = vicinityInfo.getConversationExpirationSeconds();
    }

    /**
     * Record the current state of the conversation
     * @param requestId - request id
     * @param subject - subject of the conversation
     * @param from - from Actor
     * @param to - to Actor
     * @param index - message order
     * @param status - status of the conversation
     * @return
     */
    public String record(String area, String requestId, String subject, String from, String to, String index, Status status) {
        var convo = generateSignature(area, requestId);
        var entry = new Entry(subject, from, to, index).toString();
        var rc = jedis.hset(convo, entry, status.toString()) == 1 ? entry : "ERROR_UNABLE_TO_RECORD_CONVO";
        jedis.expire(convo, expirationSeconds);

        return rc;
    }

    /**
     * Record the current state of the conversation
     * @param event - the event in process
     * @param status - the status of the event processing
     * @return
     */
    public String record(ArchitectureFirstPhrase event, Status status) {
        return  (!event.name().equals("SelfVicinityCheckup") && !event.name().equals("Acknowledgement")
                && !event.toFirst().equals("VicinityMonitor"))
                ? record(event.area(), event.getRequestId(), event.name(), event.from(), event.toFirst(), String.valueOf(event.index()), status)
                : "WARNING_CONVO_ENTRY_IGNORED";
    }

    /**
     * Determines if an Actor has acknowledged an event
     * @param requestId
     * @param eventName
     * @param from
     * @param to
     * @return
     */
    public boolean hasAcknowledged(String requestId, String areaName, String eventName, String from, String to) {
        var convo = generateSignature(requestId, areaName);

        AtomicBoolean hasPassed = new AtomicBoolean(false);

        var cursor = new JedisHCursor(jedis);
        cursor.processAll(convo, e -> {
            var entry = Entry.fromString(e.getKey());
            if (entry.subject.equals(eventName) &&
                entry.from.equals(from) &&
                entry.to.equals(to)) {

                if (e.getValue().equals(Status.AcknowledgedByActor.toString())) {
                    hasPassed.set(true);
                    return true;
                }
            }

            return false;
        });

        return hasPassed.get();
    }

    /**
     * Generate a signature for a given request
     * @param requestId
     * @return
     */
    private String generateSignature(String requestId, String areaName) {
        return String.format(CONVO_TEMPLATE, areaName, requestId);
    }
 }
