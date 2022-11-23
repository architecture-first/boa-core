package com.architecturefirst.boa.framework.business.vicinity.messages;

import com.architecturefirst.boa.framework.technical.phrases.ArchitectureFirstPhrase;
import com.architecturefirst.boa.framework.technical.translation.TranslationFactory;
import com.architecturefirst.boa.framework.technical.util.TranslationUtils;
import com.google.gson.Gson;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * The message sent through the Vicinity between Actors
 */
@Data
@RequiredArgsConstructor
public class VicinityMessage implements Serializable {
    private VicinityHeader header = new VicinityHeader();
    private String jsonPayload = "";

    /**
     * Creates a Vicinity message
     * @param from - Actor sending the message
     * @param to - Actor to receive the message
     */
    public VicinityMessage(String from, String to) {
        header.setFrom(from);
        header.setTo(to);
    }

    /**
     * Sets the payload of the message by type
     * @param payload
     * @param classType
     * @return the message
     */
    public VicinityMessage setPayload(Object payload, Type classType) {
        header.setPhraseType(classType.getTypeName());
        jsonPayload = new Gson().toJson(payload, classType);

        return this;
    }

    /**
     * Returns who the message is from
     * @return
     */
    public String from() {
        return header.getFrom();
    }

    /**
     * Return who the message is targeted to
     * @return
     */
    public String to() {
        return header.getTo();
    }

    /**
     * Returns the token for the message
     * @return
     */
    public String token() {
        return header.getToken();
    }

    /**
     * Returns the subject of the message
     * @return
     */
    public String subject() {
        return header.getSubject();
    }


    /**
     * Returns whether the event is for all actors in an area
     * @return true if the event is for all actors in an area
     */
    public boolean isForAll() {return ArchitectureFirstPhrase.PHRASE_ALL_PARTICIPANTS.equals(to());}

    /**
     * Returns this object as a JSON string
     * @return JSON string
     */
    public String toString() {
        return new Gson().toJson(this, this.getClass());
    }

    /**
     * Builds the Vicinity message from a JSON string
     * @param jsonMessage
     * @return
     */
    public static VicinityMessage from(String jsonMessage) {
        try {
            return new Gson().fromJson(jsonMessage, VicinityMessage.class);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Generates a Vicinity message from and phrase
     * @param phrase
     * @param to
     * @return
     */
    public static VicinityMessage from(ArchitectureFirstPhrase phrase, String to) {
        VicinityMessage message = new VicinityMessage(phrase.from(), to);
        message.getHeader().setArea(phrase.area());
        message.getHeader().setProject(phrase.project());
        message.getHeader().setArea(phrase.area());
        message.getHeader().setTtl(phrase.ttl());

        message.setPayload(phrase, phrase.getClass());

        if (StringUtils.isNotEmpty(phrase.getTranslationType()) && !phrase.isCompressed()) {
            message.getHeader().setTranslationType(phrase.getTranslationType());
            var translator = TranslationFactory.acquireTranslationHandler(message);
            translator.convertFromFormat(message);
        }

        return message;
    }

    /**
     * Generates a Vicinity message from a phrase
     * @param phrase
     * @return
     */
    public static VicinityMessage from(ArchitectureFirstPhrase phrase) {
        return from(phrase, phrase.toFirst());
    }


}
