package de.upteams.tasktracker.exception.handling.exceptions.avatar;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class InvalidAvatarFileException extends RestApiException {
    public InvalidAvatarFileException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
