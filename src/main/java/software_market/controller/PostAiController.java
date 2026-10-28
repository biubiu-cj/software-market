package software_market.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software_market.dto.request.ai.AiChatRequestDTO;
import software_market.service.ai.PostAiService;
import software_market.utils.Result;
import software_market.vo.ai.PostAiAnalysisVO;

@RestController
@RequestMapping("/api/auth/post")
@RequiredArgsConstructor
public class PostAiController {

    private final PostAiService postAiService;

    @GetMapping("/{postId}/ai-analysis")
    public Result<PostAiAnalysisVO> aiAnalysis(@PathVariable Integer postId) {
        return Result.success(postAiService.analyze(postId), "获取 AI 分析成功");
    }

    /**
     * 第一版只预留接口，追问 AI 在后续阶段实现。
     */
    @PostMapping("/{postId}/ai-chat")
    public Result<Void> aiChat(@PathVariable Integer postId,
                               @RequestBody @Valid AiChatRequestDTO request) {
        return Result.fail(501, "追问 AI 功能将在后续版本开放");
    }
}
