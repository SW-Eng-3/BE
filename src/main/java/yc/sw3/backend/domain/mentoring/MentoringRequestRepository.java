package yc.sw3.backend.domain.mentoring;

import org.springframework.data.jpa.repository.JpaRepository;
import yc.sw3.backend.domain.user.User;
import java.util.List;
import java.util.UUID;

public interface MentoringRequestRepository extends JpaRepository<MentoringRequest, UUID> {
    List<MentoringRequest> findAllByMentee(User mentee);
}
