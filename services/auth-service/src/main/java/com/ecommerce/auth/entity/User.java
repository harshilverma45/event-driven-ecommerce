package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String role = "USER";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ✅ Single lifecycle hook for both insert and update
    @PrePersist
    @PreUpdate
    private void beforeSave() {

        LocalDateTime now = LocalDateTime.now();

        // Handle timestamps
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;

        // Default provider (backward compatibility)
        AuthProvider effectiveProvider =
                (this.provider == null) ? AuthProvider.LOCAL : this.provider;

        this.provider = effectiveProvider;

        // Validation logic
        if (effectiveProvider == AuthProvider.LOCAL) {
            if (this.password == null || this.password.isEmpty()) {
                throw new RuntimeException("LOCAL users must have a password");
            }
        }

        if (effectiveProvider == AuthProvider.GOOGLE) {
            if (this.password != null && !this.password.isEmpty()) {
                throw new RuntimeException("GOOGLE users must not have a password");
            }
        }
    }
}