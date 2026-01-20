package de.upteams.tasktracker.taskstatus.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class TaskStatusNotFoundException extends RestApiException {
    public TaskStatusNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Tasks Status not found");
    }
}
