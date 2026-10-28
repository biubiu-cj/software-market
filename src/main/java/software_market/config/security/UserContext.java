package software_market.config.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * 业务层获取“当前用户”的兼容层。
 *
 * 不再自己维护 ThreadLocal：一律从 SecurityContextHolder 读取，
 * 认证信息由 JwtAuthenticationFilter 写入、由 Spring Security 在请求结束时清理。
 */
public final class UserContext {

    private UserContext() {
    }

    public static Integer getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getPrincipal() instanceof LoginUser loginUser ? loginUser : null;
    }

    public static boolean isAuthenticated() {
        return getLoginUser() != null;
    }

    /**
     * 仅供单元测试或非 Web 线程使用；Web 请求请勿调用。
     */
    public static void setUserId(Integer userId) {
        LoginUser loginUser = LoginUser.forUserId(userId);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                loginUser, null, loginUser.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
