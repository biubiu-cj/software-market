package software_market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(value = "product_id", type = IdType.AUTO)
    private Integer productId;

    private Integer userId;

    private String productName;

    private String description;

    private Integer price;

    private String photos;           // 逗号分隔的图片URL

    private LocalDateTime postTime;

    private Boolean isSold;

    private Boolean isAnonymous;

    private Boolean isDelete;        // 软删除标记
}