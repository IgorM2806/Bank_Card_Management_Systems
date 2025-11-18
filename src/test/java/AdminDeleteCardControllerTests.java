import org.example.Main;
import org.example.controller.AdminDeleteCardController;
import org.example.entity.RoleEnum;
import org.example.entity.User;
import org.example.exception.CardNotFoundException;
import org.example.service.CardDeletionAdminService;
import org.example.service.UserDetailsImpl;
import org.example.util.GenerateJwtTokenTest;
import org.example.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = {AdminDeleteCardController.class})
@AutoConfigureMockMvc
public class AdminDeleteCardControllerTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockitoBean
    CardDeletionAdminService cardDeletionAdminService;

    @Autowired
    private GenerateJwtTokenTest generateToken;

    @Test
    void testSuccessfulCardDeletion() throws Exception {
        String phoneNumber = "1234567890";
        Long cardId = 1L;

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("existingUser");
        existingUser.setSurname("existingSurname");
        existingUser.setPatronymic("existingPatronymic");
        existingUser.setRole(RoleEnum.ROLE_ADMIN);
        existingUser.setPasswordHash("existingPasswordHash");
        existingUser.setPhoneNumber(phoneNumber);

        UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);
        String token = generateToken.generateJwtToken(userDetails);

        doNothing().when(cardDeletionAdminService).deleteCard(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/v1/cards/delete/" + cardId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void testHandleCardNotFoundException() throws Exception {
        String phoneNumber = "1234567890";
        Long cardId = 2L;
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("existingUser");
        existingUser.setSurname("existingSurname");
        existingUser.setPatronymic("existingPatronymic");
        existingUser.setRole(RoleEnum.ROLE_ADMIN);
        existingUser.setPasswordHash("existingPasswordHash");
        existingUser.setPhoneNumber(phoneNumber);

        UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);
        String token = generateToken.generateJwtToken(userDetails);

        doThrow(new CardNotFoundException("Карта с указанным ID не найдена"))
                .when(cardDeletionAdminService).deleteCard(cardId);

        mockMvc.perform(delete("/api/admin/v1/cards/delete/" + cardId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
