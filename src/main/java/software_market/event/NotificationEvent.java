package software_market.event;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationEvent {

    /** 幂等键, 由生产端生成, 消费端据此去重 */
    private String eventId;
    private Integer receiver;
    private Integer sender;
    private String type;
    private String text;
    private Integer target;

    public NotificationEvent(Integer receiver, Integer sender, String type, String text, Integer target) {
        this.receiver = receiver;
        this.sender = sender;
        this.type = type;
        this.text = text;
        this.target = target;
    }
}
