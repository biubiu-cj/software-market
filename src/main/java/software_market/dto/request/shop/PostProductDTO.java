package software_market.dto.request.shop;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PostProductDTO {

    @NotNull(message = "用户ID不能为空")
    private Integer userID;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", message = "价格不能小于0")
    private Integer price;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    @Size(max = 1000, message = "商品描述长度不能超过1000")
    private String content;

    /**
     * 商品图片URL列表
     */
    private List<String> photos;

    /**
     * 是否匿名发布
     */
    private Boolean isAnonymous = false;
}
