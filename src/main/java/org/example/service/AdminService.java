package org.example.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdminService {

    private final UserRepository userRepository;


    private final CardRepository cardRepository;

    @Transactional
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public Users createUser(String name, String surname, String patronymic, RoleEnum role,
                            String passwordHash, String phoneNumber) {
        passwordHash = "temp";
        System.out.println("Обращение в сервис createUser!");
        List<Users> existingUsers = userRepository.findByNameAndSurnameAndPatronymic(name, surname, patronymic);

        if (!existingUsers.isEmpty()) {
            throw  new DuplicateUserException("Пользователь с такими данными уже существует!");
        }
        Users user = new Users(name, surname, patronymic, role, passwordHash, phoneNumber);
        return  userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public Users updateUserWithPasswordHash(Long userId, String password) {
        System.out.println("Сервис - updateUserWithPasswordHash");
        try {
            Optional<Users> existingUserOptional = userRepository.findById(userId);
            if (!existingUserOptional.isPresent()) {
                throw new RuntimeException("Пользователь с указанным идентификатором не найден");
            }
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

            Users existingUser = existingUserOptional.get();
            existingUser.setPasswordHash(passwordHash);
            return userRepository.save(existingUser);
        }catch (RuntimeException e) {
            e.printStackTrace();
            return  null;
        }
    }

    @Transactional
    //@PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public Cards createCardForUser(Long userId, String cardNumber,
                                   LocalDate expirationDate, Status status, BigDecimal balance) {
        System.out.println("Обращение в сервис createCardForUser!");
        Optional<Users> usersOptional = userRepository.findById(userId);
        if (!usersOptional.isPresent()) {
            throw new UserNotFoundException("User not found");
        }
        Users user = usersOptional.get();

        boolean exists = cardRepository.existsByCardNumber(cardNumber);
        if (exists) {
            throw new DuplicateCardNumberException("Карточка с таким номером уже существует");
        }

        Cards card = new Cards(cardNumber, expirationDate, user, status, balance, RequestBlocking.NO);
        return cardRepository.save(card);
    }

}
