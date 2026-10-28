package software_market.config.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import software_market.service.impl.CustomUserDetailsService;

import java.io.IOException;

/**
 * 从 Authorization: Bearer 头解析 Access Token，校验通过后写入 SecurityContextHolder。
 *
 * 注意：
 * 1. 只接受 Access Token（subject/issuer/tokenType/过期时间/签名全部校验），
 *    Refresh Token 和 OAuth2 Token 都会在这里被拒绝。
 * 2. 不接受 URL 参数传 token；唯一例外是 WebSocket 握手（由 ChatWebSocket 自行校验）。
 * 3. 校验失败不直接写响应，而是保持匿名，由 AuthorizationFilter 判定是否放行，
 *    再交给 RestAuthenticationEntryPoint 返回统一的 401 JSON。
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtProvider.parseAccessToken(token);
                LoginUser loginUser = userDetailsService.loadUserById(jwtProvider.getUserId(claims));

                UsernamePasswordAuthenticationToken authentication =
                        UsernamePasswordAuthenticationToken.authenticated(
                                loginUser, null, loginUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                log.debug("Access Token 校验失败: {}", e.getMessage());
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // SecurityContextHolderFilter 通常已清理，这里兜底，避免线程复用导致身份泄漏
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
