package yc.sw3.backend.domain.community;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
