package software_market.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateAvatarDTO {
    /**
     * 头像文件
     */
    private MultipartFile file;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;
}
