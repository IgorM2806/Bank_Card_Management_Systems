
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.controller.AdminAddCardController;
import org.example.dto.CreateCardDTO;
import org.example.entity.RoleEnum;
import org.example.entity.Status;
import org.example.entity.User;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = {AdminAddCardController.class})
@AutoConfigureMockMvc
public class AdminAddCardControllerNotValidTests {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminService adminService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;


    @Test
    void testCreateCardWithEmptyCardNumber() throws Exception {
        String phoneNumber = "1234567890";
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("John");
        existingUser.setSurname("Doe");
        existingUser.setPatronymic("Johnovich");
        existingUser.setRole(RoleEnum.ROLE_ADMIN);
        existingUser.setPasswordHash("encodedPassword");
        existingUser.setPhoneNumber(phoneNumber);

        CreateCardDTO createCardDTO = new CreateCardDTO("",
                LocalDate.now().plusYears(1), Status.ACTIVE, BigDecimal.valueOf(1000.00));

        UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);

        String  token = generateJwtToken(userDetails);

        String jsonContent = objectMapper.writeValueAsString(createCardDTO);

        mockMvc.perform(post("/admin/v1/addCards/{userId}", existingUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .content(jsonContent)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateCardWithNotvalidBalance() throws Exception {
        String phoneNumber = "1234567890";
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("John");
        existingUser.setSurname("Doe");
        existingUser.setPatronymic("Johnovich");
        existingUser.setRole(RoleEnum.ROLE_ADMIN);
        existingUser.setPasswordHash("encodedPassword");
        existingUser.setPhoneNumber(phoneNumber);

        CreateCardDTO createCardDTO = new CreateCardDTO("1234567890123456",
                LocalDate.now().plusYears(1), Status.ACTIVE, BigDecimal.valueOf(-1000.00));

        UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);
        String  token = generateJwtToken(userDetails);
        String jsonContent = objectMapper.writeValueAsString(createCardDTO);

        mockMvc.perform(post("/admin/v1/addCards/{userId}", existingUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void testCreateCardWithNotValidToken() throws Exception {
        String phoneNumber = "1234567890";
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("John");
        existingUser.setSurname("Doe");
        existingUser.setPatronymic("Johnovich");
        existingUser.setRole(RoleEnum.ROLE_USERS);
        existingUser.setPasswordHash("encodedPassword");
        existingUser.setPhoneNumber(phoneNumber);

        CreateCardDTO createCardDTO = new CreateCardDTO("1234567890123456",
                LocalDate.now().plusYears(1), Status.ACTIVE, BigDecimal.valueOf(1000.00));

        UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);
        String  token = generateJwtToken(userDetails);
        String jsonContent = objectMapper.writeValueAsString(createCardDTO);

        mockMvc.perform(post("/admin/v1/addCards/{userId}", existingUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(jsonContent)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

    }

    private String generateJwtToken(UserDetailsImpl userDetails) throws Exception {
        UsernamePasswordAuthenticationToken token =
                new  UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
        return jwtUtils.generateToken(token);
    }

}
