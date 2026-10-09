package software_market.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import software_market.dto.request.feedback.SubmitFeedBackDTO;
import software_market.service.FeedBackService;
import software_market.utils.Result;
import software_market.vo.feedback.GetAllFeedBackVO;
import software_market.vo.feedback.SubmitFeedBackVO;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class FeedBackController {

    private final FeedBackService feedBackService;

    /**
     * 提交反馈
     * @param submitFeedBackDTO
     * @return
     */
    @PostMapping("/submitFeedback")
    public Result<SubmitFeedBackVO> submitFeedback(@RequestBody SubmitFeedBackDTO submitFeedBackDTO) {
        SubmitFeedBackVO submitFeedBackVO= feedBackService.submitFeedback(submitFeedBackDTO);
        return Result.success(submitFeedBackVO,"反馈成功");
    }

    /**
     * 获取所有反馈
     * @return
     */

    @GetMapping("/getAllFeedback")
    public Result<GetAllFeedBackVO> getAllFeedback() {
        GetAllFeedBackVO getAllFeedBackVO= feedBackService.getAllFeedback();
        return Result.success(getAllFeedBackVO,"获取反馈成功");
    }

    @PostMapping("/resolveFeedback")
    public Result<Void> resolveFeedback(@RequestBody java.util.Map<String, Integer> body) {
        Integer feedbackId = body.get("feedbackId");
        feedBackService.resolveFeedback(feedbackId);
        return Result.success(null, "反馈已处理");
    }

}
