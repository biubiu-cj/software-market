package software_market.vo.rating;

import lombok.Data;

/**
 * 一页帖子所需的评分汇总：平均分、星级分布、当前用户评分。
 */
@Data
public class RatingSummaryVO {

    private Double averageRating;

    private GetStarsVO stars;

    private Integer userRating;

    /**
     * 没有评分时的默认值，避免调用方对 null 做额外判断。
     */
    public static RatingSummaryVO empty() {
        RatingSummaryVO summary = new RatingSummaryVO();
        summary.setAverageRating(0.0);

        GetStarsVO stars = new GetStarsVO();
        stars.setStar1(0);
        stars.setStar2(0);
        stars.setStar3(0);
        stars.setStar4(0);
        stars.setStar5(0);
        summary.setStars(stars);

        summary.setUserRating(0);
        return summary;
    }
}