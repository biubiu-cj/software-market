package software_market.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import software_market.controller.AdminController;
import software_market.controller.UserController;
import software_market.dto.response.TokenPair;
import software_market.entity.User;
import software_market.mapper.UserMapper;
import software_market.service.AdminService;
import software_market.service.UserService;
import software_market.service.impl.CustomUserDetailsService;
import software_market.vo.user.CurrentUserVO;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 过滤链级别测试：真实的 JwtAuthenticationFilter + SecurityFilterChain + 统一 401/403 JSON。
 */
@WebMvcTest(controllers = {UserController.class, AdminController.class})
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtProvider.class, CustomUserDetailsService.class,
        RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
        WebSecurityConfig.class})
@TestPropertySource(properties = {
        "jwt.secret=c29mdHdhcmUtbWFya2V0LXVuaXQtdGVzdC1zZWNyZXQta2V5LTAxMjM0NTY3ODk=",
        "jwt.access-token-expire=7d",
        "jwt.refresh-token-expire=30d",
        "jwt.oauth2-access-token-expire=1h",
        "app.cors.allowed-origins=http://localhost:5173"
})
class SecurityChainTest {

    private static final String SECRET =
            "c29mdHdhcmUtbWFya2V0LXVuaXQtdGVzdC1zZWNyZXQta2V5LTAxMjM0NTY3ODk=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        when(userMapper.selectById(1)).thenReturn(user(1, "student"));
        when(userMapper.selectById(2)).thenReturn(user(2, "admin"));
        when(userService.getCurrentUserInfo(any())).thenReturn(new CurrentUserVO());
        when(userService.register(any())).thenReturn(new TokenPair("access", "refresh"));
        when(adminService.showUsers(any())).thenReturn(List.of());
    }

    @Test
    void registerIsPublic() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void protectedEndpointWithoutTokenReturns401Json() throws Exception {
        mockMvc.perform(get("/api/auth/info"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void validAccessTokenCanAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/info")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwtProvider.generateAccessToken("1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // UserContext 从 SecurityContextHolder 读取到了 token 中的 userId
        verify(userService).getCurrentUserInfo(1);
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        JwtProvider expiredProvider = new JwtProvider(SECRET, "-1s", "30d", "1h");

        mockMvc.perform(get("/api/auth/info")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + expiredProvider.generateAccessToken("1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void tamperedTokenReturns401() throws Exception {
        String token = jwtProvider.generateAccessToken("1");
        String tampered = token.substring(0, token.length() - 2) + "AA";

        mockMvc.perform(get("/api/auth/info")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenCannotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/info")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwtProvider.generateRefreshToken("1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void normalUserGets403OnAdminEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/showUsers")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwtProvider.generateAccessToken("1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/showUsers")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwtProvider.generateAccessToken("2"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void corsPreflightSucceeds() throws Exception {
        mockMvc.perform(options("/api/auth/info")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    void requestDoesNotLeakAuthenticationIntoNextRequest() throws Exception {
        mockMvc.perform(get("/api/auth/info")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwtProvider.generateAccessToken("1")))
                .andExpect(status().isOk());

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "请求结束后 SecurityContext 必须被清理");
    }

    private User user(int id, String identity) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(identity + id + "@example.com");
        user.setIdentity(identity);
        user.setDeleted(false);
        return user;
    }
}
