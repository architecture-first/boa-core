package com.architecture1st.boa.framework.technical.cache;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.ScanParams;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Iterates through cache entries and allows actions
 */
public class JedisCursor {
    private final JedisPooled jedis;
    private final int DEFAULT_CURSOR_START = 0;
    private final int DEFAULT_CHUNK_SIZE = 1000;
    private final String DEFAULT_MATCH_PARAMS = "*";
    private String cursor;

    public JedisCursor(JedisPooled jedis) {this.jedis = jedis;}

    /**
     * Iterates through entries
     * @param filter - Jedis compatible filter
     * @param start - starting entry
     * @param chunkSize - amount of entries returned per call
     * @param fnOnChunk - method to execute per chunk
     */
    public void processAll(String filter, int start, int chunkSize,
                           Function<String, Boolean> fnOnChunk) {
        ScanParams scanParams = new ScanParams().count(chunkSize).match(filter);
        cursor = String.valueOf(start);
        var end = String.valueOf(DEFAULT_CURSOR_START);
        AtomicBoolean hasPassed = new AtomicBoolean(false);
        do {
            var scanResult = jedis.scan(cursor, scanParams);
            scanResult.getResult().forEach(e -> {
                if (fnOnChunk.apply(e)) {
                    hasPassed.set(true);
                }
            });

            if (hasPassed.get()) {
                break;
            }
            cursor = scanResult.getCursor();
        } while (!cursor.equals(end));
    }

    /**
     * Iterates through entries
     * @param filter - Jedis compatible filter
     * @param chunkSize - amount of entries returned per call
     * @param fnOnChunk - method to execute per chunk
     */
    public void processAll(String filter, int chunkSize,
                           Function<String, Boolean> fnOnChunk) {
        processAll(filter, DEFAULT_CURSOR_START, chunkSize, fnOnChunk);
    }

    /**
     * Iterates through entries
     * @param filter - Jedis compatible filter
     * @param fnOnChunk - method to execute per chunk
     */
    public void processAll(String filter,
                           Function<String, Boolean> fnOnChunk) {
        processAll(filter, DEFAULT_CURSOR_START, 100, fnOnChunk);
    }

    /**
     * Iterates through entries
     * @param fnOnChunk - method to execute per chunk
     */
    public void processAll(Function<String, Boolean> fnOnChunk) {
        processAll(DEFAULT_MATCH_PARAMS, DEFAULT_CURSOR_START, DEFAULT_CHUNK_SIZE, fnOnChunk);
    }

}
