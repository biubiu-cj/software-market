package software_market.dto.request.chat;

import lombok.Data;

@Data
public class GetChatHistoryDTO {

    /**
     * 发送者ID
     */
    private Integer senderUserID;

    /**
     * 接收者ID
     */
    private Integer targetUserID;

}