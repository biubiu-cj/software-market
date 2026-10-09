package software_market.mapper.post;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.post.Plike;

@Mapper
public interface PlikeMapper extends BaseMapper<Plike> {

    @Select("SELECT COUNT(*) FROM plike WHERE user_id = #{userId} AND ptarget_id = #{postId}")
    long countByUserAndPost(@Param("userId") Integer userId,
                            @Param("postId") Integer postId);

    @Delete("DELETE FROM plike WHERE user_id = #{userId} AND ptarget_id = #{postId}")
    void deleteByUserAndPost(@Param("userId") Integer userId,
                             @Param("postId") Integer postID);

    @Delete("DELETE FROM plike WHERE ptarget_id = #{postId}")
    void deleteByPostId(@Param("postId") Integer postId);

    @Delete("DELETE FROM plike WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
}
