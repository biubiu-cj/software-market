package software_market_agent.eval.metrics;

import org.junit.jupiter.api.Test;
import software_market_agent.eval.model.EvalCase;
import software_market_agent.eval.model.RetrievalScores;
import software_market_agent.model.milvus.VectorSearchResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetrievalMetricCalculatorTest {

    private final RetrievalMetricCalculator calculator = new RetrievalMetricCalculator();

    @Test
    void computesHitMrrAndRecallForPostGroundTruth() {
        EvalCase evalCase = new EvalCase();
        evalCase.setQuestion("补考怎么申请");
        evalCase.setGroundTruthPostIds(List.of(1L, 5L));

        RetrievalScores scores = calculator.calculate(List.of(
                hit(2, 0, "无关帖子"),
                hit(1, 0, "相关帖子一"),
                hit(5, 0, "相关帖子二")
        ), evalCase, 8);

        assertTrue(scores.isHitAtK());
        assertEquals(1.0 / 2, scores.getMrr(), 1e-9);
        assertEquals(1.0, scores.getRecallAtK(), 1e-9);
    }

    @Test
    void fallsBackToTitleGroundTruth() {
        EvalCase evalCase = new EvalCase();
        evalCase.setQuestion("校园网连不上");
        evalCase.setGroundTruthPostTitles(List.of("校园网连不上怎么办"));

        RetrievalScores scores = calculator.calculate(List.of(
                hit(9, 0, "无关", "宿舍热水"),
                hit(10, 0, "相关", "校园网连不上怎么办")
        ), evalCase, 8);

        assertTrue(scores.isHitAtK());
        assertEquals(1.0 / 2, scores.getMrr(), 1e-9);
    }

    @Test
    void computesReplyLevelRecall() {
        EvalCase evalCase = new EvalCase();
        evalCase.setQuestion("校园卡补办");
        evalCase.setGroundTruthReplyKeys(List.of("1:10", "1:11"));

        RetrievalScores scores = calculator.calculate(List.of(
                hit(1, 10, "第一条相关回复"),
                hit(2, 20, "无关回复")
        ), evalCase, 8);

        assertTrue(scores.isHitAtK());
        assertEquals(1.0, scores.getMrr(), 1e-9);
        assertEquals(0.5, scores.getRecallAtK(), 1e-9);
    }

    @Test
    void returnsNullWhenNoGroundTruthOrHits() {
        EvalCase evalCase = new EvalCase();
        evalCase.setQuestion("没有真值");
        assertNull(calculator.calculate(List.of(hit(1, 0, "x")), evalCase, 8));

        evalCase.setGroundTruthPostIds(List.of(1L));
        assertNull(calculator.calculate(List.of(), evalCase, 8));
    }

    @Test
    void topKLimitsEvaluationScope() {
        EvalCase evalCase = new EvalCase();
        evalCase.setQuestion("补考");
        evalCase.setGroundTruthPostIds(List.of(3L));

        RetrievalScores scores = calculator.calculate(List.of(
                hit(1, 0, "a"),
                hit(2, 0, "b"),
                hit(3, 0, "命中但超出 topK")
        ), evalCase, 2);

        assertFalse(scores.isHitAtK());
        assertEquals(0.0, scores.getMrr(), 1e-9);
    }

    private VectorSearchResult hit(long postId, long commentId, String content) {
        return hit(postId, commentId, content, "帖子 " + postId);
    }

    private VectorSearchResult hit(long postId, long commentId, String content, String title) {
        VectorSearchResult hit = new VectorSearchResult();
        hit.setId("post:" + postId + ":comment:" + commentId);
        hit.setPostId(postId);
        hit.setCommentId(commentId);
        hit.setTitle(title);
        hit.setContent(content);
        hit.setScore(0.8);
        return hit;
    }
}
