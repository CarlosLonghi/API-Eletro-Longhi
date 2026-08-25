package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.repository.UserRepository;
import br.com.carloslonghi.eletrolonghi.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User save(User user) {
        String password = user.getPassword();
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(false);

        return userRepository.save(user);
    }

    public Page<User> findAll(String name, String email, Role role, Boolean enabled, Pageable pageable) {
        return userRepository.findAll(UserSpecification.withFilters(name, email, role, enabled), pageable);
    }

    public Optional<User> updateRole(Long id, Role role) {
        return userRepository.findById(id).map(user -> {
            user.setRole(role);
            return userRepository.save(user);
        });
    }

    public Optional<User> updateStatus(Long id, boolean enabled) {
        return userRepository.findById(id).map(user -> {
            user.setEnabled(enabled);
            return userRepository.save(user);
        });
    }
}
