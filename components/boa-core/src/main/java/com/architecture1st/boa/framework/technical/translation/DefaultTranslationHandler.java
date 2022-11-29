package com.architecture1st.boa.framework.technical.translation;

import com.architecture1st.boa.framework.business.vicinity.messages.VicinityMessage;
import com.architecture1st.boa.framework.technical.util.TranslationUtils;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class DefaultTranslationHandler implements TranslationHandler {

    /**
     * Converts the payload of the message in place to the default compressed format
     * @param message
     * @return
     */
    public VicinityMessage convertToFormat(VicinityMessage message) {
        var jsonPayload = message.getJsonPayload();
        byte[] bytes = new Gson().fromJson(jsonPayload, byte[].class);
        jsonPayload = new String(TranslationUtils.detranslate(bytes, message.getHeader().getPayloadSize()));
        message.setJsonPayload(jsonPayload);

        return message;
    }

    /**
     * Converts the payload of the message in place from the default compressed format
     * @param message
     * @return
     */
    public VicinityMessage convertFromFormat(VicinityMessage message) {
        int originalPayloadSize = message.getJsonPayload().length();
        var compressedPayloadJson = TranslationUtils.translate(message.getJsonPayload().getBytes(StandardCharsets.UTF_8));
        message.setPayload(compressedPayloadJson, byte[].class);
        message.getHeader().setPayloadSize(originalPayloadSize);

        return message;
    }
}
