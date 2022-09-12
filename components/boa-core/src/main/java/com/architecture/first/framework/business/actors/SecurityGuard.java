package com.architecture.first.framework.business.actors;

import com.architecture.first.framework.business.vicinity.events.AcknowledgementEvent;
import com.architecture.first.framework.business.vicinity.events.AnonymousOkEvent;
import com.architecture.first.framework.business.vicinity.events.ErrorEvent;
import com.architecture.first.framework.security.events.AccessRequestEvent;
import com.architecture.first.framework.security.events.SecurityIncidentEvent;
import com.architecture.first.framework.technical.events.ArchitectureFirstEvent;
import com.architecture.first.framework.technical.events.CheckupEvent;
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
     * @param event
     * @return true if not one of the excluded events
     */
    static boolean needsAnAccessToken(ArchitectureFirstEvent event) {
        return !isOkToProceed(event);
    }

    public static List<String> NON_SECURED_EVENTS = new ArrayList<>(List.of(new String[]{
            "AccessRequestEvent", "AcknowledgementEvent", "AnonymousOkEvent", "CheckupEvent", "ErrorEvent"
    }));

    /**
     * Determines if the event passes validation and can be sent in the Vicinity
     * @param event
     * @return true if the event is valid
     */
    static boolean isOkToProceed(ArchitectureFirstEvent event) {
        if (NON_SECURED_EVENTS.contains(event.type())) {
            return true;
        }

        return event instanceof ErrorEvent || event instanceof AccessRequestEvent
                || event instanceof CheckupEvent || event instanceof AcknowledgementEvent
                || event instanceof AnonymousOkEvent
                || isTokenValid(event.getAccessToken());
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
     * Reply to the original event
     * @param event
     * @return
     */
    static ArchitectureFirstEvent replyToSender(ArchitectureFirstEvent event) {
        Actor actor = determineTargetActor(event);

        var incident = new SecurityIncidentEvent(actor, SECURITY_GUARD,  event.from(), event)
                .setAsRequiresAcknowledgement(false);

        return actor.say(incident);
    }

    /**
     * Determine actor that is targeted for the event
     * @param event
     * @return
     */
    private static Actor determineTargetActor(ArchitectureFirstEvent event) {
        Actor actor = (event.getSource() instanceof Actor)
                ? (Actor) event.getSource()
                : (event.getTarget() != null && event.getTarget().isPresent())
                ? event.getTarget().get() : null;
        return actor;
    }

    /**
     * Report a security error for the event
     * @param event
     * @param message
     * @return
     */
    public static ArchitectureFirstEvent reportError(ArchitectureFirstEvent event, String message) {
        Actor actor = determineTargetActor(event);
        event.setHasErrors(true);

        var incident = new SecurityIncidentEvent(actor, SECURITY_GUARD,  VICINITY_MONITOR, event);
        incident.setAsRequiresAcknowledgement(false);
        incident.setMessage(message);
        incident.setHasErrors(true);

        return actor.announce(incident);
    }
}
