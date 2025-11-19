import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.dto.CreateUserDTO;
import org.example.entity.RoleEnum;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserDetailsImpl;
import org.example.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
public class AdminAddUserControllerNotValidTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @MockitoBean
    private BCryptPasswordEncoder encoder;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void testCreateUser_DuplicatePhoneNumber() throws Exception {
        String duplicatePhoneNumber = "0000000009";

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("John");
        existingUser.setSurname("Doe");
        existingUser.setPatronymic("Johnovich");
        existingUser.setRole(RoleEnum.ROLE_ADMIN);
        existingUser.setPasswordHash("encodedPassword");
        existingUser.setPhoneNumber(duplicatePhoneNumber);

        when(userRepository.findByPhoneNumber(duplicatePhoneNumber))
                .thenReturn(Optional.of(existingUser));

        CreateUserDTO createUserDTO = new CreateUserDTO("Ivan", "Ivanov", "Ivanovich",
                RoleEnum.ROLE_ADMIN, "admin123", duplicatePhoneNumber);

        UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);
        String  token = generateJwtToken(userDetails);

        mockMvc.perform(post("/api/admin/v1/users/add")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(createUserDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content()
                        .string(containsString("Пользователь с таким номером телефона уже существует!")));
    }

    private String generateJwtToken(UserDetailsImpl userDetails) throws Exception {
        UsernamePasswordAuthenticationToken token =
                new  UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
        return jwtUtils.generateToken(token);
    }

        @Test
        void testCreateUser_NoValidtoken() throws Exception {
            String phoneNumber = "1234567890";

            CreateUserDTO createUserDTO = new CreateUserDTO("Ivan", "Ivanov",
                    "Ivanovich", RoleEnum.ROLE_ADMIN, "admin123", phoneNumber);

            String  token = "noValidToken";
            MockHttpServletResponse response = mockMvc.perform(post("/api/admin/v1/users/add")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .content(objectMapper.writeValueAsString(createUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse();

            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        }
        @Test
        void testCreateUser_NoAdminRole() throws Exception {
        String phoneNumber = "1234567890";
            User existingUser = new User();
            existingUser.setId(1L);
            existingUser.setName("John");
            existingUser.setSurname("Doe");
            existingUser.setPatronymic("Johnovich");
            existingUser.setRole(RoleEnum.ROLE_USERS);
            existingUser.setPasswordHash("encodedPassword");
            existingUser.setPhoneNumber(phoneNumber);

            CreateUserDTO createUserDTO = new CreateUserDTO("Ivan", "Ivanov", "Ivanovich",
                    RoleEnum.ROLE_ADMIN, "admin123", phoneNumber);

            UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);

            String  token = generateJwtToken(userDetails);

            mockMvc.perform(post("/api/admin/v1/users/add")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .content(objectMapper.writeValueAsString(createUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
}
