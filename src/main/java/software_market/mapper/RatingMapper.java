package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import software_market.entity.Rating;

@Mapper
public interface RatingMapper extends BaseMapper<Rating> {

    @Delete("DELETE FROM rating WHERE item_type = #{itemType} AND item_id = #{itemId}")
    void deleteByItem(@Param("itemType") String itemType, @Param("itemId") Integer itemId);

    @Delete("DELETE FROM rating WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
}
