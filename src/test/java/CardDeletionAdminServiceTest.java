import org.example.exception.CardNotFoundException;
import org.example.repository.CardRepository;
import org.example.service.CardDeletionAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CardDeletionAdminServiceTest {
    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardDeletionAdminService cardDeletionAdminService;

    @Test
    public void testDeleteExistingCard() throws Exception {
        Long existingCardId = 1L;

        given(cardRepository.existsById(existingCardId)).willReturn(true);

        cardDeletionAdminService.deleteCard(existingCardId);

        assertThatNoException().isThrownBy(() -> cardDeletionAdminService.deleteCard(existingCardId));
    }

    @Test
    public void testDeleteNonExistentCard () throws Exception {
        Long nonExistentCardId = 1L;
        given(cardRepository.existsById(nonExistentCardId)).willReturn(false);

        assertThatExceptionOfType(CardNotFoundException.class).isThrownBy(() -> cardDeletionAdminService
                .deleteCard(nonExistentCardId)).withMessageContaining("Карта с указанным ID не найдена!");

    }
}
