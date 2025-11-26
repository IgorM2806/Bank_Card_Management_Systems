package org.example.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.example.entity.*;
import org.example.exception.DuplicateCardNumberException;
import org.example.exception.DuplicateUserException;
import org.example.exception.UserNotFoundException;
import org.example.repository.CardRepository;
import org.example.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class AdminService {
    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final UserDaoImpl userDaoImpl;

    public AdminService(UserRepository userRepository, CardRepository cardRepository,  UserDaoImpl userDaoImpl) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.userDaoImpl = userDaoImpl;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public User createUser(String name, String surname, String patronymic, RoleEnum role,
                           String plainPassword, String phoneNumber) {

        if (userDaoImpl.checkIfUserExistsByPhoneNumber(phoneNumber)) {
            throw new DuplicateUserException("Пользователь с таким номером телефона уже существует!");
        }

        // Хэшируем пароль сразу же при создании пользователя
        String passwordHash = bCryptPasswordEncoder.encode(plainPassword);

        User user = new User(name, surname, patronymic, role, passwordHash, phoneNumber);
        return userRepository.save(user); // Сохраняем пользователя с хэшированным паролем
    }

    // Метод для обновления пароля отдельного пользователя (если потребуется)
    @Transactional
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public User updateUserPassword(Long userId, String newPlainPassword) {
        Optional<User> existingUserOptional = userDaoImpl.findUserById(userId);
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
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public Card createCardForUser(Long userId, String cardNumber,
                                  LocalDate expirationDate, Status status, BigDecimal balance) {
        System.out.println("Обращение в сервис createCardForUser!");
        Optional<User> usersOptional = userDaoImpl.findUserById(userId);
        if (!usersOptional.isPresent()) {
            throw new UserNotFoundException("User not found");
        }
        User user = usersOptional.get();

        boolean exists = cardRepository.existsByCardNumber(cardNumber);
        if (exists) {
            throw new DuplicateCardNumberException("Карточка с таким номером уже существует");
        }

        Card card = new Card(cardNumber, expirationDate, user, status, balance, RequestBlocking.NO);
        return cardRepository.save(card);
    }
}
