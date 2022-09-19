package com.architecture.first.framework.business.vicinity.todo;

import com.architecture.first.framework.business.vicinity.Vicinity;
import com.architecture.first.framework.business.vicinity.acknowledgement.Acknowledgements;
import com.architecture.first.framework.business.vicinity.info.VicinityInfo;
import com.architecture.first.framework.technical.cache.JedisHCursor;
import com.architecture.first.framework.technical.phrases.ArchitectureFirstPhrase;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a list of items for actors in a group to process
 */
@Component
public class ToDoList {
    public enum Status {
        Pending ("Pending"),
        InProgress ("InProgress"),
        Completed ("Completed"),
        Failed ("Failed");

        private final String status;

        Status(String status) {
            this.status = status;
        }
    }

    public static long NOT_EXECUTED = -1l;

    @Autowired
    private JedisPooled jedis;

    @Autowired
    private Acknowledgements ack;

    @Autowired
    private Vicinity vicinity;

    @Autowired
    private VicinityInfo vicinityInfo;

    private final Gson gson = new Gson();

    public static String TO_DO_LIST = "ToDo";

    /**
     * Add a task that represents an unacknowledged phrase
     * @param group - group, such as Merchant
     * @param key - key to determine entry
     * @param position - position in the UnAck list
     * @return 1 if successful
     */
    public long addTask(String group, String key, long position) {
        if (isEnabled()) {
            var entry = new ToDoListEntry(group, key, position);
            return jedis.hset(generateSignature(group), entry.toString(), Status.Pending.status);
        }

        return NOT_EXECUTED;
    }

    /**
     * Adds a phrase to process later
     * @param phrase
     * @return 1 if successful
     */
    public long addTask(ArchitectureFirstPhrase phrase) {
        if (isEnabled()) {
            phrase.setAsToDoTask(true);

            var entry = new ToDoListEntry(phrase.toFirstGroup(), phrase.getRequestId(), phrase.index());
            phrase.setToDoLink(entry.toString());

            return jedis.hset(generateSignature(phrase.toFirstGroup()), entry.toString(), Status.Pending.status);
        }

        return NOT_EXECUTED;
    }

    /**
     * Completes a TO-DO task.
     * @param phrase
     * @return 1 if successful
     */
    public long completeTask(ArchitectureFirstPhrase phrase) {
        if (isEnabled()) {
            ack.recordAcknowledgement(phrase);
            jedis.hdel(generateSignature(phrase.toFirstGroup()), phrase.getToDoLink());  // delete if not owned
            var entry = new ToDoListEntry(phrase.getTarget().get().name(), phrase.getRequestId(), phrase.index());
            return jedis.hdel(generateSignature(phrase.toFirstGroup()), entry.toString());
        }

        return NOT_EXECUTED;
    }

    /**
     * Fails a TO-DO task.
     * @param phrase
     * @return 1 if successful
     */
    public long failTask(ArchitectureFirstPhrase phrase) {
        if (isEnabled()) {
            return jedis.hset(generateSignature(phrase.toFirstGroup()), phrase.getToDoLink(), Status.Failed.status);
        }

        return NOT_EXECUTED;
    }

    /**
     * Rassigns a TO-DO task
     * @param group
     * @param key
     * @return 1 if successful
     */
    public long reassignTask(String group, String key) {
        if (isEnabled()) {
            return jedis.hset(generateSignature(group), key, Status.Pending.status);
        }

        return NOT_EXECUTED;
    }

    /**
     * Closes a TO-DO task
     * @param group
     * @param key
     * @return 1 if successful
     */
    public long closeTask(String group, String key) {
        if (isEnabled()) {
            return jedis.hdel(generateSignature(group), key);
        }

        return NOT_EXECUTED;
    }

    // Note: has side effects
    public Optional<ArchitectureFirstPhrase> acquireAvailableTask(String group, String requestor) {
        if (isEnabled()) {
            AtomicReference<ToDoListEntry> ref = new AtomicReference<>();

            var cursor = new JedisHCursor(jedis);
            var signature = generateSignature(group);
            cursor.processAll(signature, e -> {
                var entry = ToDoListEntry.from(e.getKey());
                if (ref.get() == null && e.getValue().equals(Status.Pending.toString())) {
                    ref.set(entry);
                    return true;
                } else if (!entry.hasOwner() || (entry.hasOwner() && !vicinity.actorIsAvailable(entry.getOwner()))) {
                    var phrase = ack.getUnacknowledgedPhrase(entry.getKey(), String.valueOf(entry.getIndex()));
                    if (phrase != null) {
                        reassignTask(entry.getGroup(), e.getKey());
                    } else {
                        closeTask(entry.getGroup(), e.getKey());
                    }
                }

                return false;
            });

            if (ref.get() != null) {
                var entry = ref.get();
                var phrase = ack.getUnacknowledgedPhrase(entry.getKey(), String.valueOf(entry.getIndex()));
                if (phrase != null) {        // there may be no phrases to process
                    if (StringUtils.isNotEmpty(entry.getOwner())) {
                        phrase.setOriginalActorName(entry.getOwner());
                    }
                    phrase.shouldAwaitResponse(false);

                    jedis.hdel(signature, entry.toString());
                    entry.setOwner(requestor);
                    jedis.hset(signature, entry.toString(), Status.InProgress.status);

                    return Optional.of(phrase);
                } else {
                    closeTask(entry.getGroup(), entry.toString());
                }

                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /**
     * Generates a TO-DO list signature by group
     * @param group
     * @return
     */
    private String generateSignature(String group) {
        return String.format("%s:%s",TO_DO_LIST, group);
    }

    private boolean isEnabled() {
        return vicinityInfo.getTodo().equals(VicinityInfo.VALUE_ENABLED);
    }
}
