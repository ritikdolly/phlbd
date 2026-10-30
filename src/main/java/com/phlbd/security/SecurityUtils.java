package com.phlbd.security;

import com.phlbd.entity.enums.RoleType;
import com.phlbd.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return principal;
    }

    public static String getCurrentUserId() {
        return getCurrentUserPrincipal().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUserPrincipal().getEmail();
    }

    public static RoleType getCurrentUserRole() {
        return getCurrentUserPrincipal().getRole();
    }

    public static boolean hasRole(RoleType role) {
        UserPrincipal principal = getCurrentUserPrincipal();
        return principal.getRole() == role;
    }

    public static boolean isOwner() {
        return hasRole(RoleType.ROLE_OWNER);
    }
}
