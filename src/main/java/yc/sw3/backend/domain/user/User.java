package yc.sw3.backend.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_verified")
    private boolean isVerified;

    @Column(name = "restricted_until")
    private LocalDateTime restrictedUntil;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public void verify() {
        this.isVerified = true;
    }

    public void restrict(int days) {
        this.restrictedUntil = LocalDateTime.now().plusDays(days);
    }

    public boolean isRestricted() {
        return restrictedUntil != null && restrictedUntil.isAfter(LocalDateTime.now());
    }
}
