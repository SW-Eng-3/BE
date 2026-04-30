package yc.sw3.backend.domain.report;

public enum ReportStatus {
    PENDING("접수됨"),
    PROCESSED("처리됨"),
    REJECTED("기각됨");

    private final String description;
    ReportStatus(String description) { this.description = description; }
    public String getDescription() { return description; }
}
