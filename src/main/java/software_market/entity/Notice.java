package software_market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice {

    /**
     * 通知ID
     */
    @TableId(value = "noticeID", type = IdType.AUTO)
    private Integer noticeId;

    /**
     * 接收者
     */
    private Integer receiver;

    /**
     * 发送者
     */
    private Integer sender;

    /**
     * 通知类型
     * pcomment / ccomment / punish / feedback / sue / post
     */
    private String type;

    /**
     * 通知内容
     */
    private String ntext;

    /**
     * 时间
     */
    private LocalDateTime time;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 目标ID
     */
    private Integer target;
}