package software_market_agent.rag;

import org.junit.jupiter.api.Test;
import software_market_agent.config.AgentProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QueryRewriteServiceTest {

    private final QueryRewriteService service = new QueryRewriteService(new AgentProperties(), null, null);

    @Test
    void takesFirstMeaningfulLineAndRemovesFences() {
        assertEquals("校园卡丢失补办流程", service.normalize("```text\n校园卡丢失补办流程\n```"));
    }

    @Test
    void stripsQuotesAndKnownPrefixes() {
        assertEquals("图书馆座位预约规则", service.normalize("「图书馆座位预约规则」"));
        assertEquals("补考申请流程", service.normalize("查询：补考申请流程"));
    }

    @Test
    void truncatesToConfiguredLength() {
        AgentProperties properties = new AgentProperties();
        properties.getRag().setRewriteMaxChars(10);
        QueryRewriteService limited = new QueryRewriteService(properties, null, null);

        assertEquals("1234567890", limited.normalize("123456789012345"));
    }

    @Test
    void blankReturnsNull() {
        assertNull(service.normalize("   \n```\n```"));
        assertNull(service.normalize(null));
    }
}
