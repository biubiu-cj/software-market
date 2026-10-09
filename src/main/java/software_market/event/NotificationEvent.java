package software_market.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private Integer receiver;
    private Integer sender;
    private String type;
    private String text;
    private Integer target;
}
