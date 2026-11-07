
package kutman.dev.Kforum.model;

import jakarta.persistence.*;
import kutman.dev.Kforum.model.DTO.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // почем тут юзеры тоже не используются
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private boolean blocked;

    public User(String username, String email, String hashedPassword,
                String name, String surname) {
        this.username = username;
        this.email = email;
        this.password = hashedPassword;
        this.name = name;
        this.surname = surname;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

    }

    public String getEmail() {
        return this.email;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Long getId() {
        return this.id;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
