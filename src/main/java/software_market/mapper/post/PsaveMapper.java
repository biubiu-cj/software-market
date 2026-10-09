package software_market.mapper.post;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.post.Psave;

import java.util.List;

@Mapper
public interface PsaveMapper extends BaseMapper<Psave> {

    @Select("SELECT * FROM psave"
            + " WHERE user_id = #{userId}"
            + " ORDER BY psave_id DESC")
    List<Psave> selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM psave"+" WHERE user_id = #{userId} AND ptarget_id = #{postId}")
    int countByUserAndPost(@Param("userId") Integer userId,
                           @Param("postId") Integer postId);

    @Delete("DELETE FROM psave WHERE user_id = #{userId} AND ptarget_id = #{postId}")
    void deleteByUserAndPost(@Param("userId") Integer userId,
                             @Param("postId") Integer postID);

    @Delete("DELETE FROM psave WHERE ptarget_id = #{postId}")
    void deleteByPostId(@Param("postId") Integer postId);

    @Delete("DELETE FROM psave WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
}
