package software_market.entity.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cclike")
public class Cclike {

    @TableId(value = "cclike_id", type = IdType.AUTO)
    private Integer cclikeId;

    private Integer cctargetId;

    private Integer userId;
}
