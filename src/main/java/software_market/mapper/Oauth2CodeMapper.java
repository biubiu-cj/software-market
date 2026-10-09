package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.Oauth2Code;

@Mapper
public interface Oauth2CodeMapper extends BaseMapper<Oauth2Code> {

    @Select("SELECT * FROM oauth2_code WHERE code = #{code} AND is_used = 0")
    Oauth2Code selectByCodeAndNotUsed(@Param("code") String code);
}
