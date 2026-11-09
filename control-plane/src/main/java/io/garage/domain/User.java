package io.garage.domain;

import java.time.Instant;
import java.util.Optional;

import io.garage.domain.enums.UserRoleEnum;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String username;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(nullable = false)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public UserRoleEnum role; // ADMIN, DEVELOPER

    public Instant createdAt = Instant.now();

    protected User() {

    }

    public User(String username, String email, String passwordHash, UserRoleEnum role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public static Optional<User> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }
}


