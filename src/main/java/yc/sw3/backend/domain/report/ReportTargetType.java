package yc.sw3.backend.domain.report;

public enum ReportTargetType {
    POST("게시글"),
    USER("사용자"),
    COMMENT("댓글");

    private final String description;
    ReportTargetType(String description) { this.description = description; }
    public String getDescription() { return description; }
}
