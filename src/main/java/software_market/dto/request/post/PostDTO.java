package software_market.dto.request.post;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotBlank(message = "分区不能为空")
    private String category;

    /**
     * 图片 URL 字符串（后端存原串）
     */
    private String photos;

    /**
     * 使用 | 分隔
     * 例如：Java|SpringBoot|MySQL
     */
    private String tagList;
}
