package com.project.management.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class RoleValidator {

    public boolean hasRole(String role) {
        Collection<?> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        return authorities.stream().anyMatch(grantedAuthority -> grantedAuthority.toString().equals(role));
    }
}