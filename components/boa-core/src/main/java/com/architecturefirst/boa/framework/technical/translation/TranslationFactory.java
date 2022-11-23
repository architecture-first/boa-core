package com.architecturefirst.boa.framework.technical.translation;

import com.architecturefirst.boa.framework.business.vicinity.messages.VicinityMessage;
import org.springframework.stereotype.Component;

@Component
public class TranslationFactory {

    TranslationHandler getTranslationHandler(VicinityMessage message) {
        try {
            var clsName = String.format("%s.%sTranslationHandler", this.getClass().getPackageName(), message.getHeader().getTranslationType());
            Class cls = Class.forName(clsName);
            return (TranslationHandler) cls.getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TranslationHandler acquireTranslationHandler(VicinityMessage message) {
        return new TranslationFactory().getTranslationHandler(message);
    }
}
