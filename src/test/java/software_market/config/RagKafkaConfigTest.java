package software_market.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RagKafkaConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration.class))
            .withUserConfiguration(RagKafkaConfig.class)
            .withPropertyValues("spring.kafka.bootstrap-servers=localhost:9092");

    @Test
    void keepsDefaultJsonTemplateAndAddsRagStringTemplate() {
        runner.run(context -> {
            assertThat(context).hasBean("kafkaTemplate");
            assertThat(context).hasBean("ragKafkaTemplate");
            assertThat(context.getBean("kafkaTemplate")).isInstanceOf(KafkaTemplate.class);
            assertThat(context.getBean("ragKafkaTemplate")).isInstanceOf(KafkaTemplate.class);
        });
    }
}
