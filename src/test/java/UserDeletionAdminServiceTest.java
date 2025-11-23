import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.repository.UserRepository;
import org.example.service.UserDeletionAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class UserDeletionAdminServiceTest {
    @InjectMocks
    private UserDeletionAdminService userDeletionAdminService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = standaloneSetup(userDeletionAdminService).build(); // Создаем MockMvc самостоятельно
    }


    @Test
    void testDeleteExistingUser() throws Exception{
        Long validUserId = 1L;
        User user = new User();

        when(userRepository.findById(validUserId)).thenReturn(Optional.of(user));

        userDeletionAdminService.deleteUser(validUserId);

        verify(userRepository).delete(eq(user));
        verify(entityManager).lock(eq(user), eq(LockModeType.PESSIMISTIC_READ));
    }
    @Test
    @WithMockUser(username = "admin", roles = "ROLE_ADMIN")
    void testDeleteNonExistentUser() throws Exception{
        Long invalidUserId = 1L;
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDeletionAdminService.deleteUser(invalidUserId))
                .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("The user with the specified ID was not found!");
    }
}
