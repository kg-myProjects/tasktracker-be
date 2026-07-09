package de.upteams.tasktracker.user.dto.response;

import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import de.upteams.tasktracker.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDto {

    private String email;
    private Role role;
    private ConfirmationStatus confirmationStatus;

    private String firstName;
    private String lastName;
    private String birthDate;
    private String city;
    private String phone;
    private String about;

    private String avatarUrl;
    private Long avatarUpdatedAt;
}
