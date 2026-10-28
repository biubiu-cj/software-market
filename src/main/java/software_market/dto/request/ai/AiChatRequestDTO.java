package software_market.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatRequestDTO {

    @NotBlank(message = "追问内容不能为空")
    private String message;

    private String conversationId;
}
