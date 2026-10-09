package software_market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("refresh_token")
public class RefreshToken {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Integer userId;

    private String token;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField("is_used")
    private Boolean isUsed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
