package software_market.entity.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pclike")
public class Pclike {

    @TableId(value = "pclike_id", type = IdType.AUTO)
    private Integer pclikeId;

    private Integer pctargetId;

    private Integer userId;
}
