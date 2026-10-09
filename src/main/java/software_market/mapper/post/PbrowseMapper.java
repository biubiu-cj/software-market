package software_market.mapper.post;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.post.Pbrowse;

import java.util.List;

@Mapper
public interface PbrowseMapper extends BaseMapper<Pbrowse> {

    @Select("select * from pbrowse where user_id = #{userId} "
            + "order by time desc "
            + "limit #{limit} offset #{offset}")
    List<Pbrowse> selectByUserId(Integer userId, Integer limit, Integer offset);

    @Select("select * from pbrowse where user_id = #{userId} "
            + "order by time desc ")
    List<Pbrowse> selectTotalByUserId(Integer userId);

    @Select("select count(*) from pbrowse where user_id = #{userId} and ptarget_id = #{postId}")
    long countByUserAndPost(Integer userId, Integer postId);

    @Insert("insert into pbrowse (ptarget_id, user_id, time) values (#{ptargetId}, #{userId}, #{time}) "
            + "on duplicate key update time = values(time)")
    int upsert(Pbrowse pbrowse);

    @Delete("DELETE FROM pbrowse WHERE ptarget_id = #{postId}")
    void deleteByPostId(@Param("postId") Integer postId);

    @Delete("DELETE FROM pbrowse WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);
}
