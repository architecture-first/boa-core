package com.architecturefirst.boa.framework.business.vicinity.tasklist;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a connection to a Tasklist
 */
@Data
@AllArgsConstructor
public class TaskListConnection {
    private String taskList;
    private String task;
}
