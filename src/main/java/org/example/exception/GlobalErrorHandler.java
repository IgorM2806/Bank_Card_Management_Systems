package org.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidOperationException.class)
    @ResponseBody
    public ErrorResponse handleInvalidOperationException(
            InvalidOperationException ex, HttpServletRequest request) {

        log.warn("Ошибка операции: {}, URI={}", ex.getMessage(), request.getRequestURI());

        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                "Операция невозможна",
                ex.getLocalizedMessage());
    }
    // Обработка общего исключения
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorResponse handleGlobalException(Exception ex) {
        ErrorResponse internalServerError = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error", ex.getLocalizedMessage());
        return internalServerError;
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseBody
    public ErrorResponse handleInsufficientFundsException(InsufficientFundsException ex) {
        return new ErrorResponse(HttpStatus.PAYMENT_REQUIRED.value(), "Payment Required",
                ex.getLocalizedMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CardNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleCardNotFoundException(CardNotFoundException ex) {
        log.error("Карточка не найдена: {}", ex.getMessage(), ex);

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateCardNumberException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateCardNumberException(DuplicateCardNumberException ex) {
        return new ErrorResponse(HttpStatus.CONFLICT.value(),
                "Карточка с таким номером уже существует.", ex.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleHttpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException ex) {
        return new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Resource not available.",
                ex.getMessage());
    }


    public class ErrorResponse {
        private int code;
        private String message;
        private String details;

        public ErrorResponse(int code, String message, String details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        // Геттеры и сеттеры...

        public int getCode() {return code;}

        public void setCode(int code) {this.code = code;}

        public String getMessage() {return message;}

        public void setMessage(String message) {this.message = message;}

        public String getDetails() {return details;}

        public void setDetails(String details) {this.details = details;}
    }

}
