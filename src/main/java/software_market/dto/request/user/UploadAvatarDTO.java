package software_market.dto.request.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadAvatarDTO {
    /**
     * 头像文件
     */
    private MultipartFile file;
}
