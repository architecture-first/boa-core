package com.architecturefirst.boa.framework.business.vicinity.area;

import com.architecturefirst.boa.framework.business.actors.Actor;
import com.architecturefirst.boa.framework.business.vicinity.info.VicinityInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an area of a vicinity
 */
@Component
public class Area {

    public static final String AREA_NAME = "name";

    public static final String VALUE_AREA_NAME_DEFAULT = "common";

    public static final String AREA_NEIGHBOR = "neighbor.area";
    public static final String AREA_ACTOR_COUNT = "actor.count";
    public static final String AREA_ACTOR_COUNT_AND = AREA_ACTOR_COUNT + ".";
    public static final String AREA_ACTOR_COUNT_TOTAL = AREA_ACTOR_COUNT_AND + "total";
    public static final String AREA_ACTOR_CAPACITY = "actor.capacity";
    public static final String AREA_ACTOR_CAPACITY_TOTAL = AREA_ACTOR_CAPACITY + ".total";
    public static final String AREA_SIGNATURE = "boa.V%$:A%s";
    public static final String AREA_CHILD_FORMAT = "%s.%s.";
    @Autowired
    private JedisPooled jedis;

    @Autowired
    private VicinityInfo vicinityInfo;

    private final String areaConnectionId = UUID.randomUUID().toString();

    /**
     * Initializes an Area based on defaults
     */
    public void init() {
        AreaConfig config = new AreaConfig();
        config.setName("Common");
        init(config);
    }

    /**
     * Initializes an Area based on the config
     * @param config
     */
    public void init(AreaConfig config) {
        String vicinityName = vicinityInfo.getVicinityName();

        // Create Area Config
        var areaName = generateSignature(config.getName(), vicinityName);

        jedis.hset(areaName, AREA_NAME, config.getName());
        jedis.hset(areaName, AREA_ACTOR_CAPACITY_TOTAL, String.valueOf(config.getTotalCapacityOfActors()));

        config.getTotalCapacityOfActorsByType().entrySet().forEach(c -> {
            jedis.hset(areaName, String.format(AREA_CHILD_FORMAT,AREA_ACTOR_CAPACITY, c.getKey()), String.valueOf(c.getValue()));
        });

        config.getNeighborAreas().entrySet().forEach(c -> {
            jedis.hset(areaName, String.format(AREA_CHILD_FORMAT,AREA_NEIGHBOR, c.getKey()), String.valueOf(c.getValue()));
        });

        // Add to Vicinity Config
        jedis.hset("boa.vicinity.areas", areaName, "Active");

    }

    /**
     * Return the reachable neighbors
     * @param area
     * @param ttl
     * @return
     */
    public List<String> getReachableNeighbors(String area, int ttl) {
        var config = jedis.hgetAll(AREA_CHILD_FORMAT);
        var list = config.entrySet().stream()
                .filter(es -> es.getKey().contains(AREA_NEIGHBOR) && Integer.parseInt(es.getValue()) <= ttl )
                .map(es -> es.getValue())
                .toList();

        return list;
    }

    /**
     * Return a combination signature
     * @param areaName
     * @param vicinityName
     * @return Signature
     */
    private String generateSignature(String areaName, String vicinityName) {
        return String.format(AREA_SIGNATURE, vicinityName, areaName);
    }

    /**
     * Record when an Actor enters an area
     * @param areaName
     * @param actor
     * @return true if successful recording
     */
    public boolean enter(String areaName, Actor actor) {
        String vicinityName = vicinityInfo.getVicinityName();
        var signature = generateSignature(areaName, vicinityName);
        jedis.hincrBy(signature, String.format(AREA_CHILD_FORMAT,AREA_ACTOR_COUNT_AND, actor.group()), 1);
        jedis.hincrBy(signature, AREA_ACTOR_COUNT_TOTAL, 1);

        return true;
    }

    /**
     * Record when an Actor exits an area
     * @param areaName
     * @param actor
     * @return true if successful recording
     */
    public boolean exit(String areaName, Actor actor) {
        String vicinityName = vicinityInfo.getVicinityName();
        var signature = generateSignature(areaName, vicinityName);
        jedis.hincrBy(signature, String.format(AREA_CHILD_FORMAT,AREA_ACTOR_COUNT_AND, actor.group()), -1);
        jedis.hincrBy(signature, AREA_ACTOR_COUNT_TOTAL, -1);

        return true;
    }

}
