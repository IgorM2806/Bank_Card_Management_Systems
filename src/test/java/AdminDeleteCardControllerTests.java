import org.example.Main;
import org.example.controller.AdminDeleteCardController;
import org.example.exception.CardNotFoundException;
import org.example.service.CardDeletionAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@ContextConfiguration(classes = {AdminDeleteCardController.class})
@AutoConfigureMockMvc
@Profile("dev")
public class AdminDeleteCardControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardDeletionAdminService cardDeletionAdminService;

    @Test
    void testSuccessfulCardDeletion() throws Exception {
        mockMvc.perform(delete("/api/v1/cards/delete/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
        verify(cardDeletionAdminService).deleteCard(anyLong());
    }

    @Test
    void testCardNotFoundError() throws Exception {
        doThrow(new CardNotFoundException("Карта не найдена"))
                .when(cardDeletionAdminService)
                .deleteCard(anyLong());

        mockMvc.perform(delete("/api/v1/cards/delete/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is("Карта не найдена")));
    }
}
