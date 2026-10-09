package software_market.dto.request.notice;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoticeGetDTO {

    /**
     * 每页数量
     */
    @NotBlank(message = "每页数量不能为空")
    private Integer pageSize;

    /**
     * 0 表示首页
     * 否则加载更早数据
     */
    @NotBlank(message = "页码不能为空")
    private Integer requireID;

    /**
     * 是否已读
     * 0 未读
     * 1 已读
     */
    @NotBlank(message = "是否已读不能为空")
    private Integer read;

}
