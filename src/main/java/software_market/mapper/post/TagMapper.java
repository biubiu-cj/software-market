package software_market.mapper.post;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import software_market.entity.post.Tag;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
