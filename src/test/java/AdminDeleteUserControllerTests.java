
import org.example.Main;
import org.example.entity.RoleEnum;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.service.UserDeletionAdminService;
import org.example.service.UserDetailsImpl;
import org.example.util.GenerateJwtTokenTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = {AdminDeleteUserControllerTests.class})
@AutoConfigureMockMvc
public class AdminDeleteUserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDeletionAdminService userDeletionAdminService;

    @Autowired
    private GenerateJwtTokenTest generateToken;

    @Test
    void testDeleteExistingUser() throws Exception {
        Long existingUserId = 1L;

        User existingUsers = new User();
        existingUsers.setId(existingUserId);
        existingUsers.setName("existingUser");
        existingUsers.setSurname("existingSurname");
        existingUsers.setRole(RoleEnum.ROLE_ADMIN);
        existingUsers.setPasswordHash("existingPasswordHash");
        existingUsers.setPhoneNumber("1234567890");

        UserDetailsImpl userDetails = new UserDetailsImpl(existingUsers);
        String token = generateToken.generateJwtToken(userDetails);

        doNothing().when(userDeletionAdminService).deleteUser(existingUserId);

        mockMvc.perform(delete("/api/admin/v1/users/delete/" + existingUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteNonExistingUser() throws Exception {
        Long nonExistingUserId = 999L;
        Long existingUserId = 1L;

        User existingUsers = new User();
        existingUsers.setId(existingUserId);
        existingUsers.setName("existingUser");
        existingUsers.setSurname("existingSurname");
        existingUsers.setRole(RoleEnum.ROLE_ADMIN);
        existingUsers.setPasswordHash("existingPasswordHash");
        existingUsers.setPhoneNumber("1234567890");

        UserDetailsImpl userDetails = new UserDetailsImpl(existingUsers);
        String token = generateToken.generateJwtToken(userDetails);

        doThrow(new UserNotFoundException("Non-existing user"))
                .when(userDeletionAdminService).deleteUser(nonExistingUserId);

        mockMvc.perform(delete("/api/admin/v1/users/delete/" + nonExistingUserId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
