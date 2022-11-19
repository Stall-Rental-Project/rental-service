package com.srs.rental.task;

import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.Serializable;

/**
 * @author duynt on 3/28/22
 */
@Data
class TaskResult implements Serializable {
    final String taskName;
    boolean completed;
    boolean success;
    Exception exception;
    String stacktrace;

    public TaskResult(String taskName) {
        this.taskName = taskName;
    }

    static TaskResult success(String taskName) {
        var result = new TaskResult(taskName);
        result.completed = true;
        result.success = true;
        return result;
    }

    static TaskResult failure(String taskName, Exception e) {
        var result = new TaskResult(taskName);
        result.completed = true;
        result.success = false;
        result.exception = e;
        result.stacktrace = ExceptionUtils.getStackTrace(e);
        return result;
    }
}
