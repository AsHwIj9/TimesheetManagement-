package com.project.TimeSheetManagement;

import com.project.TimeSheetManagement.security.RoleProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@SpringBootApplication
@EnableConfigurationProperties(RoleProperties.class)
public class TimeSheetManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeSheetManagementApplication.class, args);
    }

}
