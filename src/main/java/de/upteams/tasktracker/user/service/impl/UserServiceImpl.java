package de.upteams.tasktracker.user.service.impl;

import de.upteams.tasktracker.exception.handling.exceptions.avatar.AvatarProcessingException;
import de.upteams.tasktracker.exception.handling.exceptions.avatar.InvalidAvatarFileException;
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

import java.awt.image.BufferedImage;
import java.io.IOException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.upteams.tasktracker.user.util.UserUtils.AVATAR_DIR;
import static de.upteams.tasktracker.user.util.UserUtils.MAX_AVATAR_SIZE;

/**
 * Service for various operations with Employees
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final AppUserMapper mappingService;

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
            user.setFirstName(UserUtils.emptyFieldToNull(UserUtils.normalizeUserName(dto.getFirstName())));
        }

        if (dto.getLastName() != null) {
            user.setLastName(UserUtils.emptyFieldToNull(UserUtils.normalizeUserName(dto.getLastName())));
        }
        if (dto.getBirthDate() != null) user.setBirthDate(UserUtils.emptyFieldToNull(dto.getBirthDate()));
        if (dto.getCity() != null) user.setCity(UserUtils.emptyFieldToNull(dto.getCity()));
        if (dto.getPhone() != null) user.setPhone(UserUtils.emptyFieldToNull(dto.getPhone()));
        if (dto.getAbout() != null) user.setAbout(UserUtils.emptyFieldToNull(dto.getAbout()));

        AppUser saved = repository.save(user);

        return mappingService.mapEntityToUserDetailsDto(saved);
    }

    @Transactional
    public UserDetailsDto updateAvatar(MultipartFile file) {

        AppUser user = getCurrentUserOrThrow();

        if (file == null || file.isEmpty() ||
                file.getContentType() == null ||
                (!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg")) ||
                file.getSize() > MAX_AVATAR_SIZE) {
            throw new InvalidAvatarFileException("Invalid avatar file: must be PNG or JPEG and <= 5MB");
        }

        try {
            BufferedImage processedImage = UserUtils.resizeAvatarAndConvertToPng(file, 400);

            String filename = UserUtils.generateUserAvatarFileName(user.getId());
            UserUtils.saveUserAvatar(processedImage, AVATAR_DIR, filename);

            user.setAvatarUrl("/uploads/avatars/" + filename);
            AppUser saved = repository.save(user);

            return mappingService.mapEntityToUserDetailsDto(saved);

        } catch (IOException e) {
            throw new AvatarProcessingException("Failed to process avatar file on server", e);
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


