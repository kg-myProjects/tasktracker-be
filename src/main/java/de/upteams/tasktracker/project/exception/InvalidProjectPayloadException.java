package de.upteams.tasktracker.project.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class InvalidProjectPayloadException extends RestApiException {
    public InvalidProjectPayloadException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
