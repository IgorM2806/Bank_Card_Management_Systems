import org.example.Main;
import org.example.entity.RoleEnum;
import org.example.entity.User;
import org.example.service.CardTransferService;
import org.example.service.UserDetailsImpl;
import org.example.util.GenerateJwtTokenTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
public class UserTransferControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenerateJwtTokenTest generateJwtTokenTest;

    @MockitoBean
    private CardTransferService transferService;

    @Test
    void shouldPerformTransferSuccessfully() throws Exception {
        User existingUser = new User("existingUser", "existingUser",
                "existingUser", RoleEnum.ROLE_ADMIN, "password", "1234567890");
        UserDetailsImpl existingUserDetails = new UserDetailsImpl(existingUser);

        String token = generateJwtTokenTest.generateJwtToken(existingUserDetails);

        String requestJson = """
                {
                "userId": 1,
                "sourceCard": "1234567890123456",
                "targetCard": "9876543210987654",
                "amount": "100.00"
                }""";

        Long userId = 1L;
        Long sourceCard = 1234567890123456L;
        Long targetCard = 9876543210987654L;
        BigDecimal amount = new BigDecimal("100.00");
        doNothing().when(transferService)
                .performTransferForUser(userId, sourceCard, targetCard, amount);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/v1/transferCards/transfer")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());

        verify(transferService).performTransferForUser(userId, sourceCard, targetCard, amount);
    }
    @Test
    void invalidRequestWithWrongDataType() throws Exception {
        User existingUser = new User("existingUser", "existingUser",
                "existingUser", RoleEnum.ROLE_ADMIN, "password", "1234567890");
        UserDetailsImpl existingUserDetails = new UserDetailsImpl(existingUser);

        String token = generateJwtTokenTest.generateJwtToken(existingUserDetails);

        String requestJson = """
                {
                "userId": 1,
                "sourceCard": "1234567890123456",
                "targetCard": "9876543210987654",
                "amount": "abc"
                }""";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/v1/transferCards/transfer")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingRequiredFieldInRequest() throws Exception {
        User existingUser = new User("existingUser", "existingUser",
                "existingUser", RoleEnum.ROLE_ADMIN, "password", "1234567890");
        UserDetailsImpl existingUserDetails = new UserDetailsImpl(existingUser);
        String token = generateJwtTokenTest.generateJwtToken(existingUserDetails);
        String requestJson = """
        {
          "userId": 1,
          "sourceCard": "123456",
          "amount": 100.0
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/v1/transferCards/transfer")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

}
