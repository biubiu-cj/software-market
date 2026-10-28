package software_market.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import software_market.service.impl.CustomUserDetailsService;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 允许跨域的前端来源，必须显式配置。
     * 开发环境默认 localhost:5173；生产环境通过 CORS_ALLOWED_ORIGINS 环境变量覆盖。
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;

    // ====================== 认证组件 ======================

    /**
     * 标准 DaoAuthenticationProvider：UserDetailsService + BCryptPasswordEncoder。
     * 继续兼容数据库里已有的 BCrypt 密码，不需要迁移密码数据。
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(daoAuthenticationProvider);
    }

    // ====================== 过滤链 ======================

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider,
                                                           CustomUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtProvider, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
                /*
                 * 关闭 CSRF：本服务是无状态 JWT API，认证凭证放在 Authorization 头中，
                 * 浏览器不会自动携带，因此不存在基于 Cookie 的 CSRF 攻击面。
                 */
                .csrf(csrf -> csrf.disable())

                // CORS 来源显式配置，禁止 "*" + allowCredentials 的组合
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 无状态：不创建 HttpSession，身份只来自每次请求的 Access Token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 统一异常响应：401 / 403 都是 Result JSON，而不是 HTML 错误页
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                .authorizeHttpRequests(auth -> auth
                        // CORS 预检
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ---------- 公开接口 ----------
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/adminLogin",
                                "/api/auth/register",
                                "/api/auth/validateEmail",
                                "/api/auth/refresh",
                                "/api/auth/modifyPassword").permitAll()
                        // 微信登录跳转与回调（由微信服务器发起，无法携带 JWT）
                        .requestMatchers(HttpMethod.GET,
                                "/api/auth/wechat/authorize",
                                "/api/auth/wechat/callback").permitAll()
                        // OAuth2 第三方协议端点：使用 app_id/app_secret 或 OAuth2 access_token 自校验
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/connect/check",
                                "/api/auth/connect/token",
                                "/api/auth/connect/userinfo").permitAll()
                        // 公开头像静态资源
                        .requestMatchers("/avatar/**").permitAll()
                        // WebSocket 握手：token 在握手 query 中，由 ChatWebSocket 自行校验
                        .requestMatchers("/websocket/**").permitAll()
                        // 错误分发
                        .requestMatchers("/error").permitAll()

                        // ---------- 管理员接口 ----------
                        .requestMatchers(HttpMethod.GET,
                                "/api/auth/admininfo",
                                "/api/auth/getSues",
                                "/api/auth/getAllFeedback").hasAnyRole("ADMIN", "OFFICIAL")
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/addAdmin",
                                "/api/auth/changePassword",
                                "/api/auth/updateAdminInfo",
                                "/api/auth/deleteUser",
                                "/api/auth/deleteAdmin",
                                "/api/auth/showUsers",
                                "/api/auth/noViolation",
                                "/api/auth/violation",
                                "/api/auth/adminPost",
                                "/api/auth/resolveFeedback").hasAnyRole("ADMIN", "OFFICIAL")

                        // ---------- 其余接口全部需要登录 ----------
                        .anyRequest().authenticated()
                );

        // JWT 过滤器放在用户名密码过滤器之前
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JwtAuthenticationFilter 作为 Bean 会被 Spring Boot 额外注册进 Servlet 容器，
     * 这里关闭自动注册，保证它只存在于 Security 过滤链中、每个请求只执行一次。
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    // ====================== CORS ======================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (origins.isEmpty()) {
            throw new IllegalStateException("app.cors.allowed-origins 不能为空");
        }
        if (origins.contains("*")) {
            throw new IllegalStateException(
                    "app.cors.allowed-origins 不允许使用 \"*\"，请显式配置前端域名");
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
