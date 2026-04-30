package yc.sw3.backend.domain.user;

public enum JobCategory {
    BACKEND_DEVELOPER("백엔드 개발자"),
    FRONTEND_DEVELOPER("프론트엔드 개발자"),
    MOBILE_DEVELOPER("모바일 앱 개발자"),
    DATA_ENGINEER("데이터 엔지니어"),
    AI_ML_ENGINEER("AI/ML 엔지니어"),
    PRODUCT_MANAGER("기획자/PM"),
    DESIGNER("디자이너"),
    MARKETER("마케터"),
    OTHER("기타");

    private final String description;
    JobCategory(String description) { this.description = description; }
    public String getDescription() { return description; }
}
