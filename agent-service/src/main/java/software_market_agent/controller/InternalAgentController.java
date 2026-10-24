package software_market_agent.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software_market_agent.model.internal.IndexRequest;
import software_market_agent.model.internal.PostAnalysisRequest;
import software_market_agent.model.internal.PostAnalysisResponse;
import software_market_agent.rag.RagIndexService;
import software_market_agent.service.PostAnalysisService;

import java.util.Map;

@RestController
@RequestMapping("/internal/agent")
@RequiredArgsConstructor
public class InternalAgentController {

    private final PostAnalysisService postAnalysisService;

    private final RagIndexService ragIndexService;

    @PostMapping("/post-analysis")
    public PostAnalysisResponse postAnalysis(@RequestBody @Valid PostAnalysisRequest request) {
        return postAnalysisService.analyze(request);
    }

    /**
     * 手动重建/删除单个帖子的向量，便于联调和修复。
     */
    @PostMapping("/index")
    public Map<String, Object> index(@RequestBody IndexRequest request) {
        if (request.isDelete()) {
            ragIndexService.delete(request.getPostId());
        } else {
            ragIndexService.reindex(request.getPostId(), request.getVersion());
        }
        return Map.of("status", "OK", "postId", request.getPostId(), "delete", request.isDelete());
    }

    /**
     * 全量回填，首次联调或修复索引时使用。
     */
    @PostMapping("/index/backfill")
    public Map<String, Object> backfill() {
        int success = ragIndexService.backfillAll();
        return Map.of("status", "OK", "indexedPosts", success);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP");
    }
}
