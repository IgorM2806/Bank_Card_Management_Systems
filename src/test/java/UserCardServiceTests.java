import org.example.dto.CardBalanceDto;
import org.example.entity.Card;
import org.example.entity.User;
import org.example.repository.CardRepository;
import org.example.service.BalanceService;
import org.example.service.UserCardService;
import org.example.util.MaskCardNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserCardServiceTests {
    @Mock
    private CardRepository cardRepository;
    @InjectMocks
    private BalanceService balanceService;

    @InjectMocks
    private UserCardService userCardService;

    @Test
    public void testDepositCardBalance_successfullyAddAmount() throws Exception{
        Long cardId = 1L;
        BigDecimal initialBalance = new BigDecimal("1000");
        BigDecimal depositAmount = new BigDecimal("500");
        BigDecimal expectedNewBalance = new BigDecimal("1500");

        Card mockCard = new Card();
        mockCard.setId(cardId);
        mockCard.setBalance(initialBalance);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        BigDecimal result = balanceService.deposit(cardId, depositAmount);

        assertEquals(expectedNewBalance, result);
        verify(cardRepository).save(mockCard);
    }

    @Test
    public void testWithdrawCardBalance_successfullySubtractAmount() throws Exception{
        Long cardId = 1L;
        BigDecimal initialBalance = new BigDecimal("1000");
        BigDecimal withdrawAmount = new BigDecimal("500");
        BigDecimal expectedNewBalance = new BigDecimal("500");

        Card mockCard = new Card();
        mockCard.setId(cardId);
        mockCard.setBalance(initialBalance);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class)))
        .thenAnswer(invocation -> invocation.getArguments()[0]);

        BigDecimal result = balanceService.withdraw(cardId, withdrawAmount);
        assertEquals(expectedNewBalance, result);
        verify(cardRepository).save(mockCard);
    }

    @Test
    public void testViewBalancesForUser_returnsMaskedCardsWithBalances() throws Exception{
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setName("Current User");

        Card firstCard = new Card();
        firstCard.setCardNumber("1234567890123456");
        firstCard.setBalance(new BigDecimal("1000"));
        Card secondCard = new Card();
        secondCard.setCardNumber("6543210987654321");
        secondCard.setBalance(new BigDecimal("2000"));

        when(cardRepository.findAllByOwner(currentUser)).thenReturn(List.of(firstCard, secondCard));

        MaskCardNumber maskCardNumber = new MaskCardNumber();

        List<CardBalanceDto> expectedResult = Arrays.asList(
                new CardBalanceDto(maskCardNumber.maskCardNumber(firstCard.getCardNumber()), firstCard.getBalance()),
                new CardBalanceDto(maskCardNumber.maskCardNumber(secondCard.getCardNumber()), secondCard.getBalance())
        );

        List<CardBalanceDto> actualResult = userCardService.viewBalancesForUser(currentUser);

        assertEquals(expectedResult.size(), actualResult.size());
        for (int i = 0; i < expectedResult.size(); i++) {
            assertEquals(expectedResult.get(i).getCardNumber(), actualResult.get(i).getCardNumber());
            assertEquals(expectedResult.get(i).getBalance(), actualResult.get(i).getBalance());
        }
    }
}
