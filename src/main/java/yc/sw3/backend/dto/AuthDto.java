package yc.sw3.backend.dto;

import lombok.*;
import yc.sw3.backend.domain.user.Role;
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
        private String major;
        private String currentCompany;
        private String jobCategory;
        private String country;
        private String bio;
    }
}
