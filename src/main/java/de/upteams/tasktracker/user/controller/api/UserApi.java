package de.upteams.tasktracker.user.controller.api;

import de.upteams.tasktracker.user.dto.request.UpdateUserDetailsDto;
import de.upteams.tasktracker.user.dto.response.UpdateAvatarResponseDto;
import de.upteams.tasktracker.user.dto.response.UserDetailsDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST mappings for user operations.
 * Implementation classes should implement this interface.
 */
@RequestMapping("/api/v1/users")
public interface UserApi extends UserApiSwaggerDoc {

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    List<UserResponseDto> getAll();

    @GetMapping("/me-details")
    UserDetailsDto getUserDetails();

    @PatchMapping("/update-user")
    UserDetailsDto updateUserDetails(@RequestBody UpdateUserDetailsDto request);

    @PatchMapping(value = "/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UpdateAvatarResponseDto updateAvatar(@RequestParam("file") MultipartFile file);
}