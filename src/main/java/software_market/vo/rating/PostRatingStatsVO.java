package software_market.vo.rating;

import lombok.Data;

/**
 * 批量评分统计的数据库投影，仅用于 Mapper 查询结果映射。
 */
@Data
public class PostRatingStatsVO {

    private Integer postId;

    private Double averageScore;

    private Long star1;

    private Long star2;

    private Long star3;

    private Long star4;

    private Long star5;
}