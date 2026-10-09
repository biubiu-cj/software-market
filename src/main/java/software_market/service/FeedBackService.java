package software_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import software_market.dto.request.feedback.SubmitFeedBackDTO;
import software_market.entity.FeedBack;
import software_market.vo.feedback.GetAllFeedBackVO;
import software_market.vo.feedback.SubmitFeedBackVO;

public interface FeedBackService extends IService<FeedBack> {
    SubmitFeedBackVO submitFeedback(SubmitFeedBackDTO submitFeedBackDTO);

    GetAllFeedBackVO getAllFeedback();

    void resolveFeedback(Integer feedbackId);
}
