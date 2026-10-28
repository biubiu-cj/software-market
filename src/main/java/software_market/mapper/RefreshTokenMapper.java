package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import software_market.entity.RefreshToken;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    @Select("SELECT * FROM refresh_token WHERE token = #{token} LIMIT 1")
    RefreshToken selectByToken(@Param("token") String token);

    /**
     * 原子地把未使用的 Refresh Token 标记为已使用。
     * 返回 1 表示本次调用成功消费；返回 0 表示已被并发请求消费（或不存在）。
     */
    @Update("UPDATE refresh_token SET is_used = 1 WHERE token = #{token} AND is_used = 0")
    int markUsedIfUnused(@Param("token") String token);

    @Delete("DELETE FROM refresh_token WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
}
