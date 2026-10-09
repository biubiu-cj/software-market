package software_market.vo.notice;

import lombok.Data;

@Data
public class GetNoticeNumVO {
    private Integer totalNum;
    private Integer unreadTotalNum;
    private Integer readTotalNum;
}
