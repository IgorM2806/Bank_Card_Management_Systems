package org.example.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.entity.*;
import org.example.exception.DuplicateCardNumberException;
import org.example.exception.DuplicateUserException;
import org.example.exception.UserNotFoundException;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdminService {

    private final UserRepository userRepository;


    private final CardRepository cardRepository;

    @Transactional
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public User createUser(String name, String surname, String patronymic, RoleEnum role,
                           String plainPassword, String phoneNumber) { // Параметр plainPassword теперь принимает обычный текст
        System.out.println("Обращение в сервис createUser!");

        // Проверяем наличие дубликатов по телефону
        Optional<User> existingUsers = userRepository.findByPhoneNumber(phoneNumber);
        if (!existingUsers.isEmpty()) {
            throw new DuplicateUserException("Пользователь с таким номером телефона уже существует!");
        }

        // Хэшируем пароль сразу же при создании пользователя
        String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        User user = new User(name, surname, patronymic, role, passwordHash, phoneNumber);
        return userRepository.save(user); // Сохраняем пользователя с хэшированным паролем
    }

    // Метод для обновления пароля отдельного пользователя (если потребуется)
    @Transactional
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public User updateUserPassword(Long userId, String newPlainPassword) {
        System.out.println("Обновление пароля пользователя");
        Optional<User> existingUserOptional = userRepository.findById(userId);
        if (!existingUserOptional.isPresent()) {
            throw new RuntimeException("Пользователь с указанным идентификатором не найден");
        }

        // Получаем текущего пользователя
        User existingUser = existingUserOptional.get();

        // Генерируем новый хэшированный пароль
        String newPasswordHash = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());

        // Обновляем пароль
        existingUser.setPasswordHash(newPasswordHash);
        return userRepository.save(existingUser);
    }

    @Transactional
    //@PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public Cards createCardForUser(Long userId, String cardNumber,
                                   LocalDate expirationDate, Status status, BigDecimal balance) {
        System.out.println("Обращение в сервис createCardForUser!");
        Optional<User> usersOptional = userRepository.findById(userId);
        if (!usersOptional.isPresent()) {
            throw new UserNotFoundException("User not found");
        }
        User user = usersOptional.get();

        boolean exists = cardRepository.existsByCardNumber(cardNumber);
        if (exists) {
            throw new DuplicateCardNumberException("Карточка с таким номером уже существует");
        }

        Cards card = new Cards(cardNumber, expirationDate, user, status, balance, RequestBlocking.NO);
        return cardRepository.save(card);
    }

}
