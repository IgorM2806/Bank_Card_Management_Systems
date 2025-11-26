package org.example.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.example.dao.UserDao;

import java.util.Optional;

@Component
public class UserDaoImpl implements UserDao {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public boolean checkIfUserExistsByPhoneNumber(String phoneNumber) {
        TypedQuery<Long> query = entityManager
                .createQuery("SELECT COUNT(u.id) FROM User u WHERE u.phoneNumber = :phoneNumber", Long.class);
        query.setParameter("phoneNumber", phoneNumber);
        long count = query.getSingleResult();
        return count > 0;
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long userId) {
        return Optional.ofNullable(entityManager.find(User.class, userId));
    }
}
