package software_market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rating")
public class Rating {

    @TableId(value = "rating_id", type = IdType.AUTO)
    private Integer ratingId;

    private Integer userId;

    private String itemType;

    private Integer itemId;

    private Integer score;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}