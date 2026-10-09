package software_market.mapper.post;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import software_market.entity.post.Post;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Update("update post set browse_num = browse_num + 1 where post_id = #{postId}")
    void updateBrowseNum(Integer postId);

    @Update("""
        update post
        set like_num = greatest(like_num + #{delta}, 0)
        where post_id = #{postId}
        """)
    void updateLikeNum(@Param("postId") Integer postId,
                       @Param("delta") Integer delta);

    @Select("SELECT post_id FROM post WHERE user_id = #{userId}")
    List<Integer> selectPostIdsByUserId(@Param("userId") Integer userId);

    @Update("update post set heat = heat + #{delta} where post_id = #{postId}")
    void updateHeat(@Param("postId") Integer postId, @Param("delta") Double delta);

    @Delete("DELETE FROM post WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
}
