package de.upteams.tasktracker.user.entity;

import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.Column;

/**
 * Application User entity
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "{user.email.notBlank}")
    @Column(
            name = "email",
            unique = true,
            nullable = false,
            columnDefinition = "VARCHAR(255) COLLATE ascii_bin"
    )
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "city")
    private String city;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "about", length = 1024)
    private String about;

    @Column (name = "avatar_url", length = 512)
    private String avatarUrl;

    @NotNull(message = "{field.notNull}")
    @Column(name = "confirm_status", nullable = false)
    @ColumnDefault("'UNCONFIRMED'")
    @Enumerated(EnumType.STRING)
    private ConfirmationStatus confirmationStatus = ConfirmationStatus.UNCONFIRMED;

    @NotNull(message = "{field.notNull}")
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public AppUser(String password, String email) {
        this.password = password;
        this.email = email;
        role = Role.ROLE_USER;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", confirmationStatus=" + confirmationStatus +
                ", password='" + (StringUtils.isBlank(password) ? "null" : "*hidden*") + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", city='" + city + '\'' +
                ", phone='" + phone + '\'' +
                ", about='" + about + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}
