package software_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software_market.config.security.UserContext;
import software_market.dto.request.notice.NoticeGetDTO;
import software_market.entity.Notice;
import software_market.entity.User;
import software_market.exception.BizException;
import software_market.mapper.NoticeMapper;
import software_market.mapper.UserMapper;
import software_market.service.NoticeService;
import software_market.vo.notice.GetNoticeNumVO;
import software_market.vo.notice.NoticeGetVO;
import software_market.vo.notice.NoticeVO;
import software_market.vo.notice.ReadNoticeVO;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void createNotice(Notice notice) {
        this.save(notice);
    }

    @Override
    public NoticeGetVO getNotice(NoticeGetDTO noticeGetDTO) {
        Integer userId = UserContext.getUserId();
        if(userId == null){
            throw new RuntimeException("用户未登录");
        }
        Integer read = noticeGetDTO.getRead();
        Integer requireID = noticeGetDTO.getRequireID();
        Integer pageSize = noticeGetDTO.getPageSize();
        LambdaQueryWrapper<Notice> wrapper= Wrappers.<Notice>lambdaQuery().eq(Notice::getReceiver,userId);
        if(read != null){
            wrapper.eq(Notice::getIsRead,read==1);
        }
        if(requireID != null&&requireID!=0){
            wrapper.lt(Notice::getNoticeId,requireID);
        }
        wrapper.orderByDesc(Notice::getNoticeId);
        wrapper.last("limit "+pageSize);
        List<Notice> noticeList = this.list(wrapper);

        // 批量查询发送者用户信息
        Set<Integer> senderIds = noticeList.stream()
                .map(Notice::getSender)
                .collect(Collectors.toSet());
        final Map<Integer, User> userMap;
        if (!senderIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(senderIds);
            userMap = users.stream().collect(Collectors.toMap(User::getUserId, u -> u));
        } else {
            userMap = Collections.emptyMap();
        }

        List<NoticeVO> noticeVOList = noticeList.stream().map(notice -> {
            NoticeVO vo = new NoticeVO();
            User senderUser = userMap.get(notice.getSender());
            vo.setNoticeId(notice.getNoticeId());
            vo.setReceiver(notice.getReceiver());
            vo.setSender(notice.getSender());
            vo.setSenderName(senderUser != null ? senderUser.getName() : "用户已注销");
            vo.setSenderAvatar(senderUser != null ? senderUser.getAvatarUrl() : "");
            vo.setType(notice.getType());
            vo.setNtext(notice.getNtext());
            vo.setTime(notice.getTime());
            vo.setRead(notice.getIsRead());
            vo.setTarget(notice.getTarget());
            vo.setPostId("chat".equals(notice.getType()) ? 0 : notice.getTarget());
            return vo;
        }).toList();
        NoticeGetVO noticeGetVO = new NoticeGetVO();
        noticeGetVO.setNoticeList(noticeVOList);
        long totalNum = this.count(Wrappers.<Notice>lambdaQuery().eq(Notice::getReceiver, userId));
        long unreadTotalNum = this.count(
                Wrappers.<Notice>lambdaQuery()
                        .eq(Notice::getReceiver, userId)
                        .eq(Notice::getIsRead, false)
        );
        Integer lastID = 0;
        if (!noticeList.isEmpty()) {
            lastID = noticeList.get(noticeList.size() - 1).getNoticeId();
        }
        noticeGetVO.setLastID(lastID);
        noticeGetVO.setTotalNum((int) totalNum);
        noticeGetVO.setUnreadTotalNum((int) unreadTotalNum);
        return noticeGetVO;
    }

    @Override
    public GetNoticeNumVO getNoticeNum() {
        Integer userId = UserContext.getUserId();
        if(userId==null) {
            throw new RuntimeException("用户未登录");
        }
        long totalNum=this.count(Wrappers.<Notice>lambdaQuery().eq(Notice::getReceiver,userId));
        long unreadTotalNum=this.count(Wrappers.<Notice>lambdaQuery().eq(Notice::getReceiver,userId).eq(Notice::getIsRead,false));
        long readTotalNum=totalNum-unreadTotalNum;
        GetNoticeNumVO vo = new GetNoticeNumVO();
        vo.setTotalNum((int) totalNum);
        vo.setUnreadTotalNum((int) unreadTotalNum);
        vo.setReadTotalNum((int) readTotalNum);
        return vo;
    }

    @Override
    public ReadNoticeVO readNotice(Long noticeId) {
        if(noticeId==null){
            throw new BizException("参数错误");
        }
        Notice notice = this.getById(noticeId);
        if(notice==null){
            throw new BizException("该通知不存在");
        }
        if(notice.getIsRead()) {
            throw new BizException("该通知已读");
        }
        notice.setIsRead(true);
        this.updateById(notice);
        ReadNoticeVO vo = new ReadNoticeVO();
        vo.setStatus("success");
        return vo;
    }
}
