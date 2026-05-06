package yc.sw3.backend.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "yc.sw3.backend.domain",
    includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = org.springframework.data.jpa.repository.JpaRepository.class
    )
)
public class JpaConfig {
}
