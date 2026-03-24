package de.upteams.tasktracker.user.controller;

import de.upteams.tasktracker.user.controller.api.RegisterControllerApi;
import de.upteams.tasktracker.user.dto.request.UserCreateDto;
import de.upteams.tasktracker.user.dto.response.UserCreateResponseDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.service.impl.UserRegisterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RegisterController implements RegisterControllerApi {

    private final UserRegisterServiceImpl service;

    @Override
    public UserResponseDto confirmRegistration(String code) {
        return service.confirmRegistration(code);
    }

    @Override
    public UserCreateResponseDto register(UserCreateDto registerUser) {
        return service.register(registerUser);
    }


    @GetMapping("/confirm-redirect/{code}")
    public ResponseEntity<Void> confirmEmailRedirect(@PathVariable String code) {
        service.confirmRegistration(code);
        URI redirectUri = URI.create("http://localhost:5173/#/login?confirmed=true");
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();


    }
}
