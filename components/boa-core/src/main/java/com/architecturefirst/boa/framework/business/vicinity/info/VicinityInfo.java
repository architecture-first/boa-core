package com.architecturefirst.boa.framework.business.vicinity.info;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;

import javax.annotation.PostConstruct;
import java.util.function.Function;

/**
 * Contains information for the particular Vicinity
 */
@Data
@Component
public class VicinityInfo {

    public static final String BOA_VICINITY_INFO = "boa.vicinity.info";
    public static final String VALUE_VICINITY_NAME = "name";
    public static final String VALUE_VICINITY_ENV_TO_DO = "env.to-do";
    public static final String VALUE_VICINITY_ENV_ACKNOWLEDGEMENT = "env.acknowledgement";
    public static final String VALUE_VICINITY_ENV_ACTOR_ENTERED_PHRASE = "env.actor-entered-phrase";

    public static final String VALUE_VICINITY_ENV_BULLETIN_BOARD_ENTRY_EXPIRATION_SECONDS = "env.bulletin.board.entry.expiration.seconds";
    public static final String VALUE_VICINITY_ENV_VAULT_EXPIRATION_SECONDS = "env.vault.expiration.seconds";
    public static final String VALUE_VICINITY_ENV_TASK_LIST_SECONDS = "env.task-list.expiration.seconds";
    public static final String VALUE_VICINITY_ENV_ACKNOWLEDGEMENT_SECONDS = "env.acknowledgement.expiration.seconds";
    public static final String VALUE_VICINITY_ENV_CONVERSATION_SECONDS = "env.conversation.expiration.seconds";

    public static String VALUE_ENABLED = "enabled";
    public static String VALUE_DISABLED = "disabled";

    private static boolean wasInitRun = false;

    @Autowired
    private JedisPooled jedis;

    public VicinityInfo() {}

    /**
     * Initializes the Vicinity
     */
    @PostConstruct
    protected void init() {
    }

    /**
     * Gets the Vicinity name
     * @return Vicinity name
     */
    public String getVicinityName() {
        return jedis.hget(BOA_VICINITY_INFO,VALUE_VICINITY_NAME);
    }

    /**
     * Gets the Vicinity TODO status
     * @return TODO status
     */
    public boolean isTODOEnabled() {
        return VALUE_ENABLED.equals(jedis.hget(BOA_VICINITY_INFO,VALUE_VICINITY_ENV_TO_DO));
    }

    /**
     * Gets the Vicinity acknowledgement status
     * @return acknowledgement status
     */
    public boolean isAcknowledgementEnabled() {
        return VALUE_ENABLED.equals(jedis.hget(BOA_VICINITY_INFO,VALUE_VICINITY_ENV_ACKNOWLEDGEMENT));
    }

    /**
     * Gets the Vicinity entered-phrase status
     * @return entered-phrase status
     */
    public boolean isActorEnteredPhraseEnabled() {
        return VALUE_ENABLED.equals(jedis.hget(BOA_VICINITY_INFO,VALUE_VICINITY_ENV_ACTOR_ENTERED_PHRASE));
    }

    /**
     * The number of expiration seconds for a bulletin board entry
     * @return expiration seconds
     */
    public long getBulletinBoardEntryExpirationSeconds() {
        return Long.parseLong(jedis.hget(BOA_VICINITY_INFO,VALUE_VICINITY_ENV_BULLETIN_BOARD_ENTRY_EXPIRATION_SECONDS));
    }

    /**
     * The number of expiration seconds for the vault
     * @return expiration seconds
     */
    public long getVaultExpirationSeconds() {
        return Long.parseLong(jedis.hget(BOA_VICINITY_INFO,VALUE_VICINITY_ENV_VAULT_EXPIRATION_SECONDS));
    }

    /**
     * The number of expiration seconds for the task list
     * @return expiration seconds
     */
    public long getTaskListExpirationSeconds() {
        return Long.parseLong(jedis.hget(BOA_VICINITY_INFO,VALUE_VICINITY_ENV_VAULT_EXPIRATION_SECONDS));
    }

    /**
     * The number of expiration seconds for an acknowledgement
     * @return expiration seconds
     */
    public long getAcknowledgementExpirationSeconds() {
        return Long.parseLong(jedis.hget(BOA_VICINITY_INFO,VALUE_VICINITY_ENV_VAULT_EXPIRATION_SECONDS));
    }

    /**
     * The number of expiration seconds for a conversation
     * @return expiration seconds
     */
    public long getConversationExpirationSeconds() {
        return Long.parseLong(jedis.hget(BOA_VICINITY_INFO,VALUE_VICINITY_ENV_CONVERSATION_SECONDS));
    }
}
