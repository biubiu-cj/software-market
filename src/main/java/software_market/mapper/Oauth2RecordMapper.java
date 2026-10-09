package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.Oauth2Record;

import java.util.List;

@Mapper
public interface Oauth2RecordMapper extends BaseMapper<Oauth2Record> {

    @Select("SELECT * FROM oauth2_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Oauth2Record> selectByUserId(@Param("userId") Integer userId);
}
