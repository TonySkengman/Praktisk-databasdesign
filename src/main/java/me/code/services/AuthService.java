package me.code.services;

import java.util.UUID;

public class AuthService {

    private static UUID userId;

    public static UUID getUserId() {
        return userId;
    }

    public static void setUserId(UUID userId) {
        AuthService.userId = userId;
    }
}
