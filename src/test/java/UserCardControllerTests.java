import org.example.Main;
import org.example.controller.UserCardController;
import org.example.dto.CardBalanceDto;
import org.example.entity.Users;
import org.example.exception.CardNotFoundException;
import org.example.exception.InsufficientFundsException;
import org.example.exception.InvalidOperationException;
import org.example.exception.UserNotFoundException;
import org.example.repository.UserRepository;
import org.example.service.UserCardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.PAYMENT_REQUIRED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = UserCardControllerTests.class)
@AutoConfigureMockMvc
public class UserCardControllerTests {
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private CardBalanceDto cardBalanceDto;
    @MockitoBean
    private UserCardService userCardService;
    @Autowired
    private UserCardController userCardController;
    @Autowired
    private MockMvc mockMvc;


    @Test
    public void test_getCardsWithBalancesForUser_Success() throws Exception {
        long validUserId = 1L;
        Users mockCurrentUser = new Users();
        mockCurrentUser.setId(1L);
        when(userRepository.findById(validUserId)).thenReturn(Optional.of(mockCurrentUser));

        BigDecimal initialBalance = new BigDecimal("1000.00"); // Пример начального баланса
        CardBalanceDto cardBalanceDto = new CardBalanceDto("1234567891234567", initialBalance);
        List<CardBalanceDto> expectedResultList = Arrays.asList(cardBalanceDto);
        when(userCardService.viewBalancesForUser(any())).thenReturn(expectedResultList);

        ResponseEntity<List<CardBalanceDto>> response = userCardController.getCardsWithBalancesForUser(validUserId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody()); // Убедимся, что тело ответа существует
        assertEquals(expectedResultList.size(), response.getBody().size());
    }

    @Test
    public void test_getCardsWithBalancesForUser_UserNotFound() throws Exception {
        // Arrange
        Long invalidUserId = 999L; // Некорректный ID

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        try {

            userCardController.getCardsWithBalancesForUser(invalidUserId);

            fail("Expected a UserNotFoundException to be thrown.");
        } catch(UserNotFoundException e) {
            assertEquals("User not found!", e.getMessage());
        }
    }

    @Test
    public void depositCardBalanceTests()  throws Exception {
        Long validCardId = 1L;
        BigDecimal initialBalance = new BigDecimal("1000.00");

        doNothing().when(userCardService).depositCardBalance(validCardId, initialBalance);

        MockHttpServletRequestBuilder requestBuilder = put("/api/v1/cards/change-deposit/" + validCardId)
                .param("amountChange", initialBalance.toString())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }

    @Test
    public void test_depositCardBalance_CardNotFound() throws Exception {
        // Arrange
        Long invalidCardId = 999L;
        BigDecimal anyPositiveAmount = new BigDecimal("100");

        doThrow(new CardNotFoundException("Карточка с указанным ID не найдена"))
                .when(userCardService).depositCardBalance(invalidCardId, anyPositiveAmount);

        // Act & Assert
        MockHttpServletRequestBuilder request = put("/api/v1/cards/change-deposit/" + invalidCardId)
                .param("amountChange", anyPositiveAmount.toString())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound()) // Ожидается ошибка Not Found (404)
                .andDo(print()); // Для удобства вывода полной информации о запросе и ответе
    }

    @Test
    public void test_depositCardBalance_NegativeAmount() throws Exception {
        // Arrange
        Long validCardId = 1L;
        BigDecimal negativeAmount = new BigDecimal("-100");

        doThrow(new InvalidOperationException("Negative amount is not allowed")).when(userCardService).depositCardBalance(validCardId, negativeAmount);

        // Act & Assert
        MockHttpServletRequestBuilder request = put("/api/v1/cards/change-deposit/" + validCardId)
                .param("amountChange", negativeAmount.toString())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest()) // Ожидается Bad Request (400)
                .andDo(print()); // Для удобного отображения подробностей запроса и ответа
    }

    // Тестируем успешное снятие средств
    @Test
    public void testWithdrawBalanceSuccess() throws Exception {
        // Arrange
        long cardId = 1L;
        BigDecimal amountWithdraw = new BigDecimal("100");

        Mockito.doNothing().when(userCardService).withdrawCardBalance(Mockito.anyLong(), Mockito.any(BigDecimal.class));

        // Act
        ResponseEntity<Object> response = userCardController.withdrawBalance(cardId, amountWithdraw);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testWithdrawBalanceInsufficientFunds() throws Exception {
        // Arrange
        long cardId = 1L;
        BigDecimal amountWithdraw = new BigDecimal("1000");

        Mockito.doThrow(new InsufficientFundsException("Недостаточно средств")).when(userCardService).withdrawCardBalance(Mockito.anyLong(), Mockito.any(BigDecimal.class));

        // Act
        ResponseEntity<Object> response = userCardController.withdrawBalance(cardId, amountWithdraw);

        // Assert
        assertEquals(PAYMENT_REQUIRED, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("Недостаточно средств", response.getBody());
    }


}
