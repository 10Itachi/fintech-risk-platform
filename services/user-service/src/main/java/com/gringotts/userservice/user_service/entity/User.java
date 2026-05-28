package com.gringotts.userservice.user_service.entity;

import com.gringotts.userservice.user_service.enums.IsActive;
import com.gringotts.userservice.user_service.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

   /* @Column(unique = true, nullable = false)  if unique user code is needed
    private String userCode;*/

    @Column(nullable = false, unique = true)
    private String keycloakUserId;

    @Column(nullable = false)
    private String userName;

    @Column(unique = true)
    private String phoneNumber;

    /*@Column(nullable = false)
    private String passwordHash;*/

    @Column(unique = true,nullable = false)
    @Email
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    //@Enumerated(EnumType.STRING)
    @Enumerated(EnumType.STRING)
    private IsActive isActive;


    @Column(nullable = false)
    private LocalDateTime createdAt;
}
