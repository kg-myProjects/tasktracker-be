package de.upteams.tasktracker.exception.handling.exceptions.avatar;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class AvatarProcessingException extends RestApiException {
    public AvatarProcessingException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public AvatarProcessingException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        this.initCause(cause);
    }
}