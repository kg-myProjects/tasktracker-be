package de.upteams.tasktracker.user.controller;

import de.upteams.tasktracker.user.controller.api.UserApi;
import de.upteams.tasktracker.user.dto.request.UpdateUserDetailsDto;
import de.upteams.tasktracker.user.dto.response.UserDetailsDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

/**
 * REST Controller that receives http-requests for various operations with Employees
 */
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    /**
     * Service for various operations with Employees
     */
    private final UserService service;

    @Override
    public List<UserResponseDto> getAll() {
        return service.getAll();
    }


    @Override
    public UserDetailsDto getUserDetails() {
        return service.getUserDetails();
    }

    @Override
    public UserDetailsDto updateUserDetails(UpdateUserDetailsDto request) {
        return service.updateUserDetails(request);
    }

    @Override
    public UserDetailsDto updateAvatar(MultipartFile file) {
        return service.updateAvatar(file);
    }
}
