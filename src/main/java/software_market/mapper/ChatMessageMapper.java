package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import software_market.entity.ChatMsg;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMsg> {
}
