package yc.sw3.backend.domain.mentoring;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import yc.sw3.backend.domain.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mentoring_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MentoringRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id")
    private User mentee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private User mentor;

    @Enumerated(EnumType.STRING)
    private MentoringStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public void updateStatus(MentoringStatus status) {
        this.status = status;
    }
}
