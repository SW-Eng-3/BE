package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yc.sw3.backend.domain.gamification.PointHistory;
import yc.sw3.backend.domain.gamification.PointHistoryRepository;
import yc.sw3.backend.domain.gamification.PointReason;
import yc.sw3.backend.domain.user.Profile;
import yc.sw3.backend.domain.user.ProfileRepository;
import yc.sw3.backend.domain.user.User;
import yc.sw3.backend.domain.user.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GamificationService {

    private final ProfileRepository profileRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    /**
     * 포인트를 적립/차감하고 히스토리를 기록합니다.
     * @param userId 대상 사용자 ID
     * @param amount 변동 금액 (양수는 적립, 음수는 차감)
     * @param reason 포인트 변동 사유
     */
    @Transactional
    public void awardPoints(UUID userId, int amount, PointReason reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        // 1. 프로필 점수 업데이트
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 프로필이 존재하지 않습니다. ID: " + userId));

        profile.addPoints(amount);

        // 2. 히스토리 기록 저장
        PointHistory history = PointHistory.builder()
                .user(user)
                .amount(amount)
                .reason(reason)
                .build();

        pointHistoryRepository.save(history);
    }

    /**
     * 포인트를 차감하거나 삭제합니다.
     * @param userId 대상 사용자 ID
     * @param amount 차감할 금액 (양수로 입력 시 차감됨)
     * @param reason 차감 사유
     */
    public void deductPoints(UUID userId, int amount, PointReason reason) {
        awardPoints(userId, -amount, reason);
    }
}
