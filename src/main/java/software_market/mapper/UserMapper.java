package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import software_market.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
