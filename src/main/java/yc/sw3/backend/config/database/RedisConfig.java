package yc.sw3.backend.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.JpaRepository;

@Configuration
@EnableRedisRepositories(
    basePackages = "yc.sw3.backend.repository.redis",
    excludeFilters = @Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JpaRepository.class
    )
)
public class RedisConfig {
}
