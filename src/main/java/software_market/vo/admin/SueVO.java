package software_market.vo.admin;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SueVO {
    private Integer sueId;
    private String targettype;
    private Integer targetId;
    private Integer userId;
    private String reason;
    private LocalDateTime time;
    private String status;
    private Boolean finish;
    private String reporterName;
    private String targetTitle;
}
