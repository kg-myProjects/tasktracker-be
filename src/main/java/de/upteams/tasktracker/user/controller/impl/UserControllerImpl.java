package de.upteams.tasktracker.user.controller.impl;

import de.upteams.tasktracker.user.controller.interfaces.UserApi;
import de.upteams.tasktracker.user.dto.request.UpdateProfileRequest;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

/**
 * REST Controller that receives http-requests for various operations with Employees
 */
@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserApi {

    /**
     * Service for various operations with Employees
     */
    private final UserService service;

    @Override
    public List<UserResponseDto> getAll() {
        return service.getAll();
    }


    @Override
    public UserResponseDto getMe() {
        return service.getMe();
    }

    @Override
    public UserResponseDto updateMe(UpdateProfileRequest request) {
        return service.updateNickname(request.nickname());
    }

    @Override
    public UserResponseDto uploadAvatar(MultipartFile file) {
        return service.updateAvatar(file);
    }
}
