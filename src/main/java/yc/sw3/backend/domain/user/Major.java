package yc.sw3.backend.domain.user;

public enum Major {
    COMPUTER_SCIENCE("컴퓨터공학"),
    SOFTWARE_ENGINEERING("소프트웨어공학"),
    INFORMATION_SECURITY("정보보안"),
    DATA_SCIENCE("데이터사이언스"),
    ELECTRICAL_ENGINEERING("전자공학"),
    MECHANICAL_ENGINEERING("기계공학"),
    BUSINESS_ADMINISTRATION("경영학"),
    ECONOMICS("경제학"),
    DESIGN("디자인"),
    OTHER("기타");

    private final String description;
    Major(String description) { this.description = description; }
    public String getDescription() { return description; }
}
