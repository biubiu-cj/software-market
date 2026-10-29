package software_market_agent.eval.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import software_market_agent.model.milvus.VectorSearchResult;

import java.util.ArrayList;
import java.util.List;

@Data
public class EvalCaseResult {

    private String caseId;
    private String question;
    private boolean success;
    private String error;
    private String status;
    private String answer;
    private List<Long> retrievedPostIds = new ArrayList<>();
    private List<String> retrievedTitles = new ArrayList<>();
    private int hitCount;
    private RetrievalScores retrievalScores;
    private GenerationScores generationScores;
    private long latencyMs;
    /** 仅用于本地 Judge，不写入报告，避免报告体积过大 */
    @JsonIgnore
    private List<String> contexts = new ArrayList<>();
    /** 原始召回结果，仅用于计算检索指标 */
    @JsonIgnore
    private List<VectorSearchResult> hits = new ArrayList<>();
}
