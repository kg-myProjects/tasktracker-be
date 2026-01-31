package de.upteams.tasktracker.user.service.impl;

import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.exception.UserNotFoundException;
import de.upteams.tasktracker.user.persistence.UserRepository;
import de.upteams.tasktracker.user.service.UserService;
import de.upteams.tasktracker.user.util.AppUserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
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
    public UserResponseDto getMe(){
        AppUser user = getCurrentUserOrThrow();
        return mappingService.mapEntityToDto(user);
    }
    @Transactional
    public UserResponseDto updateNickname(String nickname){
        AppUser user = getCurrentUserOrThrow();
        user.setNickname(nickname);
        AppUser saved = repository.save(user);
        return mappingService.mapEntityToDto(saved);
    }
    @Transactional
    public UserResponseDto updateAvatar(MultipartFile file) {
        if(file==null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        AppUser user = getCurrentUserOrThrow();
        try{Files.createDirectories(AVATAR_DIR);
            String originalName = file.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".png";

            String filename = user.getId() + ext;
            Path target = AVATAR_DIR.resolve(filename);

            Files.write(target, file.getBytes());

            String avatarUrl = "/uploads/avatars/" + filename;

            user.setAvatarUrl(avatarUrl);
            AppUser saved = repository.save(user);

            return mappingService.mapEntityToDto(saved);

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


