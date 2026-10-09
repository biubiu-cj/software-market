package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.RefreshToken;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    @Select("SELECT * FROM refresh_token WHERE token = #{token} AND is_used = 0")
    RefreshToken selectByTokenAndNotUsed(@Param("token") String token);

    @Delete("DELETE FROM refresh_token WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
}
