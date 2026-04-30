package yc.sw3.backend.domain.gamification;

public enum PointReason {
    POST_CREATED("게시글 작성"),
    COMMENT_CREATED("댓글 작성"),
    MENTORING_COMPLETED("멘토링 완료"),
    DAILY_CHECKIN("출석 체크"),
    ADMIN_ADJUSTMENT("관리자 조정");

    private final String description;

    PointReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
