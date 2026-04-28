package yc.sw3.backend.domain.mentoring;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MentorScheduleRepository extends JpaRepository<MentorSchedule, UUID> {
}
