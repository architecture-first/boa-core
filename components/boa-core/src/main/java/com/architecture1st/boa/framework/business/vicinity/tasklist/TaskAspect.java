package com.architecture1st.boa.framework.business.vicinity.tasklist;

import com.architecture1st.boa.framework.business.actors.Actor;
import com.architecture1st.boa.framework.business.actors.SecurityGuard;
import com.architecture1st.boa.framework.business.actors.exceptions.ActorException;
import com.architecture1st.boa.framework.technical.aop.AsyncRequestContext;
import com.architecture1st.boa.framework.technical.aop.RequestContext;
import com.architecture1st.boa.framework.technical.phrases.ArchitectureFirstPhrase;
import com.architecture1st.boa.framework.technical.user.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Performs Aspect-oriented Tasklist processing
 */
@Slf4j
@Aspect
@Component
public class TaskAspect {

    private RequestContext requestContext;  // don't autowire since request context doesn't work properly in aspects

    @Autowired
    private AsyncRequestContext asyncRequestContext;

    @Autowired
    private TaskList taskList;

    /**
     * Records the task and executes it
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.architecture1st.boa.framework.business.vicinity.tasklist.TaskTracking)")
    public Object recordTask(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        TaskTracking taskTracking = method.getAnnotation(TaskTracking.class);
        ArchitectureFirstPhrase eventParam = null;
        String accessToken = null;

        var param = (joinPoint.getArgs().length > 0) ? joinPoint.getArgs()[0] : "ARCHITECTURE_FIRST_EVENT_SHOULD_BE_FIRST_PARAMETER";
        var requestId = "";
        var tasklist = "";
        if (param instanceof ArchitectureFirstPhrase) {
            eventParam = (ArchitectureFirstPhrase) param;
            requestId = eventParam.getRequestId();
            accessToken = eventParam.getAccessToken();
            tasklist = eventParam.tasklist();
        }

        var actor = (Actor) joinPoint.getTarget();

        requestContext = asyncRequestContext.requestContext();

        var userInfo = new UserInfo();
        userInfo.setAccessToken(
                (StringUtils.isNotEmpty(accessToken))
                        ? accessToken : SecurityGuard.getAccessToken()
        );
        if (userInfo.hasAccessToken() && (eventParam != null && !SecurityGuard.needsAnAccessToken(eventParam))) {
            userInfo.setUserId(SecurityGuard.getUserIdFromToken(userInfo.getAccessToken()));
            requestContext.setUserInfo(userInfo);
        }

        if (StringUtils.isNotEmpty(requestId)) {    // use the requestId from the event to keep the chain together
            requestContext.setRequestId(requestId);
        }

        if ((StringUtils.isEmpty(tasklist))) {
            requestContext.setTasklist(StringUtils.isNotEmpty(taskTracking.defaultParentTask()) ?
                taskTracking.defaultParentTask() : taskTracking.task());
            if (eventParam != null) {   // this is an optional parameter
                eventParam.setTasklist(requestContext.getTasklist());
            }
        }
        else {
            requestContext.setTasklist(tasklist);
        }

        var taskList =  requestContext.getTasklist();
        var conn = beginTask(taskList, taskTracking.task(), requestContext.getRequestId(), actor.name());

        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
        }
        catch (Throwable t) {
            failTask(conn, t.getMessage());
            log.error("Error:", t);
            requestContext.setException(t);
            actor.onException(eventParam, new ActorException(actor,  new RuntimeException(t)));
        }

        endTask(conn, requestContext.getRequestId(), actor.name());

        return proceed;
    }

    /**
     * Creates an entry for the beginning of a task
     * @param usecase
     * @param task
     * @param requestId
     * @param actorname
     * @return a tasklist connection
     */
    protected TaskListConnection beginTask(String usecase, String task, String requestId, String actorname) {
        TaskListEntry entry = new TaskListEntry(TaskListEntry.Status.InProgress, actorname);
        taskList.postEntry(requestId, usecase, task, entry.toString());

        return new TaskListConnection(usecase, task);
    }

    /**
     * Records the end of a task
     * @param conn
     * @param requestId
     * @param actorname
     */
    protected void endTask(TaskListConnection conn, String requestId, String actorname) {
        TaskListEntry entry = new TaskListEntry(TaskListEntry.Status.Complete, actorname);
        taskList.postEntry(requestId, conn.getTaskList(), conn.getTask(), entry.toString());
        taskList.recordCompletion(requestId, conn.getTaskList(), conn.getTask());
        boolean tasklistIsFinished = taskList.handleFinishedTasks(requestId, conn.getTaskList(), conn.getTask());

        if (tasklistIsFinished && requestContext.isAsync()) {
            asyncRequestContext.clearContext();
        }
    }

    /**
     * Records a task that has failed
     * @param conn
     * @param message
     */
    protected void failTask(TaskListConnection conn, String message) {
        TaskListEntry entry = new TaskListEntry(TaskListEntry.Status.Failed, "Failed");
        taskList.postEntry(requestContext.getRequestId(), conn.getTaskList(), conn.getTask(), entry.toString());
        taskList.recordFailure(requestContext.getRequestId(), conn.getTaskList(), conn.getTask(), message);
        boolean tasklistIsFinished = taskList.handleFinishedTasks(requestContext.getRequestId(), conn.getTaskList(), conn.getTask());

        if (tasklistIsFinished && requestContext.isAsync()) {
            asyncRequestContext.clearContext();
        }
    }
}
