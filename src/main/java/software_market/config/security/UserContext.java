package software_market.config.security;

import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private static final ThreadLocal<Integer> userHolder = new ThreadLocal<>();
    public static Integer getUserId() {
        return userHolder.get();
    }

    public static void setUserId(Integer userId) {
        userHolder.set(userId);
    }

    public static void clear() {
        userHolder.remove();
    }

}
