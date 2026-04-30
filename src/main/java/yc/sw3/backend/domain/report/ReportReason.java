package yc.sw3.backend.domain.report;

public enum ReportReason {
    SPAM("스팸/영리적 홍보"),
    INAPPROPRIATE_CONTENT("부적절한 콘텐츠"),
    ABUSIVE_LANGUAGE("욕설/비하 발언"),
    HARASSMENT("괴롭힘/사생활 침해"),
    FRAUD("사기/허위 정보"),
    OTHER("기타");

    private final String description;
    ReportReason(String description) { this.description = description; }
    public String getDescription() { return description; }
}
