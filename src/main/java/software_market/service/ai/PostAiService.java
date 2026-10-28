package software_market.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software_market.client.ai.AgentClient;
import software_market.config.AgentServiceProperties;
import software_market.config.AiProperties;
import software_market.config.security.UserContext;
import software_market.constant.RedisKeyConstant;
import software_market.dto.request.ai.PostAnalysisRequestDTO;
import software_market.entity.post.Post;
import software_market.exception.BizException;
import software_market.exception.UnauthorizedException;
import software_market.mapper.post.PostMapper;
import software_market.vo.ai.PostAiAnalysisVO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostAiService {

    public static final String STATUS_READY = "READY";
    public static final String STATUS_NO_HISTORY = "NO_HISTORY";
    public static final String STATUS_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    public static final String STATUS_MODEL_ERROR = "MODEL_ERROR";
    public static final String STATUS_DISABLED = "DISABLED";
    public static final String STATUS_RATE_LIMITED = "RATE_LIMITED";

    private static final String NOTICE = "以上内容来自站内历史讨论，仅供参考。";
    private static final int MAX_CONTENT_LENGTH = 8000;
    private static final int MAX_CONCLUSION_LENGTH = 2000;

    private final PostMapper postMapper;
    private final AgentClient agentClient;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final AiProperties aiProperties;
    private final AgentServiceProperties agentServiceProperties;

    public PostAiAnalysisVO analyze(Integer postId) {
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("请先登录");
        }

        String cacheKey = RedisKeyConstant.AI_ANALYSIS_CACHE + postId;
        PostAiAnalysisVO cached = readCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BizException("帖子不存在");
        }

        if (!agentServiceProperties.isEnabled()) {
            return degraded(STATUS_DISABLED, "AI 分析功能暂未启用。");
        }

        // 只有真正要调用 Agent 时才计入限流，缓存命中不消耗额度
        if (!checkRateLimit(userId)) {
            return degraded(STATUS_RATE_LIMITED, "AI 请求过于频繁，请稍后再试。");
        }

        PostAnalysisRequestDTO request = new PostAnalysisRequestDTO(
                post.getPostId(),
                post.getTitle(),
                truncate(post.getPtext(), MAX_CONTENT_LENGTH),
                post.getCategory(),
                post.getTag()
        );

        PostAiAnalysisVO result = agentClient.analyze(request);
        if (result == null) {
            return degraded(STATUS_SERVICE_UNAVAILABLE, "AI 助手暂时不可用，不影响浏览和评论。");
        }

        normalize(result);
        // 只缓存成功结果；NO_HISTORY / 服务异常不缓存，方便回填后立即重试
        if (STATUS_READY.equals(result.getStatus())) {
            writeCache(cacheKey, result);
        }
        return result;
    }

    private boolean checkRateLimit(Integer userId) {
        int limit = aiProperties.getRateLimitPerMinute();
        if (limit <= 0) {
            return true;
        }
        try {
            String key = RedisKeyConstant.AI_RATE_LIMIT + userId;
            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redis.expire(key, Duration.ofMinutes(1));
            }
            return count == null || count <= limit;
        } catch (Exception e) {
            // Redis 故障不能阻断 AI 分析
            log.warn("AI 限流检查失败，跳过限流: userId={}", userId, e);
            return true;
        }
    }

    private PostAiAnalysisVO readCache(String key) {
        try {
            String json = redis.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return null;
            }
            return objectMapper.readValue(json, PostAiAnalysisVO.class);
        } catch (Exception e) {
            try {
                redis.delete(key);
            } catch (Exception ignored) {
                // ignore
            }
            return null;
        }
    }

    private void writeCache(String key, PostAiAnalysisVO result) {
        try {
            redis.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(result),
                    Duration.ofMinutes(aiProperties.getAnalysisCacheMinutes())
            );
        } catch (Exception e) {
            log.warn("写入 AI 分析缓存失败: key={}", key, e);
        }
    }

    private void normalize(PostAiAnalysisVO result) {
        if (!StringUtils.hasText(result.getStatus())) {
            result.setStatus(STATUS_READY);
        }
        result.setConclusion(truncate(result.getConclusion(), MAX_CONCLUSION_LENGTH));
        if (result.getCitations() == null) {
            result.setCitations(new ArrayList<>());
        }
        if (result.getRelatedPosts() == null) {
            result.setRelatedPosts(new ArrayList<>());
        }
        if (result.getRelatedReplies() == null) {
            result.setRelatedReplies(new ArrayList<>());
        }
        if (!StringUtils.hasText(result.getNotice())) {
            result.setNotice(NOTICE);
        }
        if (!StringUtils.hasText(result.getGeneratedAt())) {
            result.setGeneratedAt(LocalDateTime.now().toString());
        }
    }

    private PostAiAnalysisVO degraded(String status, String conclusion) {
        PostAiAnalysisVO vo = new PostAiAnalysisVO();
        vo.setStatus(status);
        vo.setConclusion(conclusion);
        vo.setNotice(NOTICE);
        vo.setGeneratedAt(LocalDateTime.now().toString());
        return vo;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
