package software_market.entity.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pcdeny")
public class Pcdeny {

    @TableId(value = "pcdeny_id", type = IdType.AUTO)
    private Integer pcdenyId;

    private Integer pctargetId;

    private Integer userId;
}
