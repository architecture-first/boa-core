package com.architecturefirst.boa.framework.business.vicinity.phrases;

import com.architecturefirst.boa.framework.technical.phrases.ArchitectureFirstPhrase;

/**
 * Represents an acknowledgement that a previous phrase has been received
 */
public class Acknowledgement extends ArchitectureFirstPhrase {
    private String acknowledgedPhraseName;
    private ArchitectureFirstPhrase acknowledgedPhrase;

    public Acknowledgement(Object source, String from, String to) {
        super(from, to);
    }

    /**
     * Set the phrase that is acknowledged
     * @param phrase
     * @return this
     */
    public Acknowledgement setAcknowledgementPhrase(ArchitectureFirstPhrase phrase) {
        this.acknowledgedPhrase = phrase;
        this.acknowledgedPhraseName = phrase.name();
        this.setOriginalPhrase(phrase);

        return this;
    }

    /**
     * Returns the acknowledged phrase
     * @return
     */
    public ArchitectureFirstPhrase getAcknowledgedPhrase() {return this.acknowledgedPhrase;}

    /**
     * Returns the name of the acknowledged phrase
     * @return
     */
    public String getAcknowledgedPhraseName() {
        return acknowledgedPhraseName;
    }

    /**
     * Set the name of the acknowledged phrase
     * @param acknowledgedPhraseName
     * @return
     */
    public Acknowledgement setAcknowledgedPhraseName(String acknowledgedPhraseName) {
        this.acknowledgedPhraseName = acknowledgedPhraseName;
        return this;
    }

    @Override
    public boolean requiresAcknowledgement() { return acknowledgedPhrase.requiresAcknowledgement();}
}
