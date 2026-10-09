package software_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software_market.dto.request.chat.GetChatHistoryDTO;
import software_market.dto.request.chat.GetChatNoticeDTO;
import software_market.entity.ChatMsg;
import software_market.exception.BizException;
import software_market.mapper.ChatMessageMapper;
import software_market.service.ChatService;
import software_market.vo.chat.ChatHistoryVO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMsg> implements ChatService {
    private final ChatMessageMapper chatMessageMapper;
    @Override
    public void saveMessage(ChatMsg chatMsg) {
        chatMessageMapper.insert(chatMsg);
    }

    @Override
    public List<ChatHistoryVO> getChatHistory(GetChatHistoryDTO chatHistoryDTO) {
        Integer senderUserID = chatHistoryDTO.getSenderUserID();
        Integer targetUserID = chatHistoryDTO.getTargetUserID();
        if(senderUserID == null || targetUserID == null){
            throw new BizException("senderUserID or targetUserID is null");
        }
        readMessage(senderUserID,targetUserID);
        LambdaQueryWrapper<ChatMsg> wrapper = Wrappers.lambdaQuery();
        wrapper.and(w -> w
                .eq(ChatMsg::getSenderUserId,
                        chatHistoryDTO.getSenderUserID())
                .eq(ChatMsg::getTargetUserId,
                        chatHistoryDTO.getTargetUserID())
        ).or(w -> w
                .eq(ChatMsg::getSenderUserId,
                        chatHistoryDTO.getTargetUserID())
                .eq(ChatMsg::getTargetUserId,
                        chatHistoryDTO.getSenderUserID())
        );
        wrapper.orderByAsc(ChatMsg::getCreatedAt);
        List<ChatMsg> list = list(wrapper);
        return list.stream()
                .map(chatMsg -> ChatHistoryVO.builder()
                        .chatMsgId(chatMsg.getChatMsgId())
                        .senderUserId(chatMsg.getSenderUserId())
                        .targetUserId(chatMsg.getTargetUserId())
                        .content(chatMsg.getContent())
                        .createdAt(chatMsg.getCreatedAt())
                        .isRead(chatMsg.getIsRead())
                        .isAnonymous(chatMsg.getIsAnonymous())
                        .build())
                .toList();
    }

    @Override
    public Integer getChatNotice(GetChatNoticeDTO getChatNoticeDTO) {
        Integer userID = getChatNoticeDTO.getUserID();
        if(userID == null){
            throw new BizException("userID is null");
        }
        LambdaQueryWrapper<ChatMsg> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChatMsg::getTargetUserId, userID).eq(ChatMsg::getIsRead, 0);
        long count = count(wrapper);
        return (int) count;
    }

    /**
     * 修改已读状态
     */
    public void readMessage(Integer senderUserID, Integer targetUserID) {
        LambdaUpdateWrapper<ChatMsg> wrapper = Wrappers.lambdaUpdate();
        /**
         * target -> sender
         *
         * 对方发给我的消息
         * 改成已读
         */
        wrapper.eq(ChatMsg::getSenderUserId, targetUserID)
                .eq(ChatMsg::getTargetUserId, senderUserID)
                .eq(ChatMsg::getIsRead, 0)
                .set(ChatMsg::getIsRead, 1);
        update(wrapper);
    }
}
