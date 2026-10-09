package software_market.dto.request.shop;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

@Data
public class UploadPhotoDTO {

    /**
     * 上传的文件
     */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
}