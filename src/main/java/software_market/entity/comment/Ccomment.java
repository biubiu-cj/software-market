package software_market.entity.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ccomment")
public class Ccomment {

    @TableId(value = "ccomment_id", type = IdType.AUTO)
    private Integer ccommentId;

    private Integer userId;

    private Integer ctargetId;

    private Integer likeNum;

    private Integer denyNum;

    private String cctext;

    private LocalDateTime time;

    @TableField("usertargetName")
    private String usertargetName;
}
