package software_market_agent.eval.model;

import lombok.Data;

import java.util.List;

/**
 * RAG 评测单条用例（JSONL 一行）。
 * 检索真值支持帖子 ID、帖子标题、回复键（postId:commentId）三种标注方式。
 */
@Data
public class EvalCase {

    private String id;
    private String question;
    private String groundTruthAnswer;
    private List<Long> groundTruthPostIds;
    private List<String> groundTruthPostTitles;
    private List<String> groundTruthReplyKeys;
    /** 检索时要排除的帖子 ID（评测查询本身来自某个帖子时使用） */
    private Long excludePostId;
    private String category;

    public boolean hasRetrievalGroundTruth() {
        return notEmpty(groundTruthPostIds) || notEmpty(groundTruthPostTitles) || notEmpty(groundTruthReplyKeys);
    }

    public boolean hasAnswerGroundTruth() {
        return groundTruthAnswer != null && !groundTruthAnswer.isBlank();
    }

    private boolean notEmpty(List<?> values) {
        return values != null && !values.isEmpty();
    }
}
