package software_market.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import software_market.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security 的当前用户主体。
 * 所有需要“当前登录人”的地方都通过 SecurityContextHolder 中的该对象获取，不再依赖独立 ThreadLocal。
 */
@Getter
public class LoginUser implements UserDetails {

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_OFFICIAL = "ROLE_OFFICIAL";

    private final Integer userId;
    private final String email;
    private final String password;
    private final String identity;
    private final boolean enabled;
    private final List<GrantedAuthority> authorities;

    public LoginUser(Integer userId, String email, String password, String identity, boolean enabled) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.identity = identity;
        this.enabled = enabled;
        this.authorities = List.copyOf(buildAuthorities(identity));
    }

    public static LoginUser from(User user) {
        return new LoginUser(
                user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getIdentity(),
                !Boolean.TRUE.equals(user.getDeleted())
        );
    }

    /**
     * 仅供测试/非 Web 线程构造一个最小用户主体
     */
    public static LoginUser forUserId(Integer userId) {
        return new LoginUser(userId, null, null, null, true);
    }

    private static List<GrantedAuthority> buildAuthorities(String identity) {
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority(ROLE_USER));
        if ("admin".equals(identity)) {
            list.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        } else if ("official".equals(identity)) {
            list.add(new SimpleGrantedAuthority(ROLE_OFFICIAL));
        }
        return list;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
