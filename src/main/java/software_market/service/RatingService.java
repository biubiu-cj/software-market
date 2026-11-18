package software_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import software_market.dto.request.rating.RatingDTO;
import software_market.dto.request.rating.SubmitRatingDTO;
import software_market.entity.Rating;
import software_market.vo.rating.AverageRatingVO;
import software_market.vo.rating.GetStarsVO;
import software_market.vo.rating.RatingSummaryVO;

import java.util.Collection;
import java.util.Map;

public interface RatingService extends IService<Rating> {
    void submitRating(SubmitRatingDTO submitRatingDTO);

    int userPostRating(RatingDTO ratingDTO);

    AverageRatingVO getAverageRating(RatingDTO ratingDTO);

    GetStarsVO getStars(RatingDTO ratingDTO);

    /**
     * 批量获取一页帖子所需的评分信息，避免逐帖查询造成 N+1。
     *
     * @param postIds 帖子 ID 集合
     * @param userId  当前用户 ID，未登录时传 null
     * @return postId -> 评分汇总；入参为空时返回空 Map
     */
    Map<Integer, RatingSummaryVO> getRatingSummaries(Collection<Integer> postIds, Integer userId);
}
