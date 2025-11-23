
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.entity.Card;
import org.example.entity.RequestBlocking;
import org.example.entity.RoleEnum;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.repository.CardRepository;
import org.example.service.MessageResponse;
import org.example.service.UserDetailsImpl;
import org.example.service.UserRequestCardBlockService;
import org.example.service.UserUnblockCardService;
import org.example.util.GenerateJwtTokenTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
public class UserBlockCardControllerTests {

    @Autowired
    private GenerateJwtTokenTest generateJwtTokenTest;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRequestCardBlockService userRequestCardBlockService;

    @MockitoBean
    private UserUnblockCardService userUnblockCardService;

    @MockitoBean
    private CardRepository cardRepository;

    @Test
    void shouldSuccessfullyBlockCard() throws Exception {

        String token = prepareUserAndGetJWTToken(1L);

        Map<String, Object> body = new HashMap<>();
        body.put("userId", 1L);
        body.put("cardId", 1234567890123456L);

        given(userRequestCardBlockService.requestBlockCard(anyLong(), anyLong()))
                .willReturn(new MessageResponse("Запрос на блокировку успешно отправлен!", 200));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/api/user/v1/cards/block")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(asJson(body)))
                .andExpect(status().isOk());

        resultActions.andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Запрос на блокировку успешно отправлен!"));
    }

    public static String asJson(Object obj) throws JsonProcessingException {
        if (obj == null) {
            throw new IllegalArgumentException("Объект для преобразования в JSON не может быть пустым.");
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

    @Test
    void shouldHandleUserNotFoundException() throws Exception {
        String token = prepareUserAndGetJWTToken(1L);

        Map<String, Object> body = new HashMap<>();
        body.put("userId", 1L);
        body.put("cardId", 1234567890123456L);

        doThrow(new UserNotFoundException("User not found"))
                .when(userRequestCardBlockService).requestBlockCard(anyLong(), anyLong());

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/api/user/v1/cards/block")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(asJson(body)))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void shouldPreventDuplicateBlockRequestsForSameCard() throws Exception {
        String token = prepareUserAndGetJWTToken(1L);
        Card existingCard = new Card();
        User existingUser = new User("User", "User", "User",
                RoleEnum.ROLE_USER, "encodedPassword", "1234567890");
        existingUser.setId(1L);

        existingCard.setId(1L);
        existingCard.setRequestBlocking(RequestBlocking.YES);
        existingCard.setOwner(existingUser);

        given(cardRepository.findById(1L)).willReturn(Optional.of(existingCard));

        given(userRequestCardBlockService.requestBlockCard(anyLong(), anyLong()))
                .willReturn(new MessageResponse("Запрос на блокировку уже отправлен для этой карты!", 400));

        Map<String, Object> body = new HashMap<>();
        body.put("userId", 1L); // совпадающий пользователь-владелец карты
        body.put("cardId", 1L);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/api/user/v1/cards/block")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(asJson(body)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Запрос на блокировку уже отправлен для этой карты!"));
    }

    @Test
    void shouldSuccessfullyUnBlockCard() throws Exception {
        String token = prepareUserAndGetJWTToken(1L);

        Map<String, Object> body = new HashMap<>();
        body.put("userId", 1L);
        body.put("cardId", 1234567890123456L);

        given(userUnblockCardService.requestUnBlockCard(anyLong(), anyLong()))
                .willReturn(new MessageResponse("Запрос на разблокировку успешно отправлен!", 200));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/api/user/v1/cards/unblock")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(asJson(body)))
                .andExpect(status().isOk());

        resultActions.andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Запрос на разблокировку успешно отправлен!"));
    }

    @Test
    void shouldHandleUserNotFoundExceptionForUnBlock() throws Exception {
        String token = prepareUserAndGetJWTToken(1L);

        Map<String, Object> body = new HashMap<>();
        body.put("userId", 1L);
        body.put("cardId", 1234567890123456L);

        doThrow(new UserNotFoundException("User not found"))
                .when(userUnblockCardService).requestUnBlockCard(anyLong(), anyLong());

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/api/user/v1/cards/unblock")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(asJson(body)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldPreventDuplicateUnBlockRequestsForSameCard() throws Exception {
        String token = prepareUserAndGetJWTToken(1L);
        Card existingCard = new Card();
        User existingUser = new User("User", "User", "User",
                RoleEnum.ROLE_USER, "encodedPassword", "1234567890");
        existingUser.setId(1L);

        existingCard.setId(1L);
        existingCard.setRequestBlocking(RequestBlocking.NO);
        existingCard.setOwner(existingUser);

        given(cardRepository.findById(1L)).willReturn(Optional.of(existingCard));

        given(userUnblockCardService.requestUnBlockCard(anyLong(), anyLong()))
                .willReturn(new MessageResponse("Запрос на разблокировку уже отправлен для этой карты!", 400));

        Map<String, Object> body = new HashMap<>();
        body.put("userId", 1L); // совпадающий пользователь-владелец карты
        body.put("cardId", 1L);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/api/user/v1/cards/unblock")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(asJson(body)));

        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Запрос на разблокировку уже отправлен для этой карты!"));
    }



    private String prepareUserAndGetJWTToken(long userId) throws Exception {
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("User");
        existingUser.setSurname("User");
        existingUser.setPatronymic("User");
        existingUser.setRole(RoleEnum.ROLE_USER);
        existingUser.setPasswordHash("encodedPassword");
        existingUser.setPhoneNumber("1234567890");

        UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);

        return generateJwtTokenTest.generateJwtToken(userDetails);
    }
}