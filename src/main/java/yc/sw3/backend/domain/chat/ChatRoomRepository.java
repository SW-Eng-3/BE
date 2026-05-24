package yc.sw3.backend.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yc.sw3.backend.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Optional<ChatRoom> findByStudentAndSenior(User student, User senior);

    @Query("""
            SELECT r FROM ChatRoom r
            WHERE r.student = :user OR r.senior = :user
            ORDER BY COALESCE(r.lastMessageAt, r.updatedAt, r.createdAt) DESC
            """)
    List<ChatRoom> findMyRoomsOrderByLatest(@Param("user") User user);
}
