package de.upteams.tasktracker.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDetailsDto {

    private String firstName;
    private String lastName;
    private String birthDate;
    private String city;
    private String phone;
    private String about;

}