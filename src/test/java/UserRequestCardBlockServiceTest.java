import org.example.entity.Card;
import org.example.entity.RequestBlocking;
import org.example.entity.User;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.example.service.MessageResponse;
import org.example.service.UserRequestCardBlockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserRequestCardBlockServiceTest {
    @InjectMocks
    private UserRequestCardBlockService userRequestCardBlockService;

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void testRequestBlockCard_SuccessfulScenario() throws Exception{

        Long userId = 1L;
        Long cardId = 100L;

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setId(cardId);
        card.setOwner(user);
        card.setRequestBlocking(RequestBlocking.NO);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(cardRepository.findById(cardId)).willReturn(Optional.of(card));
        given(cardRepository.updateRequestBlocking(eq("YES"), anyLong(), anyLong())).willReturn(1);

        MessageResponse response = userRequestCardBlockService.requestBlockCard(user.getId(), card.getId());

        assertEquals(response.getMessage(), "Successfully requested to block the card.");
        assertEquals(response.getCode(), 200);
    }

    @Test
    void testRequestBlockCard_CardBelongsToAnotherUser() throws Exception{
        Long userId = 1L;
        Long anotherUserId = 100L;
        Long cardId = 100L;

        User user = new User();
        user.setId(userId);
        User anotherUser = new User();
        anotherUser.setId(anotherUserId);

        Card card = new Card();
        card.setId(cardId);
        card.setOwner(anotherUser);
        card.setRequestBlocking(RequestBlocking.NO);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(cardRepository.findById(anyLong())).willReturn(Optional.of(card));

        MessageResponse response = userRequestCardBlockService.requestBlockCard(user.getId(), card.getId());

        assertEquals(response.getMessage(), "Карточка принадлежит другому пользователю!");
        assertEquals(response.getCode(), 403);
    }

    @Test
    void testRequestBlockCard_UserNotFound() throws Exception{
        Long nonExistingUserId = 100L;
        Long cardId = 100L;

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        MessageResponse response = userRequestCardBlockService.requestBlockCard(nonExistingUserId, cardId);

        assertEquals(response.getMessage(), "The user has not been found!");
        assertEquals(response.getCode(), 404);
    }
}
