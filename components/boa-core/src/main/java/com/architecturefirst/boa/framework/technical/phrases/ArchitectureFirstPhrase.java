package com.architecturefirst.boa.framework.technical.phrases;

import com.architecturefirst.boa.framework.business.actors.Actor;
import com.architecturefirst.boa.framework.business.vicinity.exceptions.VicinityException;
import com.architecturefirst.boa.framework.business.vicinity.messages.VicinityMessage;
import com.architecturefirst.boa.framework.business.vicinity.phrases.Error;
import com.architecturefirst.boa.framework.technical.util.SimpleModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The core phrase for communication in the Vicinity and in process
 */
@Slf4j
public class ArchitectureFirstPhrase extends ApplicationEvent {
    public static final String REQUEST_ID = "requestId";
    public static final String DEFAULT_PROJECT = "default";
    public static final String ORIGINAL_PHRASE_NAME = "originalPhraseName";
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String CUSTOMER_INFO = "customerInfo";
    public static final String TOKEN = "token";
    public static final String JWT_TOKEN = "jwtToken";
    public static final String BOA_CONN = "boa-conn";
    public static final String BOA_PROJECT = "boa-project";
    public static final String BOA_TTL = "boa-ttl"; // Time to live
    public static final String AREA_NAME = "areaName";
    public static final String OTHER_AREA_NAME = "otherAreaName";
    public static final double TTL_DEFAULT_VALUE = 1;
    public static final String PHRASE_TYPE = "phraseType";
    public static String PHRASE_ALL_PARTICIPANTS = "all";
    private static final int requestIdSize = 20;

    public static String PHRASE_TYPE_ANONYMOUS_OK = "AnonymousOk";
    public static String PHRASE_TYPE_SECURED_BASIC = "SecuredBasic";

    private String name = "ArchitectureFirstPhrase";
    private String implementsType = PHRASE_TYPE_ANONYMOUS_OK;
    private SimpleModel header = new SimpleModel();
    private SimpleModel payload = new SimpleModel();
    private String message = "";
    private transient Optional<Actor> target = Optional.empty();
    private transient Gson gson;
    private transient Configuration gsonConfig;
    private transient boolean isPropagatedFromVicinity = false;
    private transient boolean isLocalPhrase = false;
    private transient boolean isAnnouncement = false;
    private transient boolean wasHandled = false;
    private transient boolean awaitResponse = false;
    private transient long awaitTimeoutSeconds = 30;
    private boolean isPipelinePhrase = false;
    private boolean hasErrors = false;
    private boolean isReply = false;
    private boolean requiresAcknowledgement = false;
    private boolean isToDoTask = false;
    private String toDoLink = "";
    private transient boolean processLaterIfNoActorFound = true;
    private String originalActorName = "";
    private String tasklist = "";
    private long index = 0;

    /**
     * Create a phrase
     * @param from
     * @param to
     * @param originalPhrase
     */
    public ArchitectureFirstPhrase(String from, List<String> to, ArchitectureFirstPhrase originalPhrase) {
        this("boa", from, to, originalPhrase);
        this.name = this.getClass().getSimpleName();
    }

    /**
     * Create a phrase
     * @param name
     * @param from
     * @param to
     * @param originalPhrase
     */
    public ArchitectureFirstPhrase(String name, String from, List<String> to, ArchitectureFirstPhrase originalPhrase) {
        super("boa");
        this.name = name;
        header.put(FROM, from);
        header.put(TO, to);
        header.put(BOA_TTL, TTL_DEFAULT_VALUE); // default to 1
        if (originalPhrase != null) {
            setOriginalPhrase(originalPhrase);
        }
        else {
            setRequestId(onGetRequestId());
            setOriginalPhraseName(name());
        }
    }

    /**
     * Create a phrase
     * @param from
     * @param to
     */
    public ArchitectureFirstPhrase(String from, List<String> to) {
        this("boa", from, to, null);
        this.name = this.getClass().getSimpleName();
    }

    /**
     * Create a phrase
     * @param name
     * @param from
     * @param to
     */
    public ArchitectureFirstPhrase(String name, String from, List<String> to) {
        this(name, from, to, null);
    }

    /**
     * Create a phrase
     * @param phraseToReplyTo
     */
    public ArchitectureFirstPhrase(ArchitectureFirstPhrase phraseToReplyTo) {
        this(phraseToReplyTo.name(), phraseToReplyTo.toFirst(), phraseToReplyTo.from(), phraseToReplyTo);
    }

    /**
     *Create a phrase
     * @param from
     * @param to
     */
    public ArchitectureFirstPhrase(String from, String to) {
        this("boa", from, new ArrayList<String>(Collections.singletonList(to)), null);
        this.name = this.getClass().getSimpleName();
    }

    /**
     *Create a phrase
     * @param name
     * @param from
     * @param to
     */
    public ArchitectureFirstPhrase(String name, String from, String to) {
        this(name, from, new ArrayList<String>(Collections.singletonList(to)), null);
    }

    /**
     * Create a phrase
     * @param from
     * @param to
     * @param originPhrase
     */
    public ArchitectureFirstPhrase(String from, String to, ArchitectureFirstPhrase originPhrase) {
        this("boa", from, new ArrayList<String>(Collections.singletonList(to)), originPhrase);
        this.name = this.getClass().getSimpleName();
    }

    /**
     * Create a phrase
     * @param name
     * @param from
     * @param to
     * @param originPhrase
     */
    public ArchitectureFirstPhrase(String name, String from, String to, ArchitectureFirstPhrase originPhrase) {
        this(name, from, new ArrayList<String>(Collections.singletonList(to)), originPhrase);
    }
    protected String onGetRequestId() {
        return RandomStringUtils.randomAlphanumeric(requestIdSize);
    }

    /**
     * Sets the header information
     * @param header
     * @return this phrase
     */
    public ArchitectureFirstPhrase setHeader(SimpleModel header) {
        this.header.putAll(header);
        return this;
    }

    /**
     * Sets the payload information
     * @param payload
     * @return this phrase
     */
    public ArchitectureFirstPhrase setPayload(SimpleModel payload) {
        this.payload.putAll(payload);
        return this;
    }

    /**
     * Set the Actor that the phrase targets once it arrives in the desired process
     * @param target
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setTargetActor(Actor target) {
        this.target = Optional.of(target);
        return this;
    }

    /**
     * Clear the target entry
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase resetTargetActor() {
        this.target = Optional.empty();
        return this;
    }

    /**
     * Returns the target actor
     * @return Optional
     */
    public Optional<Actor> getTarget() {return target;}

    /**
     * Returns whether there is a target actor
     * @return boolean - true if a target actor exists
     */
    public boolean hasTargetActor() {return target != null && target.isPresent();}

    /**
     * Returns the phrase type
     * @return
     */
    public String phraseType() {return (String) header.get(PHRASE_TYPE);}


    /**
     * Sets the category of the phrase
     * @param phraseType
     * @return
     */
    public ArchitectureFirstPhrase setPhraseType(String phraseType) {
        header.put(PHRASE_TYPE, phraseType);
        return this;
    }

    /**
     * Returns the category of the phrase
     * @return
     */
    public String category() {return implementsType;}

    /**
     * Sets the category of the phrase
     * @param implementsType
     * @return
     */
    public ArchitectureFirstPhrase setCategory(String implementsType) {
        this.implementsType = implementsType;
        return this;
    }

    /**
     * Sets the phrase as secured
     * @return
     */
    public ArchitectureFirstPhrase setAsSecured() {
        this.implementsType = PHRASE_TYPE_SECURED_BASIC;
        return this;
    }

    /**
     * Returns whether the phrase is secured
     * @return
     */
    public boolean isSecured() {
        return this.implementsType.equals(PHRASE_TYPE_SECURED_BASIC);
    }

    /**
     * Returns whether the phrase is a certain name
     * @return
     */
    public boolean isNamed(String name) {return this.name.equals(name) || this.getClass().getSimpleName().equals(name);}


    /**
     * Sets the name of the phrase
     * @param name
     * @return
     */
    public ArchitectureFirstPhrase setName(String name) {
        this.name = name;
        return this;
    }
    public boolean isType(Type type) {
        return this.implementsType.equals(type.getTypeName());
    }

    /**
     * Returns whether the name of the phrase matches the argument
     * @param nameOfPhrase
     * @return true if the name matches the argument
     */
    public boolean is(String nameOfPhrase) {return StringUtils.isNotEmpty(name) ? name.equals(nameOfPhrase): false;}

    /**
     * Returns whether the event is for all actors in an area
     * @return true if the event is for all actors in an area
     */
    public boolean isForAll() {return PHRASE_ALL_PARTICIPANTS.equals(toFirst());}


    /**
     * Returns the name of the phrase
     * @return
     */
    public String name() {return StringUtils.isNotEmpty(name) ? name : getClass().getSimpleName();}

    /**
     * Returns the area the phrase is in
     * @return
     */
    public String area() {return (String) this.header.get(AREA_NAME);}


    /**
     * Sets the name of the area
     * @param name
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setArea(String name) {
        this.header.put(AREA_NAME, name);
        return this;
    }

    /**
     * Returns the other area the phrase will be heard in
     * @return
     */
    public String otherArea() {return (String) this.header.get(OTHER_AREA_NAME);}


    /**
     * Sets the name of the other area the phrase will be heard in
     * @param name
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setOtherArea(String name) {
        this.header.put(OTHER_AREA_NAME, name);
        return this;
    }

    /**
     * Returns the subject of the phrase
     * @return
     */
    public String subject() {return name().replace("Phrase","");}

    /**
     * Sets the optional project. Default is 'default'
     * @param project
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setProject(String project) {
        this.header().put(BOA_PROJECT, project);
        return this;
    }

    /**
     * Returns the project
     * @return
     */
    public String project() {return (String) this.header().get(BOA_PROJECT);}

    /**
     * Returns the source of the phrase
     * @return
     */
    public String from() {return (String) header.get(FROM);}

    /**
     * Returns the target names and/or groups of the phrase
     * @return
     */
    public List<String> to() {return (List<String>) header.get(TO);}

    /**
     * Returns the first target name or group
     * @return
     */
    public String toFirst() {return ((List<String>) header.get(TO)).get(0);}

    /**
     * Returns the first group
     * @return
     */
    public String toFirstGroup() {return ((List<String>) header.get(TO)).get(0).split("\\.")[0];}

    /**
     * Sets the target name or group
     * @param name
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setTo(String name) {
        header.put(TO, new ArrayList<String>());
        ((List<String>) header.get(TO)).add(name);
        return this;
    }

    /**
     * Sets the source name
     * @param name
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setFrom(String name) {
        header.put(FROM, name);
        return this;
    }

    /**
     * Sets the Time to Live.
     * @param ttlValue
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setTTL(Integer ttlValue) {
        var val = ttlValue.doubleValue();
        header.put(BOA_TTL, val);
        return this;
    }

    /**
     * Returns the source
     * @return
     */
    public Integer ttl() {
        var val = (Double) header.get(BOA_TTL);
        return val.intValue();
    }

    /**
     * Determines if TTL is the default value for an area
     * @return true if TTL is the default value of 1
     */
    public boolean isDefaultTTL() {
        return ttl() == 1;
    }


    /**
     * Reduces the Time to Live by 1
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase reduceTTL() {
        return reduceTTL(1);
    }


    /**
     * Reduces the Time to Live to 0
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase endTTL() {
        return reduceTTL(-ttl());
    }

    /**
     * Reduces the Time to Live by the desired amount
     * @param ttlReductionAmount
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase reduceTTL(int ttlReductionAmount) {
        var ttl = ttl();
        ttl -= ttlReductionAmount;
        if (ttl < 0) ttl = 0;

        setTTL(ttl);
        return this;
    }

    /**
     * Sets the source.
     * @param name
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setSource(String name) {
        source = name;
        return this;
    }

    /**
     * Returns the source
     * @return
     */
    public Object source() {return source;}

    /**
     * Returns the header
     * @return
     */
    public SimpleModel header() {if (header == null) {header = new SimpleModel();} return header;}

    /**
     * Returns the payload
     * @return
     */
    public SimpleModel payload() {if (payload == null) {payload = new SimpleModel();} return payload;}

    /**
     * Set true if the phrase has arrived external to the process via the Vicinity
     * @param status
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setPropagatedFromVicinity(boolean status) {
        isPropagatedFromVicinity = status;
        return this;
    }

    /**
     * Returns if the phrase has arrived external to the process via the Vicinity
     * @return
     */
    public boolean isPropagatedFromVicinity() {return isPropagatedFromVicinity;}

    /**
     * Sets the phrase as one that will not be sent through the Vicinity and will stay in process
     * @param status
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setAsLocal(boolean status) {
        isLocalPhrase = status;
        return this;
    }

    /**
     * Returns if the phrase will not be sent through the Vicinity and will stay in process
     * @return
     */
    public boolean isLocal() {return isLocalPhrase;}

    /**
     * Sets the phrase as an announcement type that will be sent to a group of Actors
     * @param status
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setAsAnnouncement(boolean status) {
        isAnnouncement = status;
        return this;
    }

    /**
     * Returns if the phrase is an announcement type
     * @return
     */
    public boolean isAnnouncement() {return isAnnouncement;}

    /**
     * Sets the phrase as handled so it is no longer propagated
     * @param status
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setAsHandled(boolean status) {
        wasHandled = status;
        return this;
    }

    /**
     * Returns if the phrase was handled
     * @return
     */
    public boolean wasHandled() {return wasHandled;}

    /**
     * Sets the phrase as a pipeline phrase for dynamic processing
     * @param status
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setAsPipelinePhrase(boolean status) {
        isPipelinePhrase = status;
        return this;
    }

    /**
     * Returns if the phrase is a pipeline phrase
     * @return
     */
    public boolean isPipelinePhrase() {return isPipelinePhrase;}

    /**
     * Sets the task as a TO-DO task for processing later
     * @param status
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setAsToDoTask(boolean status) {
        isToDoTask = status;
        return this;
    }

    /**
     * Returns if the task is a TO-DO task
     * @return
     */
    public boolean isToDoTask() {return isToDoTask;}

    /**
     * Sets a link between the TO-DO task and the phrase
     * @param toDoLink
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setToDoLink(String toDoLink) {
        this.toDoLink = toDoLink;
        return this;
    }

    /**
     * Returns the TO-DO task link
     * @return TO-DO link string
     */
    public String getToDoLink() {return toDoLink;}

    /**
     * Set if should process later or allow phrase to be unhandled
     * @param status true if should process later
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setAsProcessLaterIfNoActorFound(boolean status) {
        processLaterIfNoActorFound = status;
        return this;
    }

    /**
     * Returns process later status
     * @return true if should process later
     */
    public boolean shouldProcessLaterIfNoActorFound() {return processLaterIfNoActorFound;}

    /**
     * Set the original Actor name for tracking
     * @param name
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setOriginalActorName(String name) {
        originalActorName = name;
        return this;
    }

    /**
     * Returns the original actor name for tracking
     * @return
     */
    public String originalActorName() {return originalActorName;}

    /**
     * Set the task list name
     * @param name
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setTasklist(String name) {
        tasklist = name;
        return this;
    }

    /**
     * Returns the associated task list name
     * @return
     */
    public String tasklist() {return tasklist;}

    /**
     * Sets the mode as reply
     * @param status
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setIsReply(boolean status) {
        isReply = status;
        return this;
    }

    /**
     * Returns if the mode is reply
     * @return true if is a reply
     */
    public boolean isReply() {return isReply;}

    /**
     * Sets the phrase to require acknowledgement
     * @param status
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setAsRequiresAcknowledgement(boolean status) {
        requiresAcknowledgement = status;
        return this;
    }

    /**
     * Returns status for requiring acknowledgment
     * @return true if requires acknowledgement
     */
    public boolean requiresAcknowledgement() {return requiresAcknowledgement;}

    /**
     * Sets whether the phrase or related processing has errors
     * @param status
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setHasErrors(boolean status) {
        hasErrors = status;
        return this;
    }

    /**
     * Returns whether the phrase or related processing has errors
     * @return true if the phrase or related processing has errors
     */
    public boolean hasErrors() {return hasErrors;}

    /**
     * Returns whether the phrase or related processing has errors
     * @return true if the phrase or related processing has errors
     */
    public boolean isErrorPhrase() {return "Error".equals(implementsType) || this instanceof Error;}

    /**
     * Returns the index that the phrase is by order in the UnAck (unacknowledged) or Ack (acknowledged) phrase list
     * @return index
     */
    public long index() {
        return index;
    }

    /**
     * Sets the index that the phrase is by order in the UnAck (unacknowledged) or Ack (acknowledged) phrase list
     */
    public void setIndex(long index) {
        this.index = index;
    }

    /**
     * Returns whether the caller will await response of this phrase for callback purposes
     * @return true if should await response
     */
    public boolean awaitResponse() {return awaitResponse;}

    /**
     * Sets whether the caller will await response of this phrase for callback purposes
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase shouldAwaitResponse(boolean status) {
        this.awaitResponse = status;
        return this;
    }

    /**
     * Returns the duration of time in seconds the caller will await response of this phrase
     * @return the length of time in seconds to wait
     */
    public long awaitTimeoutSeconds() {return this.awaitTimeoutSeconds;}

    /**
     * Sets the duration of time in seconds the caller will await response of this phrase
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setAwaitTimeoutSeconds(long seconds) {
        this.awaitTimeoutSeconds = seconds;
        return this;
    }

    /**
     * Sets the access token
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setAccessToken(String jwtToken) {header.put(JWT_TOKEN, jwtToken); return this;}

    /**
     * Returns the access token
     * @return access token
     */
    public String getAccessToken() {return (String) header.get(JWT_TOKEN);}

    /**
     * Returns whether the phrase contains a token
     * @return access token
     */
    public Boolean hasAccessToken() {return header.containsKey(JWT_TOKEN) && header.get(JWT_TOKEN) != null;}

    /**
     * Sets the processed access token, which is post validation
     */
    public void setProcessedJwtToken(String jwtToken) {((Map<String,Object>)payload.get(TOKEN)).put(TOKEN, jwtToken);}

    /**
     * Returns the processed access token, which is post validation
     * @return processed access token
     */
    public String getProcessedJwtToken() {return (String) ((Map<String,Object>)payload.get(TOKEN)).get(TOKEN);}

    /**
     * Adds payload for the phrase
     * @param payload
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase addPayload(SimpleModel payload) {this.payload = payload; return this;}

    /**
     * Adds a header entry
     * @param key
     * @param value
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase addHeader(String key, String value) {
        this.header().put(key, value);
        return this;
    }


    /**
     * Returns the contained message
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets a message
     * @param message
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Returns a reply form of the phrase
     * @param from
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase reply(String from) {
        this.setTo(from());
        header.put(FROM, from);
        isPropagatedFromVicinity = false;
        isLocalPhrase = false;
        wasHandled = false;
        return this;
    }

    /**
     * Returns whether a request id exists
     * @return
     */
    public boolean hasRequestId() {return StringUtils.isNotEmpty((String) this.header().get(REQUEST_ID));}

    /**
     * Sets the request id
     * @param requestId
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setRequestId(String requestId) {
        if (StringUtils.isNotEmpty(requestId)) {
            this.header().put(REQUEST_ID, requestId);
        }
        return this;
    }

    /**
     * Returns whether the original phrase name exists
     * @return
     */
    public boolean hasOriginalPhraseName() {return StringUtils.isNotEmpty((String) this.header().get(ORIGINAL_PHRASE_NAME));}

    /**
     * Sets the original phrase name
     * @param name
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setOriginalPhraseName(String name) {
        if (StringUtils.isNotEmpty(name)) {
            this.header().put(ORIGINAL_PHRASE_NAME, name);
        }
        return this;
    }

    /**
     * Returns the original phrase name exists
     * @return
     */
    public String originalPhraseName() {return (String) this.header().get(ORIGINAL_PHRASE_NAME);}

    /**
     * Sets the original phrase for tracking
     * @param originalPhrase
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase setOriginalPhrase(ArchitectureFirstPhrase originalPhrase) {
        setRequestId(originalPhrase.getRequestId());
        setOriginalPhraseName(StringUtils.isNotEmpty(originalPhrase.originalPhraseName()) ? originalPhrase.originalPhraseName(): originalPhrase.name());
        return initArchitectureFirstPhrase(originalPhrase);
    }

    private ArchitectureFirstPhrase initArchitectureFirstPhrase(ArchitectureFirstPhrase originalPhrase) {
        setRequestId(originalPhrase.getRequestId());
        setAccessToken(originalPhrase.getAccessToken());
        if (originalPhrase.header().containsKey(BOA_CONN)) {
            addHeader(BOA_CONN, (String) originalPhrase.header().get(BOA_CONN));
        }
        if (originalPhrase.header().containsKey(BOA_PROJECT)) {
            addHeader(BOA_PROJECT, (String) originalPhrase.header().get(BOA_PROJECT));
        }
        return this;
    }

    /**
     * Initialize the phrase based on the default phrase
     * @param defaultLocalPhrase
     * @return ArchitectureFirstPhrase
     */
    public ArchitectureFirstPhrase initFromDefaultPhrase(ArchitectureFirstPhrase defaultLocalPhrase) {
        setRequestId(defaultLocalPhrase.getRequestId());
        return initArchitectureFirstPhrase(defaultLocalPhrase);
    }

    /**
     * Returns the request id
     * @return
     */
    public String getRequestId() {return (String) this.header().get(REQUEST_ID);}

    /**
     * Convert this object to JSON form
     * @return
     */
    private String getCurrentJson() {
        if (gson == null) {
            initGson();
        }
        return gson.toJson(this);
    }

    private void initGson() {
        gson = new Gson();
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        gsonConfig = Configuration
                .builder()
                .mappingProvider(new JacksonMappingProvider(om))
                .jsonProvider(new JacksonJsonProvider(om))
                .build();
    }

    /**
     * Returns an Object from a JSON string
     * @param jsonPath - JSON Path
     * @param classType
     * @return
     */
    public Object getValueAs(String jsonPath, Type classType) {
        if (gson == null) {
            initGson();
        }

        var currentJson = getCurrentJson();    // convert current object
        var results = JsonPath.using(gsonConfig).parse(currentJson).read(jsonPath, (Class<? extends Object>) classType);

        return results;
    }


    /**
     * Returns an Object from a JSON string
     * @param jsonPath - JSON Path
     * @return
     */
    public Object getValue(String jsonPath) {
        return getValueAs(jsonPath, Object.class);
    }

    /**
     * Add a value to the header
     * @param name
     * @param value
     * @return
     */
    public ArchitectureFirstPhrase setHeaderValue(String name, Object value) {
        header().put(name, value);
        return this;
    }

    /**
     * Returns a header entry
     * @param root
     * @param classType
     * @return
     */
    public Object getHeaderValueAs(String root, Type classType) {
        return getValueAs("$.header." + root, classType);
    }

    /**
     * Returns a header value
     * @param root
     * @return
     */
    public Object getHeaderValue(String root) {
        return getHeaderValueAs(root, Object.class);
    }

    /**
     * Adds a payload value
     * @param name
     * @param value
     * @return
     */
    public ArchitectureFirstPhrase setPayloadValue(String name, Object value) {
        payload().put(name, value);
        return this;
    }

    /**
     * Returns a payload entry
     * @param root
     * @param classType
     * @return
     */
    public Object getPayloadValueAs(String root, Type classType) {
        return getValueAs("$.payload." + root, classType);
    }

    /**
     * Returns a payload entry
     * @param root
     * @param classType - should be a list type, such as new TypeToken<List<Product>>(){}.getType()
     * @return
     */
    public Object getPayloadListValueAs(String root, Type classType) {
        var rawList = getPayloadValue(root);
        var json = new Gson().toJson(rawList);
        return gson.fromJson(json, classType);
    }

    /**
     * Returns a payload entry
     * @param root
     * @return
     */
    public Object getPayloadValue(String root) {
        return getPayloadValueAs(root, Object.class);
    }

    @Override
    public String toString() {
        return getCurrentJson();
    }

    // Dynamic methods (start)
    private transient HashMap<String, BiFunction<ArchitectureFirstPhrase, Object, ArchitectureFirstPhrase>> fnSetters = new HashMap();
    public ArchitectureFirstPhrase addSetterFunction(String name, BiFunction<ArchitectureFirstPhrase, Object, ArchitectureFirstPhrase> fnSetter) {
        fnSetters.put(name, fnSetter);
        return this;
    }

    public ArchitectureFirstPhrase apply(BiFunction<ArchitectureFirstPhrase, Object, ArchitectureFirstPhrase> fnSetter, Object param) {
        fnSetter.apply(this, param);
        return this;
    }
    public ArchitectureFirstPhrase apply(String name, String param) {
        fnSetters.get(name).apply(this, param);
        return this;
    }

    private transient HashMap<String, Function<ArchitectureFirstPhrase, Object>> fnGetters = new HashMap();
    public ArchitectureFirstPhrase addGetterFunction(String name, Function<ArchitectureFirstPhrase, Object> fnGetter) {
        fnGetters.put(name, fnGetter);
        return this;
    }

    public Object apply(Function<ArchitectureFirstPhrase, Object> fnGetter) {
        return fnGetter.apply(this);
    }
    public Object apply(String name) {
        return fnGetters.get(name).apply(this);
    }
    // Dynamic methods (end)

    // Lifecycle phrases (start)

    /**
     * Called when instantiated from the Vicinity
     */
    public void onVicinityInit() {
        //... override for custom behavior
    }
    // Lifecycle phrases (end)

    /**
     * Convert a Vicinity message to an ArchitectureFirstPhrase
     * @param source
     * @param message
     * @return return ArchitectureFirstPhrase object or null if error
     */
    public static ArchitectureFirstPhrase from(Object source, VicinityMessage message) {
        return from(source, message, true);
    }

    /**
     * Convert a Vicinity message to an ArchitectureFirstPhrase
     * @param source
     * @param message
     * @param convertPhraseType - true to convert to the specific concrete class specified
     * @return return ArchitectureFirstPhrase object or null if error
     */
    public static ArchitectureFirstPhrase from(Object source, VicinityMessage message, boolean convertPhraseType) {
        try {
            var phraseType = message.getHeader().getPhraseType();
            if (phraseType != null && convertPhraseType) {
                if (Character.isUpperCase(phraseType.charAt(0))) {
                    return from(message);
                }
                else {
                    var cls = Class.forName(message.getHeader().getPhraseType());
                    ArchitectureFirstPhrase phrase = new Gson().fromJson(message.getJsonPayload(), (Type) cls);
                    return phrase;
                }
            }

        } catch (Exception e) {
            log.warn("Invalid class definition: ", e);
        }

        return from(message);
    }

    /**
     * Convert a Vicinity message to a generic ArchitectureFirstPhrase
     * @param message
     * @return return ArchitectureFirstPhrase object or null if error
     */
    public static ArchitectureFirstPhrase from(VicinityMessage message) {
        try {
            ArchitectureFirstPhrase phrase = new Gson().fromJson(message.getJsonPayload(), ArchitectureFirstPhrase.class);

            return phrase;
        } catch (Exception e) {
            log.error("Invalid class definition: ", e);
        }

        return null;
    }

    /**
     * Convert an ArchitectureFirstPhrase to a specific type
     * @param inPhrase
     * @param classType
     * @return return ArchitectureFirstPhrase object or null if error
     */
    public static ArchitectureFirstPhrase from(ArchitectureFirstPhrase inPhrase, Type classType) {
        try {
            var gson = new Gson();
            String jsonPhrase = gson.toJson(inPhrase, ArchitectureFirstPhrase.class);
            ArchitectureFirstPhrase phrase = gson.fromJson(jsonPhrase, classType);

            return phrase;
        } catch (Exception e) {
            var msg = "Invalid class definition";
            log.error(msg, e);
            throw new VicinityException(e);
        }
    }

    /**
     * Returns an phrase based on an original phrase without the payload
     * @param from
     * @param originalPhrase
     * @return return ArchitectureFirstPhrase object or null if error
     */
    public static ArchitectureFirstPhrase fromForReplyWithoutPayload(String from, ArchitectureFirstPhrase originalPhrase) {
        ArchitectureFirstPhrase replyPhrase = new ArchitectureFirstPhrase(originalPhrase.name(), from, originalPhrase.from());
        replyPhrase.setOriginalPhrase(originalPhrase);

        return replyPhrase;
    }

    /**
     * Returns an phrase based on an original phrase
     * @param from
     * @param originalPhrase
     * @return return ArchitectureFirstPhrase object or null if error
     */
    public static ArchitectureFirstPhrase fromForReply(String from, ArchitectureFirstPhrase originalPhrase) {
        return fromForReply(originalPhrase.name(), originalPhrase.category(), from, originalPhrase, true);
    }


    /**
     * Returns an phrase based on an original phrase
     * @param from
     * @param name
     * @param type
     * @param originalPhrase
     * @return return ArchitectureFirstPhrase object or null if error
     */
    public static ArchitectureFirstPhrase fromForReply(String name, String type, String from, ArchitectureFirstPhrase originalPhrase, boolean includePayload) {
        ArchitectureFirstPhrase replyPhrase = fromForReplyWithoutPayload(from, originalPhrase);

        if (includePayload) {
            replyPhrase.addPayload(originalPhrase.payload());
        }
        replyPhrase.setAccessToken(originalPhrase.getAccessToken());
        replyPhrase.setName(name);
        replyPhrase.setCategory(type);

        if (originalPhrase.isSecured()) {
            replyPhrase.setAsSecured();
        }

        return replyPhrase;
    }
}
