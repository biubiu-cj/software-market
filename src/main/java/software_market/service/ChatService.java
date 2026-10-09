package software_market.service;

import software_market.dto.request.chat.GetChatHistoryDTO;
import software_market.dto.request.chat.GetChatNoticeDTO;
import software_market.entity.ChatMsg;
import software_market.vo.chat.ChatHistoryVO;

import java.util.List;

public interface ChatService{
    void saveMessage(ChatMsg chatMsg);

    List<ChatHistoryVO> getChatHistory(GetChatHistoryDTO chatHistoryDTO);

    Integer getChatNotice(GetChatNoticeDTO getChatNoticeDTO);
}
