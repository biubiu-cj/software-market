package software_market.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oauth2_app")
public class Oauth2App {
    @TableId("app_id")
    private String appId;

    private String appSecret;
    private String appName;
    private String appIcon;
    private String description;
    private String redirectUri;
    private LocalDateTime createdAt;
}
