import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.entity.*;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.example.service.AdminService;
import org.example.service.UserRequestCardBlockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = {UserBlockCardControllerTests.class})
@AutoConfigureMockMvc
@Profile("dev")
class UserBlockCardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRequestCardBlockService cardBlockService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CardRepository cardRepository;

    @MockitoBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;


    // Тест успешного сценария блокировки карты
    @Test
    void testAnyCardBlockingSuccess() throws Exception {

        Long userId = 1L;
        Long cardId = 1L;
        User mockedUser = new User();
        mockedUser.setId(userId);
        Cards mockedCard = new Cards();
        mockedCard.setOwner(mockedUser);
        mockedCard.setId(cardId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockedUser));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(mockedCard));
        when(cardRepository.updateRequestBlocking(any(String.class),
                any(Long.class), any(Long.class))).thenReturn(1);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", userId); // Произвольный пользователь
        requestBody.put("cardId", cardId); // Произвольная карта
        String jsonContent = objectMapper.writeValueAsString(requestBody);

        mockMvc.perform(post("/api/v1/cards/block")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())                               // Должен возвращаться статус 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Карточка успешно заблокирована"))
                .andExpect(jsonPath("$.code").value(200));
    }

    // Тест проверки AccessDeniedException
    @Test
    void testAccessDeniedWhenBlockingNonExistentUser() throws Exception {
        Long nonexistentUserId = 999L; // Несуществующий пользователь
        Long validCardId = 3L; // Карта принадлежит кому-то другому

        List<User> usersInDb = Arrays.asList(
                new User("Alice", "Doe",
                        "Doevna", RoleEnum.USER, "temp", "1234567890"),
                new User("Bob", "Doe", "Doevich",
                        RoleEnum.USER, "temp", "1234567890")
        );

        for (User u : usersInDb) {
            given(userRepository.findById(u.getId())).willReturn(Optional.of(u));
        }

        given(userRepository.findById(nonexistentUserId)).willReturn(Optional.empty());

        // Формируем тело запроса в формате JSON
        Map<String, Object> body = new HashMap<>();
        body.put("userId", nonexistentUserId);
        body.put("cardId", validCardId);

        String jsonContent = objectMapper.writeValueAsString(body);

        ResultActions result = mockMvc.perform(post("/api/v1/cards/block")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent));

        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь не найден!"));
    }

}
