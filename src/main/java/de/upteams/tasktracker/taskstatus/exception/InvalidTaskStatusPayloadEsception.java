package de.upteams.tasktracker.taskstatus.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class InvalidTaskStatusPayloadEsception extends RestApiException {
    public InvalidTaskStatusPayloadEsception(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
