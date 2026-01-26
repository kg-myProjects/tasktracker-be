package de.upteams.tasktracker.security.controller;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.security.dto.LoginRequest;
import de.upteams.tasktracker.security.dto.request.ForgotPasswordRequestDto;
import de.upteams.tasktracker.security.dto.request.ResetPasswordRequestDto;
import de.upteams.tasktracker.security.entities.TokenResponseDto;
import de.upteams.tasktracker.security.service.AuthService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.security.service.CookieService;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

import static de.upteams.tasktracker.security.constants.Constants.ACCESS_TOKEN_COOKIE;
import static de.upteams.tasktracker.security.constants.Constants.REFRESH_TOKEN_COOKIE;

/**
 * Controller that receives authorization http requests
 */
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService service;
    private final CookieService cookieService;

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
            throw new RestApiException(
                    HttpStatus.UNAUTHORIZED,"No cookies found!"
            );
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RestApiException(
                        HttpStatus.UNAUTHORIZED, "Refresh token not found!"
                ));
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

    @Override
    public void forgotPassword(ForgotPasswordRequestDto request) {
        service.forgotPassword(request.email());
    }

    @Override
    public void validateResetToken(String token) {
        service.validateResetToken(token);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDto request) {
        service.resetPassword(request.token(), request.newPassword());
    }
}
