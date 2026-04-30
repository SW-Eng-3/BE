package yc.sw3.backend.domain.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findAllByOrderByCreatedAtDesc();
    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);
    
    long countByTargetIdAndStatus(UUID targetId, ReportStatus status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status AND " +
           "(r.targetId = :userId AND r.targetType = 'USER' OR " +
           "EXISTS (SELECT p FROM Post p WHERE p.id = r.targetId AND p.author.id = :userId AND r.targetType = 'POST'))")
    long countByTargetUserIdAndStatus(@Param("userId") UUID userId, @Param("status") ReportStatus status);
}
