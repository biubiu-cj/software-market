package software_market.mapper.comment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.comment.Ccomment;

import java.util.List;

@Mapper
public interface CcommentMapper extends BaseMapper<Ccomment> {
    @Select("SELECT * FROM ccomment WHERE ctarget_id = #{pcommentId}")
    List<Ccomment> selectPcId(@Param("pcommentId") Integer pcommentId);

    @Select("SELECT COUNT(*) FROM ccomment WHERE user_id = #{userId} AND ctarget_id = #{ctargetId} AND cctext = #{content}")
    int countDuplicate(@Param("userId") Integer userId, @Param("ctargetId") Integer ctargetId, @Param("content") String content);
}
