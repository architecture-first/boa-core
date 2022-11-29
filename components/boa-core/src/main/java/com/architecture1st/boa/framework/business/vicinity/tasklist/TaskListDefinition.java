package com.architecture1st.boa.framework.business.vicinity.tasklist;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a task list definition
 */
@Data
public class TaskListDefinition {
    private String name;
    private List<String> tasks = new ArrayList<>();
}
