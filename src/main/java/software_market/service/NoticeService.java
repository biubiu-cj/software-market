package software_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import software_market.dto.request.notice.NoticeGetDTO;
import software_market.entity.Notice;
import software_market.vo.notice.GetNoticeNumVO;
import software_market.vo.notice.NoticeGetVO;
import software_market.vo.notice.ReadNoticeVO;

public interface NoticeService extends IService<Notice> {
    NoticeGetVO getNotice(NoticeGetDTO noticeGetDTO);

    GetNoticeNumVO getNoticeNum();

    ReadNoticeVO readNotice(Long noticeId);

    void createNotice(Notice notice);
}
