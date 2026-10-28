package software_market.vo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostAiAnalysisVO {

    private String status;

    private String conclusion = "";

    private List<AiCitationVO> citations = new ArrayList<>();

    private List<AiRelatedPostVO> relatedPosts = new ArrayList<>();

    private List<AiRelatedReplyVO> relatedReplies = new ArrayList<>();

    private String notice = "以上内容来自站内历史讨论，仅供参考。";

    private String generatedAt;

    private String model;

    private Long latencyMs;
}
