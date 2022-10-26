package com.architecturefirst.boa.framework.technical.bulletinboard;

import com.architecturefirst.boa.framework.business.vicinity.area.ActorInArea;
import com.architecturefirst.boa.framework.business.vicinity.info.VicinityInfo;
import com.architecturefirst.boa.framework.technical.cache.JedisCursor;
import com.architecturefirst.boa.framework.technical.cache.JedisHCursor;
import com.architecturefirst.boa.framework.technical.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * The shared object for inter-actor communication.
 */
@Slf4j
@Repository
public class BulletinBoard {

    @Autowired
    private JedisPooled jedis;

    @Autowired
    private VicinityInfo vicinityInfo;

    private final String bulletinBoardConnectionId = UUID.randomUUID().toString();
    private long expirationSeconds;
    private static final String BULLETIN_BOARD_PREFIX = "boa.BulletinBoard:topic";

    @PostConstruct
    public void init() {
        log.info("bulletinBoardConnectionId: " + bulletinBoardConnectionId);
        expirationSeconds = vicinityInfo.getBulletinBoardEntryExpirationSeconds();
    }

    /**
     * Post an entry to the main bulletin board
     * @param name
     * @param value
     */
    public void post(String name, String value) {
        jedis.set(name, value);
        jedis.expire(name, expirationSeconds);
    }

    /**
     * Post an entry to a topic-based bulletin board
     * @param topic
     * @param name
     * @param value
     */
    // default to date based topics
    public void postTopic(String topic, String name, String value) {
        topic = DateUtils.appendDaily(topic);

        jedis.hset(topic, name, value);
        jedis.expire(topic, expirationSeconds);
    }

    /**
     * Post a status entry to an availability bulletin board
     * @param topic
     * @param name
     * @param value
     * @param statusString
     */
    // default to date based topics
    public void postStatusTopic(String topic, String area, String name, String value, String statusString) {
        topic = DateUtils.appendDaily(topic);

        String activeTopic = getFormat(topic, area, "Active");
        String awayTopic = getFormat(topic, area, "Away");
        String busyTopic = getFormat(topic, area, "Busy");
        String goneTopic = getFormat(topic, area, "Gone");

        switch (value) {
            case "Gone":
                jedis.hdel(activeTopic, name);
                jedis.hdel(awayTopic, name);
                jedis.hdel(busyTopic, name);
                jedis.hset(goneTopic, name, statusString);
                jedis.expire(goneTopic, expirationSeconds);
                return;
            case "Busy":
                jedis.hdel(activeTopic, name);
                jedis.hdel(awayTopic, name);
                jedis.hset(busyTopic, name, statusString);
                jedis.expire(busyTopic, expirationSeconds);
                return;
            case "Away":
                jedis.hdel(activeTopic, name);
                jedis.hdel(busyTopic, name);
                jedis.hset(awayTopic, name, statusString);
                jedis.expire(awayTopic, expirationSeconds);
                return;
        }
        jedis.hset(activeTopic, name, statusString);
        jedis.expire(activeTopic, expirationSeconds);

        clearIdleTopicEntries(activeTopic, awayTopic);
    }

    private String getFormat(String topic, String area, String status) {
        return String.format("%s/%s/%s/%s",BULLETIN_BOARD_PREFIX, area, topic, status);
    }

    /**
     * Remove idle entries that have not been updated recently
     * @param activeTopic
     * @param awayTopic
     */
    public void clearIdleTopicEntries(String activeTopic, String awayTopic) {
        var idleEntries = new HashMap<String,String>();

        var cursor = new JedisHCursor(jedis);
        cursor.processAll(activeTopic, e -> {
            var entry = BulletinBoardStatus.from(e.getValue());
            if (entry.getTimestamp().isBefore(ZonedDateTime.now(ZoneId.of("GMT")).minus(2, ChronoUnit.MINUTES))) {
                idleEntries.put(e.getKey(), e.getValue());
            }

            return false;
        });

        idleEntries.entrySet().forEach(e -> {
            jedis.hdel(activeTopic, e.getKey());
            jedis.hset(awayTopic, e.getKey(), e.getValue());
            jedis.expire(awayTopic, expirationSeconds);
        });

    }

    /**
     * Return active bulletin boards
     */
    public List<String> getActiveBulletinBoards(String area) {
        var activeTopic = String.format("%s/%s/VicinityStatus*/Active", BULLETIN_BOARD_PREFIX, area);
        var bulletinBoards = new ArrayList<String>();

        var cursor = new JedisCursor(jedis);
        cursor.processAll(activeTopic, e -> {
            bulletinBoards.add(e);
            return false;
        });

        return bulletinBoards;
    }

    /**
     * Return available actors in an area
     */
    public List<ActorInArea> getAvailableActors(String area, String project) {
        var activeActors = new ArrayList<ActorInArea>();

        var bulletinBoards = getActiveBulletinBoards(area);

        bulletinBoards.forEach(topic -> {
            var cursor = new JedisHCursor(jedis);
            cursor.processAll(topic, e -> {
                var actorInfo = e.getValue();
                var actorName = e.getKey();
                if (actorInfo.contains("\"message\":\"running\"") && actorName.contains(project)) {
                    activeActors.add(new ActorInArea(area, actorName));
                }

                return false;
            });
        });

        return activeActors.stream().distinct().toList();
    }

    /**
     * Determine which Actor should do the next task based on bulletin board status
     * @param topic
     * @return
     */
    public String whosTurnIsIt(String topic) {
        topic = DateUtils.appendDaily(topic);

        String activeTopic = BULLETIN_BOARD_PREFIX + topic + "/Active";
        if (jedis.exists(activeTopic)) {
            return jedis.hrandfield(activeTopic);
        }

        return "";
    }

    /**
     * Read an entry from the main bulletin board
     * @param name
     * @return
     */
    public String read(String name) {
        return jedis.get(name);
    }

    /**
     * Read an entry from a daily topic-related bulletin board
     * @param topic
     * @param name
     * @return
     */
    public String readTopicEntry(String topic, String name) {
        topic = DateUtils.appendDaily(topic);
        return jedis.hget(topic, name);
    }

    /**
     * Read a random entry from a daily topic-related bulletin board
     * @param topic
     * @return
     */
    public String readRandomTopicEntry(String topic) {
        topic = DateUtils.appendDaily(topic);
        if (jedis.exists(topic)) {
            String key = jedis.hrandfield(topic);
            return jedis.hget(topic, key);
        }

        return "";
    }

    /**
     * Read all entries from a daily topic-related bulletin board
     * @param topic
     * @param name
     * @return
     */
    public Map<String,String> readTopicEntries(String topic, String name) {
        topic = DateUtils.appendDaily(topic);
        return jedis.hgetAll(topic);
    }

    /**
     * Determines if the bulletin board is healthy
     * @return true if the bulletin board can make a simple update
     */
    public boolean isOk() {
        try {
            String bulletinboardPath = "boa.environment/health/bulletinboard";
            jedis.hset(bulletinboardPath, "BB" + bulletinBoardConnectionId, ZonedDateTime.now(ZoneId.of("GMT")).toString());
            jedis.expire(bulletinboardPath, expirationSeconds);
        }
        catch(Exception e) {
            log.error("Health Check Error: " + e);
            return false;
        }

        return true;
    }
}
