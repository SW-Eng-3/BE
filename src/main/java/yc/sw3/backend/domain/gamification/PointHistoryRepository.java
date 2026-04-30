package yc.sw3.backend.domain.gamification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, UUID> {
    List<PointHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
