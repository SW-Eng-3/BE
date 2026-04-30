package yc.sw3.backend.dto;

import lombok.*;
import yc.sw3.backend.domain.user.*;
import java.util.UUID;

public class AuthDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignupRequest {
        private String email;
        private String password;
        private String name;
        private Role role;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificationRequest {
        private String email;
        private String code;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TokenResponse {
        private String accessToken;
        private UUID userId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProfileResponse {
        private UUID userId;
        private String email;
        private String name;
        private Role role;
        private Major major;
        private String majorDescription;
        private String currentCompany;
        private JobCategory jobCategory;
        private String jobCategoryDescription;
        private Country country;
        private String countryDescription;
        private String bio;
        private int points;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileUpdateRequest {
        private Major major;
        private String currentCompany;
        private JobCategory jobCategory;
        private Country country;
        private String bio;
    }
}
