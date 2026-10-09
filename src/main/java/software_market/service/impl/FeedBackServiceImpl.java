package software_market.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import software_market.dto.request.feedback.SubmitFeedBackDTO;
import software_market.entity.FeedBack;
import software_market.exception.BizException;
import software_market.mapper.FeedBackMapper;
import software_market.service.FeedBackService;
import software_market.vo.feedback.FeedBackVO;
import software_market.vo.feedback.GetAllFeedBackVO;
import software_market.vo.feedback.SubmitFeedBackVO;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedBackServiceImpl extends ServiceImpl<FeedBackMapper,FeedBack> implements FeedBackService {
    @Override
    public SubmitFeedBackVO submitFeedback(SubmitFeedBackDTO submitFeedBackDTO) {
        String ftext = submitFeedBackDTO.getFtext();
        String attachment = submitFeedBackDTO.getAttachment();
        if (ftext == null || ftext.trim().isEmpty()) {
            throw new BizException("反馈内容不能为空");
        }
        FeedBack feedBack = new FeedBack();
        feedBack.setFtext(ftext);
        feedBack.setAttachment(attachment);
        feedBack.setTime(LocalDateTime.now());
        feedBack.setStatus("wait");
        boolean success = save(feedBack);
        if(!success){
            throw new BizException("反馈提交失败");
        }
        SubmitFeedBackVO vo = new SubmitFeedBackVO();
        vo.setFeedbackId(feedBack.getFeedbackId());
        vo.setFtext(feedBack.getFtext());
        vo.setAttachment(feedBack.getAttachment());
        return vo;
    }

    @Override
    public GetAllFeedBackVO getAllFeedback() {
        List<FeedBack> feedBacks = this.list();
        List<FeedBackVO> voList = feedBacks.stream().map(feedBack -> {

            FeedBackVO vo = new FeedBackVO();

            vo.setFeedbackId(feedBack.getFeedbackId());

            vo.setFtext(feedBack.getFtext());

            vo.setAttachment(feedBack.getAttachment());

            vo.setStatus(feedBack.getStatus());

            vo.setTime(feedBack.getTime());
            return vo;
        }).toList();
        GetAllFeedBackVO vo = new GetAllFeedBackVO();
        vo.setFeedbacks(voList);
        return vo;
    }

    @Override
    public void resolveFeedback(Integer feedbackId) {
        FeedBack feedBack = this.getById(feedbackId);
        if (feedBack == null) {
            throw new BizException("反馈不存在");
        }
        feedBack.setStatus("ok");
        this.updateById(feedBack);
    }
}
