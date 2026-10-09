package software_market.vo.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadPhotoVO {

    /**
     * 文件访问URL
     */
    private String fileURL;

    /**
     * 返回信息
     */
    private String message;
}
