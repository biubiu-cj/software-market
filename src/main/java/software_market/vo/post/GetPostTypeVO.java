package software_market.vo.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPostTypeVO {

    /**
     * 帖子类型
     * post / rating
     */
    private String postType;
}