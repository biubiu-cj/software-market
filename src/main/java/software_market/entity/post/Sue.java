package software_market.entity.post;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sue")
public class Sue {

    @TableId(value = "sue_id", type = IdType.AUTO)
    private Integer sueId;

    /** 举报目标类型: post / pcomment / ccomment */
    private String targettype;

    /** 被举报内容ID */
    private Integer targetId;

    /** 举报人用户ID */
    private Integer userId;

    /** 举报原因 */
    private String reason;

    /** 举报时间 */
    private LocalDateTime time;

    /** 处理状态: wait=待处理, ok=违规确认, nosin=无违规 */
    private String status;

    /** 是否已处理完毕 */
    private Boolean finish;
}
