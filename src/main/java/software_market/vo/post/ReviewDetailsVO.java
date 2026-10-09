package software_market.vo.post;

import lombok.Data;

@Data
public class ReviewDetailsVO extends PostDetailsVO {

    /**
     * 平均评分
     */
    private Double rating;

    /**
     * 每个星级人数
     * 下标:
     * 0 -> 1星
     * 1 -> 2星
     * ...
     */
    private int[] stars;

    /**
     * 当前用户评分
     */
    private Integer userRating;
}