package software_market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class FeedBack {
    /**
     * 反馈ID
     */
    @TableId(value = "feedbackID", type = IdType.AUTO)
    private Integer feedbackId;

    /**
     * 反馈内容
     */
    private String ftext;

    /**
     * 附件
     */
    private String attachment;

    /**
     * 时间
     */
    private LocalDateTime time;

    /**
     * 状态
     * ok / wait
     */
    private String status;
}
