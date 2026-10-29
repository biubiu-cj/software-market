package software_market_agent.eval.service;

import org.springframework.stereotype.Service;
import software_market_agent.eval.model.EvalBatchReport;
import software_market_agent.eval.model.EvalCaseResult;
import software_market_agent.eval.model.GenerationScores;
import software_market_agent.eval.model.RetrievalScores;

import java.util.List;
import java.util.StringJoiner;

@Service
public class EvalExportService {

    private static final String EOL = "\r\n";

    public String toCsv(EvalBatchReport report) {
        StringBuilder builder = new StringBuilder();
        builder.append("caseId,question,success,status,hitCount,retrievedPostIds,hitAtK,mrr,recallAtK,"
                        + "faithfulness,relevancy,answerCorrectness,contextPrecision,contextRecall,latencyMs,answer")
                .append(EOL);
        List<EvalCaseResult> results = report.getCaseResults() == null ? List.of() : report.getCaseResults();
        for (EvalCaseResult result : results) {
            RetrievalScores retrieval = result.getRetrievalScores();
            GenerationScores generation = result.getGenerationScores();
            StringJoiner row = new StringJoiner(",");
            row.add(cell(result.getCaseId()));
            row.add(cell(result.getQuestion()));
            row.add(String.valueOf(result.isSuccess()));
            row.add(cell(result.getStatus()));
            row.add(String.valueOf(result.getHitCount()));
            row.add(cell(String.join("|",
                    result.getRetrievedPostIds() == null
                            ? List.<String>of()
                            : result.getRetrievedPostIds().stream().map(String::valueOf).toList())));
            row.add(retrieval == null ? "" : String.valueOf(retrieval.isHitAtK()));
            row.add(retrieval == null ? "" : format(retrieval.getMrr()));
            row.add(retrieval == null ? "" : format(retrieval.getRecallAtK()));
            row.add(generation == null ? "" : format(generation.getFaithfulness()));
            row.add(generation == null ? "" : format(generation.getRelevancy()));
            row.add(generation == null ? "" : format(generation.getAnswerCorrectness()));
            row.add(generation == null ? "" : format(generation.getContextPrecision()));
            row.add(generation == null ? "" : format(generation.getContextRecall()));
            row.add(String.valueOf(result.getLatencyMs()));
            row.add(cell(result.getAnswer()));
            builder.append(row).append(EOL);
        }
        return builder.toString();
    }

    private String format(Double value) {
        return value == null ? "" : String.format(java.util.Locale.ROOT, "%.4f", value);
    }

    private String cell(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
