package software_market.entity.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pcomment")
public class Pcomment {

    @TableId(value = "pcomment_id", type = IdType.AUTO)
    private Integer pcommentId;

    private Integer userId;

    private Integer ptargetId;

    private Integer likeNum;

    private Integer denyNum;

    private String pctext;

    private LocalDateTime time;
}
