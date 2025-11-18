
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.Main;
import org.example.dto.CreateUserDTO;
import org.example.entity.RoleEnum;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.AdminService;
import org.example.service.UserDetailsImpl;
import org.example.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
public class AdminAddUserControllerValidTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @MockitoBean
    private BCryptPasswordEncoder encoder;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void testCreateUser_SuccessfulCreation() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO("Ivan", "Ivanov", "Ivanovich",
                RoleEnum.ROLE_ADMIN, "admin123", "0000000009");

        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setName(createUserDTO.getFirstName());
        expectedUser.setSurname(createUserDTO.getSurname());
        expectedUser.setPatronymic(createUserDTO.getPatronymic());
        expectedUser.setRole(createUserDTO.getRole());
        expectedUser.setPasswordHash(createUserDTO.getPasswordHash());
        expectedUser.setPhoneNumber(createUserDTO.getPhoneNumber());

        when(adminService.createUser(anyString(), anyString(), anyString(), any(RoleEnum.class),
                anyString(), anyString()))
                .thenAnswer(invocation -> {
                    User user = new User();
                    user.setId(1L);
                    user.setName(invocation.getArgument(0));
                    user.setSurname(invocation.getArgument(1));
                    user.setPatronymic(invocation.getArgument(2));
                    user.setRole(invocation.getArgument(3));
                    user.setPasswordHash(encoder.encode(invocation.getArgument(4)));
                    user.setPhoneNumber(invocation.getArgument(5));
                    return user;
                });
        when(encoder.encode(anyString())).thenReturn("encodedPassword");

        // Генератор пользователя
        UserDetailsImpl userDetails = new UserDetailsImpl(expectedUser);

                  new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = generateJwtToken(userDetails);

        mockMvc.perform(post("/admin/v1/users/add")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(objectMapper.writeValueAsString(createUserDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(createUserDTO.getFirstName()))
                .andExpect(jsonPath("$.surname").value(createUserDTO.getSurname()))
                .andExpect(jsonPath("$.patronymic").value(createUserDTO.getPatronymic()))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.phoneNumber").value(createUserDTO.getPhoneNumber()))
                .andExpect(jsonPath("$.passwordHash").isNotEmpty());
    }



    private String generateJwtToken(UserDetailsImpl userDetails) throws Exception {
        UsernamePasswordAuthenticationToken token =
                new  UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
        return jwtUtils.generateToken(token);
    }
}
