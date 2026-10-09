package software_market.entity.post;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tag")
public class Tag {

    @TableId(value = "tag_id", type = IdType.AUTO)
    private Integer tagId;

    private String name;

    private String value;

    private String type;

    private Integer num;
}
