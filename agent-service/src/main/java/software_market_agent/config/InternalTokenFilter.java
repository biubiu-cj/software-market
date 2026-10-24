package software_market_agent.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 内部接口鉴权。agent-service 不暴露给前端或公网。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InternalTokenFilter extends OncePerRequestFilter {

    private final AgentProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String expected = properties.getInternalToken();
        String actual = request.getHeader("X-Internal-Token");
        if (!StringUtils.hasText(expected)) {
            log.error("AGENT_SERVICE_TOKEN 未配置，拒绝所有内部请求");
            unauthorized(response);
            return;
        }
        if (!expected.equals(actual)) {
            log.warn("内部接口 Token 不匹配: uri={}", request.getRequestURI());
            unauthorized(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":\"UNAUTHORIZED\",\"message\":\"invalid internal token\"}");
    }
}
