package software_market.vo.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPostNumVO {

    /**
     * 帖子数量
     */
    private Long postCount;
}