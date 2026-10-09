package software_market.dto.request.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrowsePostDTO {

    /**
     * 分区
     */
    private String category;

    /**
     * 搜索内容（标题/正文）
     */
    private String searchInfo;

    /**
     * 标签
     */
    private String tag;

    /**
     * home/history/save/rating
     */
    @NotBlank(message = "搜索类型不能为空")
    private String searchSort;

    /**
     * 分页大小
     */
    private Integer limit = 10;

    /**
     * 偏移量
     */
    private Integer offset = 0;

}