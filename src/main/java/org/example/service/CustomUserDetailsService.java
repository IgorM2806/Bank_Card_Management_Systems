package org.example.service;

import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    private UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        // Найти пользователя по номеру телефона
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Создаем единственный authority на основе роли пользователя
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        // Возвращаем объект UserDetails с данным полномочием
        return new org.springframework.security.core.userdetails.User(
                user.getPhoneNumber(),
                user.getPasswordHash(),
                true, // активен
                true, // срок действия аккаунта не истек
                true, // срок действия учетных данных не истек
                true, // аккаунт не заблокирован
                List.of(authority) // список из единственного полномочия
        );
    }
}

