package com.architecture.first.framework.business.vicinity.acknowledgement;

import com.architecture.first.framework.business.vicinity.Vicinity;
import com.architecture.first.framework.business.vicinity.info.VicinityInfo;
import com.architecture.first.framework.business.vicinity.messages.VicinityMessage;
import com.architecture.first.framework.technical.cache.JedisHCursor;
import com.architecture.first.framework.technical.phrases.ArchitectureFirstPhrase;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A repository for the acknowledgement of the acceptance of phrases
 */
@Slf4j
@Repository
public class Acknowledgements {

    public enum Status {
        Unacknowledged ("Unacknowledged"),
        Acknowledged ("Acknowledged");

        private final String status;

        Status(String status) {
            this.status = status;
        }
    }

    public static final String INDEX = "index";
    public static long NOT_EXECUTED = -1l;

    public class Entry {
        private final String classname;
        private final ArchitectureFirstPhrase json;

        protected Entry(String classname, ArchitectureFirstPhrase json) {
            this.classname = classname;
            this.json = json;
        }
        public String toString() {var json = gson.toJson(this); return json;}
    }

    private final int expirationSeconds = 3600; //43200; // 12 hours
    private static final Gson gson = new Gson();
    public static String ACK_TEMPLATE = "Ack";
    public static String UNACK_TEMPLATE = "UnAck";

    @Autowired
    private JedisPooled jedis;

    @Autowired
    private Vicinity vicinity;

    @Autowired
    private VicinityInfo vicinityInfo;

    private final String ackConnectionId = UUID.randomUUID().toString();

    /**
     * Records a phrase that needs acknowledgement and has not been acknowledged yet
     * @param phrase
     * @return the item number of the phrase
     */
    public long recordUnacknowledgedPhrase(ArchitectureFirstPhrase phrase) {
        if (isEnabled()) {
            if ((phrase.name().equals("SelfVicinityCheckup"))) {
                return 0;
            }

            var ack = generateHandle(phrase.getRequestId(), Status.Unacknowledged);
            var index = jedis.hincrBy(ack, INDEX, 1);
            phrase.setIndex(index);
            phrase.setOriginalActorName(phrase.toFirst());
            var message = vicinity.generateMessage(phrase, phrase.toFirst());

            // note: these calls should be in a transaction

            jedis.hset(ack, String.valueOf(index), message.toString());
            jedis.expire(ack, expirationSeconds);

            return index;
        }

        return NOT_EXECUTED;
    }

    /**
     * Removes a phrase that has been acknowledged
     * @param requestId
     * @param index
     */
    public void removeUnacknowledgedPhrase(String requestId, long index) {
        if (isEnabled()) {
            var ack = generateHandle(requestId, Status.Unacknowledged);
            jedis.hdel(ack, String.valueOf(index));
        }
    }

    /**
     *
     * @param phrase
     * @return
     */
    public long recordAcknowledgement(ArchitectureFirstPhrase phrase) {
        if (isEnabled()) {
            if (phrase.name().equals("SelfVicinityCheckup") || phrase.name().equals("Acknowledgement")) {
                return 0;
            }

            var ack = generateHandle(phrase.getRequestId(), Status.Acknowledged);
            var actor = phrase.getTarget().get();
            var message = vicinity.generateMessage(phrase, phrase.toFirst());

            // note: these calls should be in a transaction
            var index = phrase.index();
            if (!jedis.hexists(ack, String.valueOf(index))) {
                jedis.hset(ack, String.valueOf(index), message.toString());
                jedis.expire(ack, expirationSeconds);

                removeUnacknowledgedPhrase(phrase.getRequestId(), phrase.index());

                // To and From reversed for acknowledgement
                var ackPhrase = new com.architecture.first.framework.business.vicinity.phrases.Acknowledgement(this, phrase.toFirst(), phrase.from())
                        .setAcknowledgementPhrase(phrase);
                var ackMessage = vicinity.generateMessage(ackPhrase, phrase.from());
                vicinity.publishMessage(ackPhrase.toFirst(), ackMessage.toString());
            }

            return index;
        }

        return NOT_EXECUTED;
    }

    /**
     * Get an unacknowledged phrase by request d and index
     * @param requestId
     * @param index
     * @return
     */
    public ArchitectureFirstPhrase getUnacknowledgedPhrase(String requestId, String index) {
        if (isEnabled()) {
            var ack = generateHandle(requestId, Status.Unacknowledged);
            var json = jedis.hget(ack, index);
            if (StringUtils.isNotEmpty(json)) {
                var message = VicinityMessage.from(json);

                return ArchitectureFirstPhrase.from(this, message);
            }
        }

        return null;
    }

    /**
     * generate a handle to a phrase set for a given request
     * @param requestId
     * @param status
     * @return
     */
    private String generateHandle(String requestId, Status status) {
        return String.format("%s/%s", requestId,  (status == Status.Acknowledged) ? ACK_TEMPLATE : UNACK_TEMPLATE);
    }

    /**
     * Determines if a given phrase was acknowledged
     * @param requestId
     * @param phraseName
     * @return
     */
    public boolean hasAcknowledged(String requestId, String phraseName) {
        if (isEnabled()) {
            var ack = generateHandle(requestId, Status.Acknowledged);

            AtomicBoolean hasPassed = new AtomicBoolean(false);

            var cursor = new JedisHCursor(jedis);
            cursor.processAll(ack, e -> {
                var vicinityMessage = VicinityMessage.from(e.getValue());
                var phrase = (com.architecture.first.framework.business.vicinity.phrases.Acknowledgement) ArchitectureFirstPhrase.from(this, vicinityMessage);
                if (phrase.getAcknowledgedPhraseName().equals(phraseName)) {
                    hasPassed.set(true);
                    return true;
                }

                return false;
            });

            return hasPassed.get();
        }

        return false;
    }

    private boolean isEnabled() {
        return vicinityInfo.getAcknowledgement().equals(VicinityInfo.VALUE_ENABLED);
    }
 }
