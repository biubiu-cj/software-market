package software_market.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    private String phone;

    private String email;

    // 密码哈希绝不能出现在任何 HTTP 响应里（getInfo 等接口会直接返回该实体）
    @JsonIgnore
    private String password;

    private String name;

    private String intro;

    private LocalDateTime banend;

    @TableField("punish_num")
    private Integer punishNum;

    @TableField("avatar_url")
    private String avatarUrl;

    private String identity;

    private Integer score;

    @TableField("is_email_notification_blocked")
    private Boolean emailNotificationBlocked;

    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // TODO: 微信登录 — 需要先在数据库执行: ALTER TABLE user ADD COLUMN wechat_openid VARCHAR(64), ADD COLUMN wechat_unionid VARCHAR(64);
    // @TableField("wechat_openid")
    // private String wechatOpenid;
    //
    // @TableField("wechat_unionid")
    // private String wechatUnionid;
}