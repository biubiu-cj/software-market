package software_market.vo.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatHistoryVO {

    @JsonProperty("chatMsgID")
    private Integer chatMsgId;

    @JsonProperty("senderUserID")
    private Integer senderUserId;

    @JsonProperty("targetUserID")
    private Integer targetUserId;

    private String content;

    private LocalDateTime createdAt;

    @JsonProperty("isRead")
    private Integer isRead;

    @JsonProperty("isAnonymous")
    private Integer isAnonymous;

}