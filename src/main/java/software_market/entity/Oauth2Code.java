package software_market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oauth2_code")
public class Oauth2Code {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String code;
    private String appId;
    private Integer userId;
    private String scope;
    private String redirectUri;
    private String state;
    private Boolean isUsed;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
