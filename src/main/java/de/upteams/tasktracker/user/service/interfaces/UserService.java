package de.upteams.tasktracker.user.service.interfaces;

import de.upteams.tasktracker.user.dto.request.UpdateUserDetailsDto;
import de.upteams.tasktracker.user.dto.response.UserDetailsDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service for various operations with users
 */
public interface UserService {

    AppUser saveOrUpdate(AppUser user);

    Optional<AppUser> getByEmail(String email);

    AppUser getByEmailOrThrow(String email);

    AppUser getByIdOrThrow(String id);

    List<UserResponseDto> getAll();

    UserDetailsDto getUserDetails();

    UserDetailsDto updateUserDetails(UpdateUserDetailsDto dto);

    UserDetailsDto updateAvatar(MultipartFile file);
}