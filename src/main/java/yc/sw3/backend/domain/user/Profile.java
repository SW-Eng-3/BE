package yc.sw3.backend.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Profile {

    @Id
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Major major;
    private String currentCompany;
    @Enumerated(EnumType.STRING)
    private JobCategory jobCategory;
    @Enumerated(EnumType.STRING)
    private Country country;
    
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Builder.Default
    @Column(nullable = false)
    private int points = 0;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addPoints(int amount) {
        this.points += amount;
    }

    public void update(Major major, String currentCompany, JobCategory jobCategory, Country country, String bio) {
        this.major = major;
        this.currentCompany = currentCompany;
        this.jobCategory = jobCategory;
        this.country = country;
        this.bio = bio;
    }
}
