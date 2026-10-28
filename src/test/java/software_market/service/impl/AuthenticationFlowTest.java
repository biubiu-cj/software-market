package software_market.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import software_market.config.security.LoginUser;
import software_market.entity.User;
import software_market.mapper.UserMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 登录认证链：UserDetailsService + DaoAuthenticationProvider + BCrypt。
 * 数据库中的密码保持原有 BCrypt 格式，不需要迁移。
 */
class AuthenticationFlowTest {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    private final CustomUserDetailsService userDetailsService = new CustomUserDetailsService(userMapper);

    private AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        when(userMapper.selectOne(any())).thenReturn(
                user(7, "student", passwordEncoder.encode("secret123")));

        Authentication authentication = authenticationManager().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("user@example.com", "secret123"));

        assertTrue(authentication.isAuthenticated());
        LoginUser principal = (LoginUser) authentication.getPrincipal();
        assertEquals(7, principal.getUserId());
        assertTrue(principal.getAuthorities().stream()
                .anyMatch(a -> LoginUser.ROLE_USER.equals(a.getAuthority())));
    }

    @Test
    void loginFailsWithWrongPassword() {
        when(userMapper.selectOne(any())).thenReturn(
                user(7, "student", passwordEncoder.encode("secret123")));

        assertThrows(BadCredentialsException.class, () -> authenticationManager().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("user@example.com", "wrong")));
    }

    @Test
    void deletedUserCannotLogin() {
        User deleted = user(7, "student", passwordEncoder.encode("secret123"));
        deleted.setDeleted(true);
        when(userMapper.selectOne(any())).thenReturn(deleted);

        assertThrows(BadCredentialsException.class, () -> authenticationManager().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("user@example.com", "secret123")));
    }

    @Test
    void adminIdentityGetsAdminAuthority() {
        when(userMapper.selectOne(any())).thenReturn(
                user(9, "admin", passwordEncoder.encode("admin123")));

        Authentication authentication = authenticationManager().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("admin@example.com", "admin123"));

        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> LoginUser.ROLE_ADMIN.equals(a.getAuthority())));
    }

    private User user(int id, String identity, String encodedPassword) {
        User user = new User();
        user.setUserId(id);
        user.setEmail("user@example.com");
        user.setIdentity(identity);
        user.setPassword(encodedPassword);
        user.setDeleted(false);
        return user;
    }
}
