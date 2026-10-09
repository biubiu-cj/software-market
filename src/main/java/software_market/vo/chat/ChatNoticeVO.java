package software_market.vo.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatNoticeVO {

    /**
     * 未读消息总数
     */
    private Integer noticeNum;

}