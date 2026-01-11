package de.upteams.tasktracker.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserCreateDto(
        @NotBlank
        @Email(message = "must be a well-formed email addres")
        @Schema(
                description = "new User email",
                example = "tes_dev@upteams.de"
        )
        String email,
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9])[A-Za-z0-9[^A-Za-z0-9]]{8,}$",
                message = "The password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number, 1 special character and be at least 8 characters long.")
        @Schema(
                description = "new User password",
                example = "dev_TR_pass_007"
        )
        String password) {
}
