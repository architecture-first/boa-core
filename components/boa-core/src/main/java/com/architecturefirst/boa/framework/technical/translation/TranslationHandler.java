package com.architecturefirst.boa.framework.technical.translation;

import com.architecturefirst.boa.framework.business.vicinity.messages.VicinityMessage;

public interface TranslationHandler {
    VicinityMessage convertToFormat(VicinityMessage message);
    VicinityMessage convertFromFormat(VicinityMessage message);

}
