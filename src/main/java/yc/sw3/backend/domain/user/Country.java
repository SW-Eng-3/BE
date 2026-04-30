package yc.sw3.backend.domain.user;

public enum Country {
    KOREA("대한민국"),
    USA("미국"),
    JAPAN("일본"),
    CHINA("중국"),
    GERMANY("독일"),
    UK("영국"),
    CANADA("캐나다"),
    OTHER("기타");

    private final String description;
    Country(String description) { this.description = description; }
    public String getDescription() { return description; }
}
