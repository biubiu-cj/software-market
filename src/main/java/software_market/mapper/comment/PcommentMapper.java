package software_market.mapper.comment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.comment.Pcomment;

import java.util.List;

@Mapper
public interface PcommentMapper extends BaseMapper<Pcomment> {
    @Select("SELECT * FROM pcomment WHERE ptarget_id = #{postId}")
    List<Pcomment> selectByPostId(@Param("postId") Integer postID);

    @Select("SELECT COUNT(*) FROM pcomment WHERE user_id = #{userId} AND ptarget_id = #{ptargetId} AND pctext = #{content}")
    int countDuplicate(@Param("userId") Integer userId, @Param("ptargetId") Integer ptargetId, @Param("content") String content);
}
