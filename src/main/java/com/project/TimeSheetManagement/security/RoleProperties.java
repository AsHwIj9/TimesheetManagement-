package com.project.TimeSheetManagement.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "roles")
public class RoleProperties {
    private String adminRole;
    private String userRole;
}