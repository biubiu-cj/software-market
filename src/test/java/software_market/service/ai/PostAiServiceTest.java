package software_market.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import software_market.client.ai.AgentClient;
import software_market.config.AgentServiceProperties;
import software_market.config.AiProperties;
import software_market.config.security.UserContext;
import software_market.entity.post.Post;
import software_market.exception.UnauthorizedException;
import software_market.mapper.post.PostMapper;
import software_market.vo.ai.PostAiAnalysisVO;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostAiServiceTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void returnsServiceUnavailableWhenAgentFails() {
        Fixture fixture = new Fixture();
        when(fixture.agentClient.analyze(any())).thenReturn(null);

        UserContext.setUserId(1);
        PostAiAnalysisVO result = fixture.service.analyze(1);

        assertEquals(PostAiService.STATUS_SERVICE_UNAVAILABLE, result.getStatus());
        assertNotNull(result.getCitations());
        assertNotNull(result.getRelatedPosts());
        assertNotNull(result.getRelatedReplies());
        verify(fixture.agentClient).analyze(any());
    }

    @Test
    void requiresLogin() {
        Fixture fixture = new Fixture();
        assertThrows(UnauthorizedException.class, () -> fixture.service.analyze(1));
    }

    @Test
    void normalizesAndCachesReadyResult() {
        Fixture fixture = new Fixture();
        PostAiAnalysisVO agentResult = new PostAiAnalysisVO();
        agentResult.setStatus(PostAiService.STATUS_READY);
        agentResult.setConclusion("x".repeat(3000));
        when(fixture.agentClient.analyze(any())).thenReturn(agentResult);

        UserContext.setUserId(1);
        PostAiAnalysisVO result = fixture.service.analyze(1);

        assertEquals(2000, result.getConclusion().length());
        assertNotNull(result.getCitations());
        assertTrue(result.getCitations().isEmpty());
        verify(fixture.ops).set(anyString(), anyString(), any(Duration.class));
    }

    private static class Fixture {
        private final PostMapper postMapper = mock(PostMapper.class);
        private final AgentClient agentClient = mock(AgentClient.class);
        private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        private final ValueOperations<String, String> ops = mock(ValueOperations.class);
        private final PostAiService service;

        private Fixture() {
            Post post = new Post();
            post.setPostId(1);
            post.setTitle("测试帖子");
            post.setPtext("测试正文");
            post.setCategory("打听求助");
            post.setTag("测试");
            when(postMapper.selectById(1)).thenReturn(post);
            when(redis.opsForValue()).thenReturn(ops);
            when(ops.increment(anyString())).thenReturn(1L);
            when(ops.get(anyString())).thenReturn(null);
            when(redis.expire(anyString(), any(Duration.class))).thenReturn(true);

            AiProperties aiProperties = new AiProperties();
            aiProperties.setRateLimitPerMinute(5);
            aiProperties.setAnalysisCacheMinutes(30);
            AgentServiceProperties agentProperties = new AgentServiceProperties();
            agentProperties.setEnabled(true);

            this.service = new PostAiService(
                    postMapper, agentClient, redis, new ObjectMapper(), aiProperties, agentProperties
            );
        }
    }
}
