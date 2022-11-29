package com.architecture1st.boa.framework.business.actors;

import com.architecture1st.boa.framework.business.vicinity.phrases.Acknowledgement;
import com.architecture1st.boa.framework.business.vicinity.phrases.AnonymousOk;
import com.architecture1st.boa.framework.technical.phrases.ArchitectureFirstPhrase;
import com.architecture1st.boa.framework.business.vicinity.phrases.Error;
import com.architecture1st.boa.framework.technical.phrases.Checkup;
import com.architecture1st.boa.framework.security.phrases.AccessRequest;
import com.architecture1st.boa.framework.security.phrases.SecurityIncident;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Represents the Security Guard in the system
 */
public interface SecurityGuard {
    static String internalToken = "";

    static final String SECURITY_GUARD = "SecurityGuard";
    static final String VICINITY_MONITOR = "VicinityMonitor";
    static final String IDENTITY_PROVIDER = "IdentityProvider";
    static final String SECURITY_CUSTOMER = "archie.t.first@boa.com";
    static final Long SECURITY_USER_ID = 201l;
    static final String SECURITY_TOKEN = "securityToken";

    static SecretKey secretKey = new SecretKey() {
        @Override
        public String getAlgorithm() {
            return null;
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }
    };

    /**
     * Returns the userid from the access token
     * @param jwtToken
     * @return userid
     */
    static Long getUserIdFromToken(String jwtToken) {
        var jws = parseToken(jwtToken);
        return ((Double)jws.getBody().get("userId")).longValue();
    }

    static String getAccessToken() {
        return internalToken;
    }

    static String getRequestId() {
        return RandomStringUtils.randomAlphanumeric(20);
    }

    /**
     * Parses the access token and returns internal claims
     * @param t
     * @return list of claims
     */
    static Jws<Claims> parseToken(String t) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(t);

        return jws;
    }

    /**
     * Determines if the event needs an access token to proceed further
     * @param phrase
     * @return true if not one of the excluded phrases
     */
    static boolean needsAnAccessToken(ArchitectureFirstPhrase phrase) {
        return !isOkToProceed(phrase);
    }

    public static List<String> NON_SECURED_PHRASES = new ArrayList<>(List.of(new String[]{
            "AccessRequest", "Acknowledgement", "AnonymousOk", "Checkup", "Error"
    }));

    /**
     * Determines if the phrase passes validation and can be sent in the Vicinity
     * @param phrase
     * @return true if the phrase is valid
     */
    static boolean isOkToProceed(ArchitectureFirstPhrase phrase) {
        if (NON_SECURED_PHRASES.contains(phrase.category())) {
            return true;
        }

        return phrase instanceof Error || phrase instanceof AccessRequest
                || phrase instanceof Checkup || phrase instanceof Acknowledgement
                || phrase instanceof AnonymousOk
                || isTokenValid(phrase.getAccessToken());
    }

    /**
     * Determines if the access token is valid
     * @param jwtToken
     * @return true if it is valid
     */
    static boolean isTokenValid(String jwtToken) {
        return validateJwtTokenFn.apply(jwtToken);
    }

    /**
     * Performs token validation
     */
    static final Function<String, Boolean> validateJwtTokenFn = (t -> {
        if (StringUtils.isEmpty(t)) {
            return false;
        }

        parseToken(t);

        return true;
    });

    /**
     * Reply to the original phrase
     * @param phrase
     * @return
     */
    static ArchitectureFirstPhrase replyToSender(ArchitectureFirstPhrase phrase) {
        Actor actor = determineTargetActor(phrase);

        var incident = new SecurityIncident(SECURITY_GUARD,  phrase.from(), phrase)
                .setAsRequiresAcknowledgement(false);

        return actor.say(incident);
    }

    /**
     * Determine actor that is targeted for the phrase
     * @param phrase
     * @return
     */
    private static Actor determineTargetActor(ArchitectureFirstPhrase phrase) {
        Actor actor = (phrase.getSource() instanceof Actor)
                ? (Actor) phrase.getSource()
                : (phrase.getTarget() != null && phrase.getTarget().isPresent())
                ? phrase.getTarget().get() : null;
        return actor;
    }

    /**
     * Report a security error for the phrase
     * @param phrase
     * @param message
     * @return
     */
    public static ArchitectureFirstPhrase reportError(ArchitectureFirstPhrase phrase, String message) {
        Actor actor = determineTargetActor(phrase);
        phrase.setHasErrors(true);

        var incident = new SecurityIncident(SECURITY_GUARD,  VICINITY_MONITOR, phrase);
        incident.setAsRequiresAcknowledgement(false);
        incident.setMessage(message);
        incident.setHasErrors(true);

        return actor.announce(incident);
    }
}
