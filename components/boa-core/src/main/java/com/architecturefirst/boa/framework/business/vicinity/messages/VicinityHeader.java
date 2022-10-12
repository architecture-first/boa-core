package com.architecturefirst.boa.framework.business.vicinity.messages;

import com.architecturefirst.boa.framework.technical.util.SimpleModel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * The header for a Vicinity message
 */
@Data
@RequiredArgsConstructor
public class VicinityHeader implements Serializable {
    private String to;
    private String from;
    private String subject;
    private String phraseType;
    private String token;
    private String area; // source area
    private String otherArea; // target area outside of source area
    private String project;
    private SimpleModel attributes;
}
