import org.example.Main;
import org.example.controller.AdminAddCardController;

import org.example.dto.CardResponseDTO;
import org.example.dto.CreateCardDTO;
import org.example.entity.*;
import org.example.service.AdminService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = {AdminAddCardController.class})
class AdminAddCardControllerValidTests {
    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminAddCardController adminAddCardController;


    private final String CARD_NUMBER = "1234567891234567";
    private final LocalDate EXPIRATION_DATE = LocalDate.now().plusYears(1);
    private final Status STATUS = Status.ACTIVE;
    private final BigDecimal BALANCE = BigDecimal.valueOf(100.00);
    private final RequestBlocking REQUEST_BLOCKING = RequestBlocking.NO;

    @Test
    void testAddCardToUser_Success() throws Exception {
        User user = new User("Ivan", "Ivanov", "Ivanovich", RoleEnum.ROLE_USER,
                "password", "1234567891");
        CreateCardDTO requestDto = new CreateCardDTO(CARD_NUMBER, EXPIRATION_DATE, STATUS, BALANCE);

        Card expectedCard = new Card(CARD_NUMBER, EXPIRATION_DATE, user, STATUS, BALANCE, REQUEST_BLOCKING);
        expectedCard.setId(1L);

        when(adminService.createCardForUser(user.getId(), CARD_NUMBER,
                EXPIRATION_DATE, STATUS, BALANCE)).thenReturn(expectedCard);
        ResponseEntity<CardResponseDTO> response = adminAddCardController.addCardToUser(user.getId(), requestDto);

        CardResponseDTO actualResponse = response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCard.getCardNumber(), actualResponse.getCardNumber());
        assertEquals(expectedCard.getExpirationDate(), actualResponse.getExpirationDate());
        assertEquals(expectedCard.getStatus(), actualResponse.getStatus());
        assertEquals(expectedCard.getBalance(), actualResponse.getBalance());
    }
}