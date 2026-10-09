package software_market.vo.notice;

import lombok.Data;

import java.util.List;

@Data
public class NoticeGetVO {

    /**
     * 通知列表
     */
    private List<NoticeVO> noticeList;

    /**
     * 最后一条通知ID
     */
    private Integer lastID;

    /**
     * 通知总数
     */
    private Integer totalNum;

    /**
     * 未读通知总数
     */
    private Integer unreadTotalNum;
}