package software_market.vo.user;

import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class UserStatisticsVO {

    private String name;
    private String intro;
    private Integer score;
    private String avatarUrl;
    private Duration durationTime;

    // 帖子统计
    private Integer totalPostCnt;
    private Integer totalPostLikeNum;
    private Integer thisYearPostCnt;
    private Integer thisYearLikeNum;
    private Integer maxLikeNum;
    private Integer maxBrowseNum;
    private Integer maxCommentNum;

    // 评论统计
    private Integer pCommentCnt;
    private Integer pCommentLikeNum;
    private Integer ccommentCnt;
    private Integer ccommentLikeNum;

    // 互动统计
    private Integer beSavedCount;
    private Integer beCommentedCount;
    private Integer repliedCount;
    private Integer savedCount;

    // 聊天统计
    private Integer chatCount;
    private List<UserBriefVO> relevantRespUsers;
    private UserBriefVO maxSayUser;
    private Integer maxSayUserCnt;
}
