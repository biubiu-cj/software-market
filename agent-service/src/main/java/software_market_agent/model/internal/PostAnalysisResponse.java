package software_market_agent.model.internal;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostAnalysisResponse {

    private String status;
    private String conclusion = "";
    private List<AiCitation> citations = new ArrayList<>();
    private List<AiRelatedPost> relatedPosts = new ArrayList<>();
    private List<AiRelatedReply> relatedReplies = new ArrayList<>();
    private String notice = "以上内容来自站内历史讨论，仅供参考。";
    private String generatedAt;
    private String model;
    private Long latencyMs;
}
