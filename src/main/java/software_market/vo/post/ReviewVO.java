package software_market.vo.post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "带评分信息的帖子返回对象")
public class ReviewVO extends PostVO {

    @Schema(description = "平均评分")
    private Double rating;

    @Schema(description = "星级分布，例如 [1星,2星,3星,4星,5星]")
    private int[] stars;

    @Schema(description = "当前用户评分")
    private Integer userRating;
}