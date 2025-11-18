import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.controller.AuthController;
import org.example.dto.LoginRequestDTO;
import org.example.entity.RoleEnum;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.AdminService;
import org.example.service.CustomUserDetailsService;
import org.example.service.UserDetailsImpl;
import org.example.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper =  new ObjectMapper();

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Открываем аннотации для инъекций
        objectMapper = new ObjectMapper(); // Необходим для сериализации объектов
    }



    @Test
    void testLoginSuccess() throws Exception {
       var loginRequestDTO = new LoginRequestDTO("0000000002", "admin123");

        User user = new User();
        user.setName("Иван");
        user.setSurname("Иванов");
        user.setPatronymic("Иванович");
        user.setRole(RoleEnum.ROLE_ADMIN);
        user.setPasswordHash("$2a$10$A60syo6l8cEuEAyRnCbvzuUPfSJjz1tQp60QGezFkLNaItjFmrB9W");
        user.setPhoneNumber("0000000002");
        user.setEnabled(true);
        user.setAccountExpired(false);
        user.setLocked(false);
        user.setCredentialsExpired(false);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null));
        when(jwtUtils.generateToken(any(Authentication.class))).thenReturn("valid-jwt-token");
        when(customUserDetailsService.loadUserByUsername("0000000002")).thenReturn(userDetails);

        mockMvc.perform(post("/api/v1/auth/login")
                        .content(objectMapper.writeValueAsBytes(loginRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // Для вывода подробностей запросов
                .andExpect(status().isOk()) // Ожидаемый код ответа 200 OK
                .andExpect(jsonPath("$.token").value("valid-jwt-token")) // Токен совпадает
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN")); // Роль верна
    }

    @Test
    void testLoginWithInvalidPassword() throws Exception {
        var loginRequestDTO = new LoginRequestDTO("0000000002", "wrong_password");

        doThrow(BadCredentialsException.class).when(authenticationManager).authenticate(any());

        mockMvc.perform(post("/api/v1/auth/login")
                        .content(objectMapper.writeValueAsBytes(loginRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Incorrect credentials or account problems.")));


    }

    @Test
    void testLoginWithInvalidPhoneNumber() throws Exception {
        var loginRequestDTO = new LoginRequestDTO("9999999999", "admin123");
        doThrow(BadCredentialsException.class).when(authenticationManager).authenticate(any());
        mockMvc.perform(post("/api/v1/auth/login")
                        .content(objectMapper.writeValueAsBytes(loginRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Incorrect credentials or account problems.")));
    }

    @Test
    void testLoginWithMalformedJSON() throws Exception {
        String malformedJson = "{\"phone\": \"0000000002\", \"password\": admin123}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .content(malformedJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid request body")));
    }
}
