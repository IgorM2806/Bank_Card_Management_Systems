import org.example.entity.Card;
import org.example.repository.CardRepository;
import org.example.service.CardTransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardTransferServiceTests {
    @Mock
    private CardRepository cardRepository;
    @InjectMocks
    private CardTransferService cardTransferService;

    @Test
    public void  testTransferAmountBetweenValidCards() throws Exception{
        Card fromCard = new Card();
        fromCard.setCardNumber("1234567890123456");
        fromCard.setBalance(BigDecimal.valueOf(1000));

        Card toCard = new Card();
        toCard.setCardNumber("9876543210987654");
        toCard.setBalance(BigDecimal.valueOf(2000));

        when(cardRepository.findByCardNumber(anyString())).thenReturn(Optional.of(fromCard))
                .thenReturn(Optional.of(toCard));
        cardTransferService.transferAmountBetweenCards(1234567890123456L, 9876543210987654L,
                BigDecimal.valueOf(500));

        assertEquals(BigDecimal.valueOf(500), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(2500), toCard.getBalance());
    }

    @Test
    public void testTransferWithIdenticalCardNumbersThrowsError() throws Exception{
        Card singleCard = new Card();
        singleCard.setCardNumber("1234567890123456");
        singleCard.setBalance(BigDecimal.valueOf(1000));

        try {
            cardTransferService.transferAmountBetweenCards(1234567890123456L, 1234567890123456L,
                    BigDecimal.valueOf(500));
            fail("Expected exception not thrown");
        }catch (IllegalArgumentException ex){
            assertEquals("Источник и цель перевода не могут совпадать.", ex.getMessage());
        }
    }
}
