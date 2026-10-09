package software_market.dto.request.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetPostNumDTO {

    /**
     * 分区
     * 例如：首页、课程交流、打分、优质贴
     */
    private String category;

    /**
     * 搜索关键词
     */
    private String searchinfo;

    /**
     * 查询模式
     * home / history / save / rating
     */
    @NotBlank(message = "searchsort不能为空")
    private String searchsort;
}