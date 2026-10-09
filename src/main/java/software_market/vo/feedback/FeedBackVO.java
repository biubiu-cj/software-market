package software_market.vo.feedback;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedBackVO {

    private Integer feedbackId;

    private String ftext;

    private String attachment;

    private String status;

    private LocalDateTime time;
}