package de.upteams.tasktracker.task.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class InvalidTaskPayloadException extends RestApiException {
    public InvalidTaskPayloadException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
