package software_market.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    @Bean
    @ConditionalOnProperty(name = "websocket.server-endpoint-exporter", havingValue = "true", matchIfMissing = true)
    public ServerEndpointExporter serverEndpointExporter() {

        return new ServerEndpointExporter();

    }
}
