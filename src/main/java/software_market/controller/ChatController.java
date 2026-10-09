package software_market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software_market.dto.request.chat.GetChatHistoryDTO;
import software_market.dto.request.chat.GetChatNoticeDTO;
import software_market.service.ChatService;
import software_market.utils.Result;
import software_market.vo.chat.ChatHistoryVO;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /**
     * 获取历史聊天记录
     * @param chatHistoryDTO
     * @return
     */
    @GetMapping("/getChatHistory")
    public Result<List<ChatHistoryVO>> getChatHistory(GetChatHistoryDTO chatHistoryDTO){
       List<ChatHistoryVO> lists=chatService.getChatHistory(chatHistoryDTO);
       return Result.success(lists,"获取历史聊天成功");
    }

    @GetMapping("/getChatNotice")
    public Result<Integer> getChatNotice(GetChatNoticeDTO getChatNoticeDTO){
        Integer count=chatService.getChatNotice(getChatNoticeDTO);
        return Result.success(count,"获取未聊天数成功");
    }
}
