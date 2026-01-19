package de.upteams.tasktracker.security.controller;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.security.dto.LoginRequest;
import de.upteams.tasktracker.security.dto.request.ForgotPasswordRequestDto;
import de.upteams.tasktracker.security.dto.request.ResetPasswordRequestDto;
import de.upteams.tasktracker.security.entities.PasswordResetToken;
import de.upteams.tasktracker.security.entities.TokenResponseDto;
import de.upteams.tasktracker.security.service.*;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.persistence.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static de.upteams.tasktracker.security.constants.Constants.ACCESS_TOKEN_COOKIE;
import static de.upteams.tasktracker.security.constants.Constants.REFRESH_TOKEN_COOKIE;

/**
 * Controller that receives authorization http requests
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {

    private final AuthService service;
    private final CookieService cookieService;
    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;
    private final MailService mailService;

    @Override
    public TokenResponseDto login(LoginRequest loginRequest, HttpServletResponse response) {
        final TokenResponseDto tokens = service.login(loginRequest);

        final Cookie accessCookie = cookieService.generateAccessTokenCookie(tokens.getAccessToken());
        final Cookie refreshCookie = cookieService.generateRefreshTokenCookie(tokens.getRefreshToken());

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return tokens;
    }

    @Override
    public TokenResponseDto refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {

        String curRefreshToken = extractRefreshTokenFromCookies(request);
        String newAccessToken = service.refreshAccessToken(curRefreshToken);
        Cookie accessCookie = cookieService.generateAccessTokenCookie(newAccessToken);
        response.addCookie(accessCookie);

        return new TokenResponseDto(newAccessToken, curRefreshToken);
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new RuntimeException("No cookies found!");
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found!"));
    }

    @Override
    public TokenResponseDto logout(HttpServletResponse response) {
        final Cookie accessCookie = cookieService.generateLogoutCookie(ACCESS_TOKEN_COOKIE);
        final Cookie refreshCookie = cookieService.generateLogoutCookie(REFRESH_TOKEN_COOKIE);
        SecurityContextHolder.clearContext();

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return new TokenResponseDto(null, null);
    }

    @Override
    public UserResponseDto getCurrentUser(@AuthenticationPrincipal AuthUserDetails user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return new UserResponseDto(
                user.getUsername(),
                user.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority(),
                user.user().getConfirmationStatus()
        );
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto dto
    ) {
        AppUser user = userRepository
                .findByEmailIgnoreCase(dto.email())
                .orElseThrow(() -> new RestApiException(HttpStatus.NOT_FOUND, "User with this email not found"));


        PasswordResetToken token = passwordResetService.createToken(user);

        String resetLink =
                "http://localhost:5173/reset-password?token=" + token.getToken();

        mailService.sendPasswordResetMail(user.getEmail(), resetLink);

    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto dto
    ) {
        passwordResetService.resetPassword(dto.token(), dto.newPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reset-password/validate")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        passwordResetService.validateToken(token);
        return ResponseEntity.ok().build();
    }

}

