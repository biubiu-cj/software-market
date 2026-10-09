package software_market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cdkey")
public class Cdkey {

    /**
     * 主键ID
     */
    @TableId(value = "cdkeyID", type = IdType.AUTO)
    private Integer cdkeyID;

    /**
     * 激活码内容
     */
    private String content;

    /**
     * 状态：
     * 0-未使用
     * 1-已使用
     */
    private Integer status;

    /**
     * 被哪个用户使用
     */
    @TableField("used_by")
    private Integer usedBy;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 使用时间
     */
    @TableField("used_time")
    private LocalDateTime usedTime;
}