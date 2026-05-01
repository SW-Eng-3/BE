package yc.sw3.backend.domain.mentoring;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MentoringStatus {
    REQUESTED("신청됨"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
    COMPLETED("완료됨"),
    CANCELLED("취소됨");

    private final String description;
}
