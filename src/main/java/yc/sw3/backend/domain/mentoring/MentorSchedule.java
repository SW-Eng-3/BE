package yc.sw3.backend.domain.mentoring;

import jakarta.persistence.*;
import lombok.*;
import yc.sw3.backend.domain.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mentor_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MentorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private User mentor;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
}
