package de.upteams.tasktracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.upteams.tasktracker.exception.handling.GlobalExceptionHandler;
import de.upteams.tasktracker.security.config.SecurityConfig;
import de.upteams.tasktracker.security.dto.AuthUserDetails;
import de.upteams.tasktracker.security.service.CustomUserDetailsService;
import de.upteams.tasktracker.security.service.JwtTokenService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    protected JwtTokenService jwtTokenService;

    protected AuthUserDetails mockUserPrincipal;
    protected AuthUserDetails mockAdminPrincipal;

    @BeforeEach
    void baseSetUp() {
        this.mockUserPrincipal = createPrincipal("user@test.com", Role.ROLE_USER);
        this.mockAdminPrincipal = createPrincipal("admin@test.com", Role.ROLE_ADMIN);
    }

    private AuthUserDetails createPrincipal(String email, Role role) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setRole(role);
        return new AuthUserDetails(user);
    }



    protected ResultActions performPost(String url, Object body, AuthUserDetails principal) throws Exception {
        return mockMvc.perform(post(url)
                .with(principal != null ? user(principal) : request -> request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions performGet(String url, AuthUserDetails principal) throws Exception {
        return mockMvc.perform(get(url)
                .with(principal != null ? user(principal) : request -> request)
                .contentType(MediaType.APPLICATION_JSON));
    }



    protected ResultActions performDelete(String url, AuthUserDetails principal) throws Exception {
        return mockMvc.perform(delete(url)
                .with(principal != null ? user(principal) : request -> request)
                .contentType(MediaType.APPLICATION_JSON));
    }

    protected ResultActions performPatch(String url, Object body, AuthUserDetails principal) throws Exception {
        return mockMvc.perform(patch(url)
                .with(principal != null ? user(principal) : request -> request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

}
