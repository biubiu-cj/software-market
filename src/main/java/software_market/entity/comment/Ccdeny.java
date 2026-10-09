package software_market.entity.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ccdeny")
public class Ccdeny {

    @TableId(value = "ccdeny_id", type = IdType.AUTO)
    private Integer ccdenyId;

    private Integer cctargetId;

    private Integer userId;
}
