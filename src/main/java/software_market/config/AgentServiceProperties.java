package software_market.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "agent.service")
public class AgentServiceProperties {

    private boolean enabled = true;

    private String baseUrl = "http://127.0.0.1:8090";

    private String token = "";

    private Duration connectTimeout = Duration.ofSeconds(2);

    private Duration readTimeout = Duration.ofSeconds(60);
}
