package com.architecturefirst.boa.framework.business.vicinity.tasklist;

import com.google.gson.Gson;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Represents an entry in a task list
 */
public class TaskListEntry {
    public enum Status {
        Pending ("Pending"),
        InProgress ("InProgress"),
        Complete ("Complete"),
        Failed ("Failed"),
        Gone ("Gone");

        private final String status;

        Status(String status) {
            this.status = status;
        }
    }

    private TaskListEntry.Status status;
    private String message;
    private String timeStamp;

    public TaskListEntry(TaskListEntry.Status status, String message) {
        this.status = status;
        this.message = message;
        touch();
    }

    public void setStatus(TaskListEntry.Status status) {
        this.status = status;
        touch();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void touch() {
        timeStamp = ZonedDateTime.now(ZoneId.of("GMT")).toString();
    }

    private String formatEntry() {
        return new Gson().toJson(this, TaskListEntry.class);
    }

    public String toString() {
        return formatEntry();
    }
}



