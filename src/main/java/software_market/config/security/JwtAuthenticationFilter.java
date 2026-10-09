package software_market.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UnauthorizedEntryPoint unauthorizedEntryPoint;

    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/adminLogin",
            "/api/auth/register",
            "/api/auth/validateEmail",
            "/api/auth/refresh",
            "/api/auth/modifyPassword",
            "/avatar/",
            "/websocket/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ 1. OPTIONS 必须放行（CORS预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 2. 解析 token（如果有）
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            try {
                Integer userId = jwtProvider.getUserId(token);
                UserContext.setUserId(userId);
            } catch (Exception e) {
                unauthorizedEntryPoint.handle(request, response, e);
                return;
            }
        }

        // ✅ 3. SecurityConfig 中已配置 permitAll() 的路径：无 token 也放行
        if (isWhiteList(path) || path.startsWith("/api/auth/")) {
            try {
                filterChain.doFilter(request, response);
            } finally {
                UserContext.clear();
            }
            return;
        }

        // ✅ 4. 其他路径必须有 token
        if (!StringUtils.hasText(token)) {
            unauthorizedEntryPoint.handle(
                    request,
                    response,
                    new RuntimeException("未携带Token")
            );
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private String resolveToken(HttpServletRequest request) {

        String bearer = request.getHeader("Authorization");

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return request.getParameter("token");
    }
}