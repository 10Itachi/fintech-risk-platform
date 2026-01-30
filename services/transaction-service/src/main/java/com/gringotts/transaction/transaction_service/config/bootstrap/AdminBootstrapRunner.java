package com.gringotts.transaction.transaction_service.config.bootstrap;

import com.gringotts.transaction.transaction_service.security.iam.User;
import com.gringotts.transaction.transaction_service.security.iam.IsActive;
import com.gringotts.transaction.transaction_service.security.iam.Role;
import com.gringotts.transaction.transaction_service.security.iam.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean adminExist = userRepository.existsByRole(Role.ADMIN);
        if (!adminExist) {
            User admin = new User();
            admin.setUserName("admin");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setEmail("admin@gmail.com");
            admin.setPhoneNumber("9999999999");
            admin.setRole(Role.ADMIN);
            admin.setIsActive(IsActive.ACTIVE);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
        }
    }
}
