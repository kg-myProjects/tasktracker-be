package de.upteams.tasktracker.task.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class InvalidTaskPayloadEsception extends RestApiException {
    public InvalidTaskPayloadEsception(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
