package org.example.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws ServletException {
        // Создаем объект тела ответа
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis()); // Можно добавить timestamp
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", "У вас недостаточно прав для доступа к данному ресурсу.");
        body.put("path", request.getRequestURI()); // Используем RequestURI вместо всего пути (для упрощения)

        // Устанавливаем необходимые заголовки и контент
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        try {
            // Отправляем тело ответа в поток вывода
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }
}
