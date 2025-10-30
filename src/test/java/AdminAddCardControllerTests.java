import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.controller.AdminAddCardController;
import org.example.dto.CreateCardDTO;
import org.example.entity.Cards;
import org.example.entity.Status;
import org.example.exception.DuplicateCardNumberException;
import org.example.service.AdminService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = {AdminAddCardController.class})
@AutoConfigureMockMvc
@Profile("dev")
class AdminAddCardControllerTests {

    // Mock сервиса для взаимодействия с бизнес-логикой
    @MockitoBean
    private AdminService adminService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    /**
     * Простой положительный тест успешного добавления карты пользователю
     */
    @Test
    void testAddCardToUser_Success() throws Exception {

        // Данные DTO для отправки POST-запроса
        var cardDto = new CreateCardDTO("1234567890000461", LocalDate.now(),
                Status.ACTIVE, new BigDecimal(10000));
                      // баланс

        // Создаем объект Card, который вернет сервис
        var createdCard = new Cards(1L);
        createdCard.setCardNumber(cardDto.getCardNumber());
        createdCard.setExpirationDate(cardDto.getExpirationDate());
        createdCard.setStatus(cardDto.getStatus());
        createdCard.setBalance(cardDto.getBalance());
        // Настройка поведения мока
        when(adminService.createCardForUser(1L, cardDto.getCardNumber(), cardDto.getExpirationDate(),
                cardDto.getStatus(), cardDto.getBalance())).thenReturn(createdCard);

        // Выполняем POST-запрос
        ResultActions result = this.mockMvc.perform(post("/admin/cards/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andDo(print());
        assertAll("Проверка успешности добавления карты",
                () -> result.andExpect(status().isOk()),
                () -> result.andExpect(jsonPath("$.cardNumber", is("1234567890000461"))),
                () -> result.andExpect(jsonPath("$.balance", is(10000))),
                () ->result.andExpect(jsonPath("$.status", is(Status.ACTIVE.toString()))));

    }

    @Test
    void testAddCardToUser_Failed() throws Exception {
        var cardDto = new CreateCardDTO("", LocalDate.now(),
                Status.ACTIVE, new BigDecimal(10000));

        // Настраиваем мок так, чтобы служба бросала исключение
        doThrow(new IllegalArgumentException("Invalid card number"))
                .when(adminService)
                .createCardForUser(1L, "", LocalDate.now(), Status.ACTIVE, new BigDecimal(10000));

        // Выполняем POST-запрос
        ResultActions result = this.mockMvc.perform(post("/admin/cards/{userId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardDto)));

        // Ждем статус isInternalServerError (500)
        result.andExpect(status().isInternalServerError());

        // Дополнительно проверяем сообщение об ошибке
        result.andExpect(content().string(containsString("Invalid card number")));
    }

    @Test
    void testConflictWhenCardAlreadyExists () throws Exception {
        Long userId = 1L;
        LocalDate date = LocalDate.now();
        BigDecimal amount = new BigDecimal("100");
        Status status = Status.ACTIVE;

        DuplicateCardNumberException exception =
                new DuplicateCardNumberException("Карточка с таким номером уже существует!");

        when(adminService.createCardForUser(userId, "1234567890000463", date, status, amount))
                .thenThrow(exception);

        CreateCardDTO cardDTO = new CreateCardDTO("1234567890000463", date, status, amount);
        String content = objectMapper.writeValueAsString(cardDTO);

        mockMvc.perform(post("/admin/cards/{userId}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isConflict())
        .andExpect(result -> assertThat(result.getResponse().getContentAsString())
                .contains("Карточка с таким номером уже существует!"));
    }
}