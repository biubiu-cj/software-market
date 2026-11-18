package software_market.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列消费者配置。
 *
 * DLT 里可能是反序列化失败的消息，因此这里用 byte[] 消费，只做日志告警，
 * 不再走 JSON 反序列化，也不再次投递 DLT（避免 DLT 无限套娃）。
 */
@Slf4j
@Configuration
public class KafkaDltConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> dltListenerContainerFactory(
            KafkaProperties kafkaProperties, SslBundles sslBundles) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(sslBundles));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        DefaultKafkaConsumerFactory<String, byte[]> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new ByteArrayDeserializer());

        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                (record, ex) -> log.error("DLT 消息处理失败 topic={} partition={} offset={}",
                        record.topic(), record.partition(), record.offset(), ex),
                new FixedBackOff(0L, 0L)));
        return factory;
    }
}