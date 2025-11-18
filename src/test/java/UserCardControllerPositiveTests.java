import org.example.Main;
import org.example.dto.CardBalanceDto;
import org.example.entity.*;
import org.example.exception.InsufficientFundsException;
import org.example.exception.UserNotFoundException;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.example.service.UserCardService;
import org.example.service.UserDetailsImpl;
import org.example.util.GenerateJwtTokenTest;
import org.example.util.MaskCardNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = UserCardControllerPositiveTests.class)
@AutoConfigureMockMvc
public class UserCardControllerPositiveTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenerateJwtTokenTest generateJwtTokenTest;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserCardService userCardService;

    @MockitoBean
    private CardRepository cardRepository;

    private MaskCardNumber maskCardNumber =  new MaskCardNumber();


    private Long existingCardId = 1234567890123456L;

    private User existingUser;
    private UserDetailsImpl existingUserDetails;
    private String token;

    @BeforeEach
    public void setUp() throws Exception {
        existingUser = new User(
                "existingUser",
                "existingUser",
                "existingUser",
                RoleEnum.ROLE_ADMIN,
                "password",
                "1234567890");
        existingUserDetails = new UserDetailsImpl(existingUser);
        token = generateJwtTokenTest.generateJwtToken(existingUserDetails);
    }

    @Test
    void testSuccessfulDepositCardBalance() throws Exception{
        String requestUrl = "/api/user/v1/cards/change-deposit/" +  existingCardId;

        given(userCardService.depositCardBalance(anyLong(), any(BigDecimal.class)))
                .willReturn(new BigDecimal("1000.00"));

        mockMvc.perform(put(requestUrl)
                        .param("amountChange", "1000")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.balance", is("1000.00")))
                .andExpect(jsonPath("$.message", is("Пополнение выполнено успешно!")));
    }

    @Test
    void testSuccessfulWithdrawCardBalance() throws Exception{
        String requestUrl = "/api/user/v1/cards/withdraw/" +  existingCardId;

        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal withdrawAmount = new BigDecimal("500.00");
        BigDecimal expectedNewBalance  = initialBalance.subtract(withdrawAmount);

        Card existingCard = new Card("1234567890123456",
                LocalDate.now().plusYears(1),
                existingUser,
                Status.ACTIVE,
                initialBalance,
                RequestBlocking.NO);

        when(cardRepository.findByCardNumber(existingCard.getCardNumber())).thenReturn(Optional.of(existingCard));
        when(userCardService.withdrawCardBalance(eq(existingCard.getId()), eq(withdrawAmount))).thenReturn(expectedNewBalance);

        BigDecimal result = userCardService.withdrawCardBalance(existingCard.getId(), withdrawAmount);

        assertEquals(expectedNewBalance, result);
    }

    @Test
    void test_getCardsWithBalancesForUser_Successful() throws Exception{

        LocalDate testDate = LocalDate.now();
        Status status = Status.ACTIVE;
        RequestBlocking requestBlocking = RequestBlocking.NO;

        Card existingCard1 = new Card("1234567890123456",  testDate, existingUser, status,
                new BigDecimal("1000.00"), requestBlocking);
        existingCard1.setId(1L);
        Card existingCard2 = new Card("1234567890123457",  testDate, existingUser, status,
                new BigDecimal("2000.00"), requestBlocking);
        existingCard2.setId(2L);

        List<Card> cards = new ArrayList<>();
        cards.add(existingCard1);
        cards.add(existingCard2);

        System.out.println("Cards before adding:");
        cards.stream().map(Card::toString).forEach(System.out::println);

        when(cardRepository.findAllByOwner(argThat(u -> u.equals(existingUser)))).thenReturn(cards);

        doReturn(cards.stream()
                .map(card -> new CardBalanceDto(new MaskCardNumber()
                        .maskCardNumber(card.getCardNumber()), card.getBalance()))
                .collect(Collectors.toList()))
                .when(userCardService).viewBalancesForUser(any(User.class));

        List<CardBalanceDto> result = userCardService.viewBalancesForUser(existingUser);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCardNumber()).isEqualTo("**** **** **** 3456");
        assertThat(result.get(0).getBalance()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(result.get(1).getCardNumber()).isEqualTo("**** **** **** 3457");
        assertThat(result.get(1).getBalance()).isEqualTo(new BigDecimal("2000.00"));
    }

    @Test
    void testGetCardsWithBalancesForNonExistingCard() throws Exception{
        doThrow(new UserNotFoundException("Card not found!")).when(userCardService).viewBalancesForUser(any(User.class));

        mockMvc.perform(get("/api/user/v1/cards/balances/" + 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDepositCardBalanceWithInvalidCardIdType() throws Exception{
        String requestUrl = "/api/user/v1/cards/change-deposit/" +  "invalid";
        mockMvc.perform(put(requestUrl)
                        .param("amountChange", "1000")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", is("Проверьте введенные данные!")));
    }

    @Test
    void testDepositCardBalanceWithNegativeAmount() throws Exception{
        String requestUrl = "/api/user/v1/cards/change-deposit/" +  existingCardId;
        mockMvc.perform(put(requestUrl)
                        .param("amountChange", "-1000")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        is("Проверьте введенные данные!")));
    }

    @Test
    void testWithdrawBalance_NoSuchCard_ReturnNotFound() throws Exception {
        long invalidCardId = 999999999999999999L;
        BigDecimal validAmount = new BigDecimal(1000);

        when(userCardService.withdrawCardBalance(invalidCardId, validAmount))
                .thenThrow(new NoSuchElementException("Card not found!"));

        mockMvc.perform(put("/api/user/v1/cards/withdraw/" + invalidCardId)
                        .param("amountWithdraw", String.valueOf(validAmount))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Card not found!")));
    }

    @Test
    void testWithdrawBalance_InsufficientFunds_ReturnBadRequest() throws Exception {
        long validCardId = 1L;
        BigDecimal overdraftAmount = new BigDecimal(1000);

        doThrow(new InsufficientFundsException("Payment Required!"))
                .when(userCardService).withdrawCardBalance(validCardId, overdraftAmount);

        mockMvc.perform(put("/api/user/v1/cards/withdraw/" + validCardId)
                        .param("amountWithdraw", String.valueOf(overdraftAmount))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.message", is("Payment Required!")));

    }
}