package software_market.vo.notice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeVO {

    @JsonProperty("noticeID")
    private Integer noticeId;

    private Integer receiver;

    private Integer sender;

    private String senderName;

    private String senderAvatar;

    private String type;

    @JsonProperty("content")
    private String ntext;

    private LocalDateTime time;

    private Boolean read;

    private Integer target;

    @JsonProperty("postID")
    private Integer postId;
}
