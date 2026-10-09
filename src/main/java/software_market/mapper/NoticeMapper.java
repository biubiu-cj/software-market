package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import software_market.entity.Notice;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    @Delete("DELETE FROM notice WHERE target = #{target}")
    void deleteByTarget(@Param("target") Integer target);

    @Delete("DELETE FROM notice WHERE sender = #{userId} OR receiver = #{userId}")
    void deleteByUser(@Param("userId") Integer userId);
}
