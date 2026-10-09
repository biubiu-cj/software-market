package software_market.entity.post;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("psave")
public class Psave {

    @TableId(value = "psave_id", type = IdType.AUTO)
    private Integer psaveId;

    private Integer ptargetId;

    private Integer userId;
}
