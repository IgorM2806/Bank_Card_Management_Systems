import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.dto.CreateUserDTO;
import org.example.entity.RoleEnum;
import org.example.entity.Users;
import org.example.exception.DuplicateUserException;
import org.example.repository.UserRepository;
import org.example.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
public class AdminAddUserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void testAdminAddUserController_Success() throws Exception {
        var userDto = new CreateUserDTO("Tester1", "Testerov1",
                "Testerovich1", RoleEnum.ADMIN, "temp", "9033430101");

        var jsonContent = objectMapper.writeValueAsString(userDto);

        Users createdUser = new Users();
        when(adminService.createUser(anyString(), anyString(), anyString(), any(RoleEnum.class),
                anyString(), anyString()))
                .thenReturn(createdUser);

        mockMvc.perform(post("/admin/users/add")
                .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminAddUserController_EmptyFields() throws Exception {
        String firstName = null;
        String lastName = null;
        String patronymic = "Testerovich1";
        RoleEnum role = RoleEnum.ADMIN;
        String passwordHash = "temp";
        String phoneNumber = "9033430101";

        CreateUserDTO userDto = new CreateUserDTO(firstName, lastName, patronymic, role, passwordHash, phoneNumber);
        String content = objectMapper.writeValueAsString(userDto);

        ResultActions result = mockMvc.perform(post("/admin/users/add")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        String responseContent = result.andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(responseContent);

        assertThat(root.path("message").asText(), equalTo("Ошибка валидации."));

        JsonNode errorsNode = root.path("errors");
        assertThat(errorsNode, notNullValue());

        assertThat(errorsNode.path("firstName").asText(), equalTo("Имя обязательно для заполнения."));
        assertThat(errorsNode.path("surname").asText(), equalTo("Имя обязательно для заполнения."));
    }

    @Test
    void testUpdateUserWithPasswordHash_ControllerSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        String newPassword = "newSecurePassword";

        Users mockUpdatedUser = new Users();
        mockUpdatedUser.setId(userId); // установим ID обновленного пользователя

        when(adminService.updateUserWithPasswordHash(anyLong(), anyString())).thenReturn(mockUpdatedUser);

        // Act
        MockHttpServletRequestBuilder request = post("/admin/users/password/update")
                .param("userId", userId.toString())
                .param("password", newPassword)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        // Assert
        mockMvc.perform(request)
                .andExpect(status().isOk())   // Проверяем успешный статус HTTP
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    void testAdminAddUserController_DuplicateUser() throws Exception {
        String firstName = "Tester1";
        String lastName = "Testerov1";
        String patronymic = "Testerovich1";
        RoleEnum role = RoleEnum.ADMIN;
        String passwordHash = "temp";
        String phoneNumber = "9033439001";

        DuplicateUserException exception = new DuplicateUserException("Пользователь с такими данными уже существует");

        doThrow(exception)
                .when(adminService)
                .createUser(firstName, lastName, patronymic, role, passwordHash, phoneNumber);

        CreateUserDTO userDto = new CreateUserDTO(firstName, lastName, patronymic, role, passwordHash, phoneNumber);

        String content = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(
                        post("/admin/users/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content))
                .andExpect(status().isConflict())
                .andExpect(result ->
                        assertThat(result.getResponse().getContentAsString(),
                                containsString("Пользователь с такими данными уже существует")));

        // Проверяем, что моковочный сервис был вызван
        verify(adminService).createUser(firstName, lastName, patronymic, role, passwordHash, phoneNumber);
    }
}
