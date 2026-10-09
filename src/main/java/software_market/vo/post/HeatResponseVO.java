package software_market.vo.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeatResponseVO {

    /**
     * 帖子ID
     */
    private Integer postID;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 热度值
     */
    private Double heat;
}