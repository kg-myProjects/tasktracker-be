package de.upteams.tasktracker.user.service.impl;

import de.upteams.tasktracker.user.dto.request.UpdateUserDetailsDto;
import de.upteams.tasktracker.user.dto.response.UserDetailsDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.exception.UserNotFoundException;
import de.upteams.tasktracker.user.persistence.UserRepository;
import de.upteams.tasktracker.user.service.UserService;
import de.upteams.tasktracker.user.util.AppUserMapper;
import de.upteams.tasktracker.user.util.UserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for various operations with Employees
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final AppUserMapper mappingService;
    private static final Path AVATAR_DIR = Paths.get("uploads", "avatars");

    @Override
    public AppUser saveOrUpdate(final AppUser user) {
        return repository.save(user);
    }

    @Override
    @Transactional
    public Optional<AppUser> getByEmail(String email) {
        return repository.findByEmailIgnoreCase(email);
    }

    @Override
    @Transactional
    public AppUser getByEmailOrThrow(String email) {
        return getByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional
    public AppUser getByIdOrThrow(String id) {
        return repository
                .findById(UUID.fromString(id))
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<UserResponseDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Transactional
    public UserDetailsDto getUserDetails(){
        AppUser user = getCurrentUserOrThrow();
        return mappingService.mapEntityToUserDetailsDto(user);
    }

    @Transactional
    public UserDetailsDto updateUserDetails(UpdateUserDetailsDto dto){

        AppUser user = getCurrentUserOrThrow();

        if (dto.getFirstName() != null) {
            user.setFirstName(UserUtils.normalizeUserName(dto.getFirstName()));
        }
        if (dto.getLastName() != null) {
            user.setLastName(UserUtils.normalizeUserName(dto.getLastName()));
        }
        if (dto.getBirthDate() != null) user.setBirthDate(dto.getBirthDate());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAbout() != null) user.setAbout(dto.getAbout());

        AppUser saved = repository.save(user);

        return mappingService.mapEntityToUserDetailsDto(saved);
    }

    @Transactional
    public UserDetailsDto updateAvatar(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        AppUser user = getCurrentUserOrThrow();
        try {
            String filename = UserUtils.generateUserAvatarFileName(user.getId(), file);

            Path savedPath = UserUtils.saveUserAvatar(file, AVATAR_DIR, filename);
            System.out.println("Avatar saved to: " + savedPath);

            String avatarUrl = "/uploads/avatars/" + filename;
            user.setAvatarUrl(avatarUrl);

            AppUser saved = repository.save(user);

            return mappingService.mapEntityToUserDetailsDto(saved);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save avatar", e);
        }
    }

    private AppUser getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UserNotFoundException();
        }

        String email = auth.getName();
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new);
    }
}


