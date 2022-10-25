package com.architecturefirst.boa.framework.business.vicinity;

import com.architecturefirst.boa.framework.business.vicinity.area.ActorInArea;
import com.architecturefirst.boa.framework.business.vicinity.phrases.VicinityConnectionBroken;
import com.architecturefirst.boa.framework.technical.bulletinboard.BulletinBoard;
import com.architecturefirst.boa.framework.technical.threading.Connection;
import com.architecturefirst.boa.framework.business.actors.Actor;
import com.architecturefirst.boa.framework.business.actors.SecurityGuard;
import com.architecturefirst.boa.framework.business.actors.exceptions.ActorException;
import com.architecturefirst.boa.framework.business.vicinity.conversation.Conversation;
import com.architecturefirst.boa.framework.business.vicinity.exceptions.VicinityException;
import com.architecturefirst.boa.framework.business.vicinity.info.VicinityInfo;
import com.architecturefirst.boa.framework.business.vicinity.messages.VicinityMessage;
import com.architecturefirst.boa.framework.business.vicinity.threading.VicinityConnections;
import com.architecturefirst.boa.framework.security.phrases.SecurityHolder;
import com.architecturefirst.boa.framework.technical.phrases.ArchitectureFirstPhrase;
import com.architecturefirst.boa.framework.technical.phrases.Local;
import com.architecturefirst.boa.framework.technical.util.SimpleModel;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.params.ScanParams;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * The Vicinity class is the main communication vehicle between Actors.
 * It can be thought of as a virtual location in which participating Actors exist.
 *
 *         Note:
 *             The Vicinity component hides the communication from the Actor.
 *             In this case, it is using Redis pub/sub to communicate
 *             This implementation could be replaced with REST calls or Message Queues or Event Grids, etc.
 */
@Component
@Slf4j

public class VicinityProxy implements Vicinity {
    public static final int ROSTER_LIST_COUNT = 100;
    public static final int JEDIS_TIMEOUT = 60000;
    public static String METHOD_POST = "POST";
    private static String VICINITY_API_MESSAGE_SEND = "api/vicinity/message/send";

    @Autowired
    private ApplicationEventPublisher publisher;

    @Value("${vicinity.url:http://localhost:19991}")
    private String vicinityUrl;

    @Value("${vicinity.processType:client}")
    private String vicinityProcessType;

    @Autowired
    private Conversation convo;

    @Autowired
    private VicinityInfo vicinityInfo;

    @Autowired
    private BulletinBoard bulletinBoard;

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;

    private final Map<String, LinkedList<String>> workQueueMap = new HashMap<>();
    private final Map<String, Integer> currentWorkforceSize = new HashMap<>();

    private static final Map<Future, String> activeTasks = new HashMap<>();
    private final Map<String, VicinityConnections> taskGroups = new HashMap<>();

    /**
     * Creates a new thread to manage Vicinity tasks
     */
    private final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicLong threadIndex = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("a1-vicinity-" + threadIndex.getAndIncrement());
            return thread;
        }
    };

    /**
     * Executes threads in a dedicated pool
     */
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 10, 30, TimeUnit.SECONDS,
                                    new LinkedBlockingDeque<>(10),
                                    threadFactory,
                                    new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * Performs Vicinity initialization
     */
    @PostConstruct
    protected void init() {
    }

    /**
     * Gracefully shuts down resources
     */
    @PreDestroy
    private void shutdown() {
        taskGroups.values().stream().forEach(VicinityConnections::shutdown);
    }

    /**
     * Determines the Vicinity Name
     * @return the Vicinity name
     */
    public String name() {
        return vicinityInfo.getVicinityName();
    }

    /**
     * Event listener for phrases.
     * The phrases are sent to the target actor in the Vicinity if they are not local or meant to be received in the current process.
     * @param phrase
     */
    @Override
    public void onApplicationPhrase(ArchitectureFirstPhrase phrase) {

        if (!phrase.isLocal() && !(phrase instanceof Local)) { // local phrases don't leave this process
            if (!phrase.isPropagatedFromVicinity()) { // don't echo back out phrases
                if (onVicinityReceivePhrase(phrase)) {
                    if (onVicinityPhraseSecurityCheck(phrase)) {
                        try {
                            phrase.to().forEach(t -> {
                                if (StringUtils.isNotEmpty(t)) {
                                    if (phraseIsNotAlreadyAssignedToAnActor(phrase, t)) {
                                        VicinityMessage message = onVicinityBeforePublishMessage(phrase, t);

                                        var path = VICINITY_API_MESSAGE_SEND;
                                        onVicinityPublishMessage(path, message);
                                        onVicinityAfterPublishMessage(path, message);

                                        if (phrase.isErrorPhrase()) {      // send error phrases to vicinity monitor as well as the caller
                                            onVicinityError(path, phrase);
                                        }
                                    }
                                } else {
                                    onVicinityEmptyTarget(phrase);
                                }
                            });
                        } catch (Exception e) {
                            onVicinityProcessingException(e);
                        }
                    } else {
                        onVicinitySecurityGuardRejectedPhrase(phrase);
                    }
                }
            }
        }
    }

    private boolean phraseIsNotAlreadyAssignedToAnActor(ArchitectureFirstPhrase phrase, String t) {
        return !phrase.hasTargetActor() || (phrase.hasTargetActor() && !t.equals(phrase.getTarget().get().name()));
    }

    /**
     * Generates a Vicinity message from and phrase
     * @param phrase
     * @param to
     * @return
     */
    public VicinityMessage generateMessage(ArchitectureFirstPhrase phrase, String to) {
        return VicinityMessage.from(phrase, to);
    }

    /**
     * Publish a message to the Vicinity
     * @param to
     * @param contents
     */
    public void publishMessage(String to, String contents) {
        try (Jedis jedisDedicated = new Jedis(redisHost, redisPort)) {
            jedisDedicated.publish(VicinityProxy.channelFor(to), contents);
        }
    }

    /**
     * Handle an invalid token
     * @param phrase
     */
    private void processInvalidToken(ArchitectureFirstPhrase phrase) {
        String msg = "Received Invalid Token: " + new Gson().toJson(phrase);
        log.error(msg);
        SecurityGuard.reportError(phrase, msg);
        SecurityGuard.replyToSender(phrase.setMessage(msg));
    }


    /**
     * Receive phrases from the environment, such as Redis, and propagate to the intended targets
     * @param owner
     * @param target
     */
    public void subscribe(Actor owner, String target, BiFunction<Actor, ArchitectureFirstPhrase, Void> fnCallback) {
        Runnable submitTask =  () -> {
            try (Jedis jedisDedicated = new Jedis(redisHost, redisPort, JEDIS_TIMEOUT)) {
                jedisDedicated.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        super.onMessage(channel, message);

                        VicinityMessage vicinityMessage = VicinityMessage.from(message);
                        if (vicinityMessage != null) {

                            AtomicReference<String> threadId = new AtomicReference<>();
                            var future = executor.submit(() -> {
                                try {
                                    threadId.set(Thread.currentThread().getName());
                                    ArchitectureFirstPhrase phrase = ArchitectureFirstPhrase.from(this, vicinityMessage);
                                    if (phrase != null) {
                                        phrase.setPropagatedFromVicinity(true);
                                        phrase.shouldAwaitResponse(false);  // this flag is for the caller not recipients
                                        phrase.onVicinityInit();
                                        log.info("Received and Locally Published Event: " + new Gson().toJson(phrase));
                                        convo.record(phrase, Conversation.Status.ReceivedInVicinity);

                                        if (SecurityGuard.isOkToProceed(phrase)) {
                                            phrase.setAsLocal(false).setAsHandled(false);
                                            if ("server".equals(vicinityProcessType)) {
                                                phrase.setPropagatedFromVicinity(false);
                                            }
                                            if (owner.isSecurityGuard()) {
                                                publisher.publishEvent(new SecurityHolder(phrase));
                                            }
                                            else {
                                                publisher.publishEvent(phrase);
                                            }
                                        }
                                        else {
                                            processInvalidToken(phrase);
                                        }
                                    } else {
                                        owner.onError("Vicinity Message is not readable as an ArchitectureFirstEvent: " + vicinityMessage);
                                    }
                                }
                                catch (Exception e) {
                                    owner.onException(new ActorException(owner, e), "Error processing phrase: ");
                                }
                                finally {
                                    activeTasks.remove(this);
                                }
                            });
                            activeTasks.put(future, "running");

                        }
                        else {
                            owner.onError("Original message is not readable as a VicinityMessage: " + message);
                        }
                    }
                }, channelFor(target));
            }
            catch(Exception e) {
                var evt =  new VicinityConnectionBroken("vicinity", owner.name())
                        .setOwner(owner.name())
                        .setTargetOwner(target)
                        .setVicinity(this)
                        .setTargetActor(owner);
                owner.onException(evt, new ActorException(owner, e), "Vicinity Error:");
                publisher.publishEvent(evt);
            }
        };

        log.info("Subscription to: " + channelFor(target));

        setupTask(target, submitTask);
    }

    /**
     * Unsubscribe from the phrase subscription
     * @param target
     */
    public void unsubscribe(String target) {
        if (taskGroups.containsKey(target)) {
            taskGroups.get(target).shutdown();
        }
    }

    /**
     * Determines if the subscription connections are ok
     * @param target
     * @param numberOfConnections
     * @return
     */
    public boolean areConnectionsOk(String target, int numberOfConnections) {
        return taskGroups.containsKey(target) && taskGroups.get(target).isOk(numberOfConnections);
    }

    /**
     * Returns an active Actor name from a specific group to communicate with
     * @param type
     * @param project
     * @return
     */

    protected ActorInArea findActiveActor(String type, String area, String project) {
        var workQueueKey = String.format("boa.%s.%s.%s", area, type,
                StringUtils.isNotEmpty(project) ? project : ArchitectureFirstPhrase.DEFAULT_PROJECT);
        if (!workQueueMap.containsKey(workQueueKey) || currentWorkforceSize.get(workQueueKey) == 0 ||
                ( workQueueMap.get(workQueueKey).size() < currentWorkforceSize.get(workQueueKey))) {

            try (Jedis jedisDedicated = new Jedis(redisHost, redisPort)) {
                String roster = getRoster(type, area);

                String cursor = "0";
                ScanParams scanParams = new ScanParams()
                                            .match("*").count(ROSTER_LIST_COUNT);
                var scanResult = jedisDedicated.hscan(roster, cursor, scanParams);

                LinkedList<String> workQueue = (workQueueMap.containsKey(workQueueKey))
                        ? workQueueMap.get(workQueueKey) : new LinkedList<>();
                workQueueMap.put(workQueueKey, workQueue);

                final List<String> cleanupList = new ArrayList<>();

                final AtomicInteger workforceSize = new AtomicInteger(0);
                scanResult.getResult().forEach(x -> {
                    if (x.getValue().contains("\"message\":\"running\"")) {
                        if (x.getKey().startsWith(area) && x.getKey().contains(project)) {
                            workQueue.push(x.getKey());
                            workforceSize.incrementAndGet();
                        }
                    }
                    else {
                        cleanupList.add(x.getKey());
                    }
                });

                currentWorkforceSize.put(workQueueKey, workforceSize.get());

                cleanupList.forEach(e -> { jedisDedicated.hdel(roster, e);});
            }
        }

        return (workQueueMap.get(workQueueKey) != null && workQueueMap.get(workQueueKey).size() > 0) ? new ActorInArea(area, workQueueMap.get(workQueueKey).pop()) : ActorInArea.NO_RESULTS();
    }

    /**
     * Returns active Actors
     * @return
     */
    public List<ActorInArea> findActiveActors(String area, String project) {
        var candidateActors = bulletinBoard.getAvailableActors(area, project);

        return candidateActors;
    }


    /**
     * Returns an actor name for a group and a project
     * @param type
     * @param project
     * @return
     */
    public ActorInArea findActor(String type, String area,  String project) {
        var prj = (StringUtils.isNotEmpty(project)) ? project : ArchitectureFirstPhrase.DEFAULT_PROJECT;
        // search for actor in default project if that is what was sent
        if (ArchitectureFirstPhrase.DEFAULT_PROJECT.equals(prj)) {
            return findActiveActor(type, area, prj);
        }

        // otherwise, search for actor in actual project first
        var actorInfo = findActiveActor(type, area, prj);

        // if not found then find actor in default project
        if (StringUtils.isEmpty(actorInfo.getActorInfo())) {
            return findActiveActor(type, area, ArchitectureFirstPhrase.DEFAULT_PROJECT);
        }

        return actorInfo;
    }

    /**
     * Returns an actor name for a group (a.k.a. type) and default project
     * @param type
     * @return
     */
    public ActorInArea findActor(String type, String area) {
        return findActor(area, type, ArchitectureFirstPhrase.DEFAULT_PROJECT);
    }

    /**
     * Returns the current roster of active Actors within a group
     * @param type
     * @return
     */
    private String getRoster(String type, String area) {
        String template = "boa.BulletinBoard:topic/VicinityStatus/%s.%s:%s/Active";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        LocalDate localDate = LocalDate.now(ZoneId.of("GMT"));
        String roster = String.format(template, area, type, dtf.format(localDate));
        return roster;
    }


    /**
     * Returns the current roster of active Actors
     * @return
     */
    private String getRoster(String area) {
        String template = "boa.BulletinBoard:topic/VicinityStatus/%s.%s:%s/Active";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        LocalDate localDate = LocalDate.now(ZoneId.of("GMT"));
        String roster = String.format(template, area, "all", dtf.format(localDate));
        return roster;
    }

    /**
     * Determines if a specific Actor is available
     * @param name
     * @return
     */
    public boolean actorIsAvailable(String name, String area) {
        try (Jedis jedisDedicated = new Jedis(redisHost, redisPort)) {
            String type = name.substring(0, name.indexOf("."));
            String roster = getRoster(type, area);

            if (jedisDedicated.hexists(roster, name)) {
                return true;
            }
        }

        return false;
    }

    protected static String channelFor(String name) {
        return "channel: " + name;
    }

    private void setupTask(String target, Runnable submitTask) {
        var tasks = (taskGroups.containsKey(target))
                ? taskGroups.get(target)
                : addConnection(target);

        if (tasks.containsConnection(target)) {
            var connection = tasks.getConnection(target);
            connection.getFuture().cancel(true);
            connection.getExecutorService().shutdownNow();
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);
        var task = executorService.submit(submitTask);
        Connection conn = new Connection(executorService, task);

        tasks.setConnection(target, conn);
    }

    /**
     * Adds a connection to a subscription
     * @param ownername
     * @return
     */
    private VicinityConnections addConnection(String ownername) {
        taskGroups.put(ownername, new VicinityConnections());
        return taskGroups.get(ownername);
    }

    /**
     * Send a message to the Vicinity Server
     * @param path
     * @param requestBody
     * @return HttpResponse
     */
    private HttpResponse<String> sendMessage(String path, String requestBody) {
        return sendMessage(path, requestBody, null);
    }

    /**
     * Send a message to the Vicinity Server
     * @param path
     * @param requestBody
     * @param headers
     * @return HttpResponse
     */
    private HttpResponse<String> sendMessage(String path, String requestBody, SimpleModel headers) {
        var fullPath = String.format("%s/%s", vicinityUrl, path);

        var requestBuilder = HttpRequest
                .newBuilder()
                .uri(URI.create(fullPath))
                .header("Content-Type","application/json");

        if (headers != null && headers.size() > 0) {
            headers.entrySet().stream()
                    .filter(h -> h.getValue() instanceof String)
                    .forEach(h -> requestBuilder.headers(h.getKey(), (String) h.getValue()));
        }

        requestBuilder.POST((requestBody == null || requestBody.isEmpty())
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(requestBody));

        var request = requestBuilder.build();

        HttpClient client = HttpClient.newHttpClient();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private VicinityInfo getInfo(String path) {
        return new Gson().fromJson(getInfo(path, null).body(), VicinityInfo.class);
    }

    private HttpResponse<String> getInfo(String path, SimpleModel headers) {
        var fullPath = String.format("%s/%s", vicinityUrl, path);

        var requestBuilder = HttpRequest
                .newBuilder()
                .uri(URI.create(fullPath))
                .header("Content-Type","application/json");

        if (headers != null && headers.size() > 0) {
            headers.entrySet().stream()
                    .filter(h -> h.getValue() instanceof String)
                    .forEach(h -> requestBuilder.headers(h.getKey(), (String) h.getValue()));
        }

        requestBuilder.GET();

        var request = requestBuilder.build();

        HttpClient client = HttpClient.newHttpClient();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Lifecycle events (start)
    private void onVicinityAfterProcessArchitectureFirstPhrase(ArchitectureFirstPhrase phrase) {
    }

    private void onVicinityReceivedPhraseBlocked(ArchitectureFirstPhrase phrase) {
        log.info("Message blocked: " + phrase.toString());
    }

    private void onVicinityProcessingException(Exception e) {
        // TODO - handle threading errors.
        log.error("Message error:", e);
    }

    private void onVicinityEmptyTarget(ArchitectureFirstPhrase phrase) {
        String msg = "to: is empty on message: " + phrase.getMessage();
        log.info(msg);
        throw new VicinityException(msg);
    }

    private void onVicinityError(String path, ArchitectureFirstPhrase phrase) {
            log.error("Error on phrase: " + phrase.getRequestId());
    }

    private void onVicinityAfterPublishMessage(String channel, VicinityMessage message) {
        log.info("Published Phrase to Vicinity: " + channel + " message: " + message);
    }

    private String onVicinityPublishMessage(String path, VicinityMessage message) {
        sendMessage(path, message.toString());

        return path;
    }

    private VicinityMessage onVicinityBeforePublishMessage(ArchitectureFirstPhrase phrase, String t) {
        return generateMessage(phrase, t);
    }

    private void onVicinityRecordConvo(ArchitectureFirstPhrase phrase) {
        convo.record(phrase, Conversation.Status.SendingViaVicinity);
    }

    private boolean onVicinityReceivePhrase(ArchitectureFirstPhrase phrase) {
        log.info("Vicinity Proxy Receiving phrase: " + phrase);

        if (phrase.name().equals("ActorEntered")) {
            if (!vicinityInfo.isActorEnteredPhraseEnabled()) {
                return false;
            }
        }

        return true;
    }

    private boolean onVicinityPhraseSecurityCheck(ArchitectureFirstPhrase phrase) {
        return SecurityGuard.isOkToProceed(phrase);
    }

    private void onVicinitySecurityGuardRejectedPhrase(ArchitectureFirstPhrase phrase) {
        processInvalidToken(phrase);
    }

    private void onVicinityAlreadyPropagatedPhrase(ArchitectureFirstPhrase phrase) {
        log.warn("Local phrase already propagated: " + phrase);
    }

    private void onVicinityLocalPhraseIgnored(ArchitectureFirstPhrase phrase) {
        log.warn("Local phrase ignored: " + phrase);
    }

    // Lifecycle events (end)

}
