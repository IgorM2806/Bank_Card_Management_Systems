
import org.example.Main;
import org.example.service.UserDeletionAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = {AdminDeleteUserControllerTests.class})
@AutoConfigureMockMvc
@Profile("dev")
public class AdminDeleteUserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDeletionAdminService userDeletionAdminService;

    @BeforeEach
    void setUp() {
        reset(userDeletionAdminService);
    }

    @Test
    void testDeleteExistingUser() throws Exception {
        Long existingUserId = 1L;

        doNothing().when(userDeletionAdminService).deleteUser(existingUserId);

        mockMvc.perform(delete("/api/v1/users/delete/" + existingUserId))
                .andExpect(status().isNoContent());
    }
}
