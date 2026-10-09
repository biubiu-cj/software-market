package software_market.vo.feedback;

import lombok.Data;
import software_market.entity.FeedBack;

import java.util.List;

@Data
public class GetAllFeedBackVO {
    private List<FeedBackVO> feedbacks;
}
