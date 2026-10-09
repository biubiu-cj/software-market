package software_market.entity.post;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pbrowse")
public class Pbrowse {

    @TableId(value = "pbrowse_id", type = IdType.AUTO)
    private Integer pbrowseId;

    private Integer ptargetId;

    private Integer userId;

    private LocalDateTime time;
}
