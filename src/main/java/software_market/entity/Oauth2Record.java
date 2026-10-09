package software_market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oauth2_record")
public class Oauth2Record {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer userId;
    private String appId;
    private String scope;
    private LocalDateTime createdAt;
}
