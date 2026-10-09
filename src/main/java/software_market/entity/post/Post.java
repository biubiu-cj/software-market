package software_market.entity.post;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {

    @TableId(value = "post_id", type = IdType.AUTO)
    private Integer postId;

    private Integer userId;

    private String category;

    private String title;

    private String ptext;

    private Integer commentNum;

    private Integer likeNum;

    private Integer browseNum;

    private LocalDateTime postTime;

    private Double heat;

    private String photos;

    private String tag;
}
