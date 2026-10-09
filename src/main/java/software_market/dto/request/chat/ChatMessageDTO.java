package software_market.dto.request.chat;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class ChatMessageDTO {

    /**
     * 接收者ID
     */
    @JSONField(name = "targetUserID")
    private Integer targetUserId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous;

}