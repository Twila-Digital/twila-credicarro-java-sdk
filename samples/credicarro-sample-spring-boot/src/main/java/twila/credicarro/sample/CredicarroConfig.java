package twila.credicarro.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import twila.credicarro.CredicarroClient;
import twila.credicarro.config.CredicarroEnvironment;

@Configuration
public class CredicarroConfig {

    @Bean(destroyMethod = "close")
    public CredicarroClient credicarroClient(
            @Value("${credicarro.client-id}") String clientId,
            @Value("${credicarro.client-secret}") String clientSecret,
            @Value("${credicarro.environment:STAGING}") CredicarroEnvironment environment) {
        return CredicarroClient.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .environment(environment)
                .build();
    }
}
