package software_market.dto.request.shop;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetProductDTO {
    /**
     * 用户ID（可选：查某个用户发布的商品）
     */
    private Integer userId;

    /**
     * 搜索关键词（商品标题/描述模糊查询）
     */
    private String searchInfo;

    /**
     * 查询类型（必填）
     * home：主页商品流
     * history：历史/用户相关
     */
    @NotBlank(message = "searchSort不能为空")
    private String searchSort;

    /**
     * 每页数量
     */
    private Integer limit = 10;

    /**
     * 偏移量（从第几条开始）
     */
    private Integer offset = 0;
}
