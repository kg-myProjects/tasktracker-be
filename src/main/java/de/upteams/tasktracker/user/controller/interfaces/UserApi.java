package de.upteams.tasktracker.user.controller.interfaces;

import de.upteams.tasktracker.user.dto.request.UpdateProfileRequest;
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



    @GetMapping("/me")
    UserResponseDto getMe();

    @PutMapping("/me")
    UserResponseDto updateMe(@RequestBody UpdateProfileRequest request);

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    UserResponseDto uploadAvatar(@RequestParam("file") MultipartFile file);
}
