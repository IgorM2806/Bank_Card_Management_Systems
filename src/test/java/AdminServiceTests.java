
import org.example.Main;
import org.example.dao.UserDao;
import org.example.entity.Card;
import org.example.entity.RoleEnum;
import org.example.entity.Status;
import org.example.entity.User;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.example.service.AdminService;
import org.example.service.UserDaoImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserDaoImpl userDaoImpl;

    @InjectMocks
    private AdminService adminService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void testCreateNewUniqueUser_Successful() throws Exception {
        String phoneNumber = "1234567890";

        when(userDaoImpl.checkIfUserExistsByPhoneNumber(phoneNumber)).thenReturn(false);

        User savedUser = new User("John", "Doe", "Ivanovich",
                RoleEnum.ROLE_USER, "hashed_password", phoneNumber);
        when(userRepository.save(any())).thenReturn(savedUser);

        // Выполняем операцию создания пользователя
        User newUser = adminService.createUser(
                "John",
                "Doe",
                "Ivanovich",
                RoleEnum.ROLE_USER,
                "password",
                phoneNumber
        );
        assertThat(newUser).isNotNull(); // Пользователь успешно создан
        assertThat(newUser.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    void testUpdateExistingUserPassword_Successful() throws Exception {
        Long validUserId = 1L;
        String newPassword = "new_password";
        String oldPassword = "old_password";

        User mockUser = new User();
        mockUser.setId(validUserId);
        mockUser.setPasswordHash(encoder.encode(oldPassword));

        when(userDaoImpl.findUserById(validUserId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        User updatedUser = adminService.updateUserPassword(validUserId, newPassword);

        verify(userRepository).save(mockUser);
        assertFalse(updatedUser.getPasswordHash().equals(oldPassword));
        assertTrue(BCrypt.checkpw(newPassword, updatedUser.getPasswordHash())) ;
    }

    @Test
    void testCreateCardWithUniqueNumber_Successful() throws Exception {
        Long validUserId = 1L;
        String cardNumber = "1234567890123456";

        User mockUser = new User();
        mockUser.setId(validUserId);

        when(userDaoImpl.findUserById(validUserId)).thenReturn(Optional.of(mockUser));
        when(cardRepository.existsByCardNumber(cardNumber)).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Card createdCard = adminService.createCardForUser(
                validUserId,
                cardNumber,
                LocalDate.now(),
                Status.ACTIVE,
                new BigDecimal("1000.00")
        );

        assertThat(createdCard.getCardNumber()).isEqualTo(cardNumber);      // Верный номер карты
        assertThat(createdCard.getOwner()).isEqualTo(mockUser);

    }
}
