package de.upteams.tasktracker.audit.resolver;

import de.upteams.tasktracker.security.dto.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    public AppUser getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }

            Object principal = auth.getPrincipal();
            if (!(principal instanceof AuthUserDetails userDetails)) {
                return null;
            }
            return userDetails.user();
        } catch (Exception e) {
            return null;
        }
    }
}