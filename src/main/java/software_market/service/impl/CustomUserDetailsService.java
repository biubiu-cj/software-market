package software_market.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import software_market.config.security.LoginUser;
import software_market.entity.User;
import software_market.mapper.UserMapper;

/**
 * 标准 UserDetailsService：
 * - 登录时按邮箱加载（loadUserByUsername）
 * - 每次请求校验 JWT 后按 userId 加载，保证封禁/删除/角色变更即时生效
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("用户不存在");
        }
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, username.trim())
        );
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return LoginUser.from(user);
    }

    public LoginUser loadUserById(Integer userId) throws UsernameNotFoundException {
        if (userId == null || userId <= 0) {
            throw new UsernameNotFoundException("用户不存在");
        }
        User user = userMapper.selectById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return LoginUser.from(user);
    }
}
