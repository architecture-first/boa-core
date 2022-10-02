package com.architecturefirst.boa.framework.business.vicinity.phrases;

import com.architecturefirst.boa.framework.business.vicinity.Vicinity;
import com.architecturefirst.boa.framework.technical.phrases.ArchitectureFirstPhrase;

/**
 * Represents an event when an Actor's connection to the Vicinity is broken and is no long able to accept phrases
 */
public class VicinityConnectionBroken extends ArchitectureFirstPhrase implements Error {

    public VicinityConnectionBroken(Object source, String from, String to) {
        super(source, "VicinityConnectionBroken", from, to);
    }

    /**
     * Set the owner Actor of the connection
     * @param owner
     * @return
     */
    public VicinityConnectionBroken setOwner(String owner) {
        this.payload().put("owner", owner);
        return this;
    }

    /**
     * Returns the owner Actor of the connection
     * @return
     */
    public String getOwner() {
        return (String) this.payload().get("owner");
    }

    /**
     * Sets the channel target for the subscription
     * @param target
     * @return
     */
    public VicinityConnectionBroken setTargetOwner(String target) {
        this.payload().put("target", target);
        return this;
    }

    /**
     * Returns the channel target for the subscription
     * @return
     */
    public String getTargetOwner() {
        return (String) this.payload().get("target");
    }

    /**
     * Sets the Vicinity used
     * @param vicinity
     * @return
     */
    public VicinityConnectionBroken setVicinity(Vicinity vicinity) {
        this.payload().put("vicinity", vicinity);
        return this;
    }

    /**
     * Returns the Vicinity used
     * @return
     */
    public Vicinity getVicinity() {
        return (Vicinity) this.payload().get("vicinity");
    }
}
