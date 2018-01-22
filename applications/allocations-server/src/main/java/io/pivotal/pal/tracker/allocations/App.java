package io.pivotal.pal.tracker.allocations;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.client.RestOperations;

import java.util.TimeZone;

@EnableCircuitBreaker
@EnableEurekaClient
@SpringBootApplication
@ComponentScan({"io.pivotal.pal.tracker.allocations", "io.pivotal.pal.tracker.restsupport"})
@EnableResourceServer
@EnableWebSecurity
public class App {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(App.class, args);
    }

    @Bean
    ProjectClient projectClient(
        RestOperations restOperations,
        @Value("${registration.server.endpoint}") String registrationEndpoint
    ) {
        return new ProjectClient(restOperations, registrationEndpoint, redisTemplate());
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {

        return new JedisConnectionFactory();

    }

    @Bean
    @Qualifier("MyRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();

        template.setConnectionFactory(jedisConnectionFactory());

        template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));

        return template;

    }
}
