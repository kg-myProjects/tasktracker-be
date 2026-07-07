package de.upteams.tasktracker.user.exception;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends RestApiException {

    public UserAlreadyExistException() {
        super(HttpStatus.CONFLICT, "A user with this email already exists.");
    }
}
