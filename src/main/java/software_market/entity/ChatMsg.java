package software_market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_msgs")
public class ChatMsg {

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Integer chatMsgId;

    /**
     * 发送者ID
     */
    private Integer senderUserId;

    /**
     * 接收者ID
     */
    private Integer targetUserId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否读
     */
    private Integer isRead;

    /**
     * 是否匿名
     */
    private Integer isAnonymous;

}