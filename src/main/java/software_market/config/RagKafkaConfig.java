package software_market.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * RAG 索引事件专用 String 模板。
 * 与现有邮件/通知的 JSON KafkaTemplate 隔离，避免跨服务共享 DTO 类。
 *
 * 注意：只要容器里存在任意 KafkaTemplate，Spring Boot 就不会再自动创建默认模板，
 * 因此这里显式保留原来的 JSON 模板，避免邮件/通知功能失效。
 */
@Configuration
public class RagKafkaConfig {

    @Bean("kafkaTemplate")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public KafkaTemplate<String, Object> kafkaTemplate(
            @Qualifier("kafkaProducerFactory") ProducerFactory<?, ?> producerFactory) {
        return new KafkaTemplate<>((ProducerFactory) producerFactory);
    }

    /**
     * 不把这个 ProducerFactory 暴露成 Bean，避免触发
     * @ConditionalOnMissingBean(ProducerFactory) 导致 Spring Boot 默认生产者工厂不创建。
     */
    @Bean("ragKafkaTemplate")
    public KafkaTemplate<String, String> ragKafkaTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(config);
        return new KafkaTemplate<>(producerFactory);
    }
}
