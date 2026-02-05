package de.upteams.tasktracker.audit.resolver;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CurrentUserResolver {

    public AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof AuthUserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Principal is not of type AuthUserDetails");
        }
        return userDetails.user();
    }
}