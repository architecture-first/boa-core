package com.architecture1st.boa.framework.technical.translation;

import com.architecture1st.boa.framework.business.vicinity.messages.VicinityMessage;

public interface TranslationHandler {
    VicinityMessage convertToFormat(VicinityMessage message);
    VicinityMessage convertFromFormat(VicinityMessage message);

}
