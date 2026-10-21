package software_market.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailEvent {

    /** 幂等键, 由生产端生成, 消费端据此去重 */
    private String eventId;
    private String to;
    private String code;

    public EmailEvent(String to, String code) {
        this.to = to;
        this.code = code;
    }
}
