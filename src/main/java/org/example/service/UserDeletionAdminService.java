package org.example.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDeletionAdminService {

    private final EntityManager em;

    private final UserRepository userRepository;

    public UserDeletionAdminService(EntityManager em, UserRepository userRepository) {
        this.em = em;
        this.userRepository = userRepository;
    }


    /**
     * Удаляет пользователя по его идентификатору и автоматически удаляет все его карты.
     *
     * @param userId идентификатор пользователя.
     * @throws UserNotFoundException если пользователь с указанным идентификатором не найден.
     */

    @Transactional
    //@PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Пользователь найден: " + user.toString());
            em.lock(user, LockModeType.PESSIMISTIC_READ);
            userRepository.delete(user);
        } else {
            System.out.println("Пользователь с указанным ID не найден.");
            throw new UserNotFoundException("Пользователь с указанным ID не найден");
        }
    }

}
