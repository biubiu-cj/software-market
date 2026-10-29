package software_market_agent.eval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software_market_agent.config.AgentProperties;
import software_market_agent.eval.model.EvalBatchReport;
import software_market_agent.eval.model.EvalCaseResult;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvalReportStoreTest {

    @Test
    void savesAndLoadsReports(@TempDir Path dir) throws Exception {
        AgentProperties properties = new AgentProperties();
        properties.getEval().setReportDir(dir.toString());
        EvalReportStore store = new EvalReportStore(properties, new ObjectMapper());

        EvalBatchReport report = new EvalBatchReport();
        report.setRunId("20261029-120000-abcd1234");
        report.setDatasetPath("classpath:eval/seed-eval.jsonl");
        report.setTotalCases(1);
        EvalCaseResult caseResult = new EvalCaseResult();
        caseResult.setCaseId("c1");
        caseResult.setQuestion("补考怎么申请");
        report.setCaseResults(List.of(caseResult));

        store.save(report);

        assertTrue(store.load(report.getRunId()).isPresent());
        assertEquals(1, store.load(report.getRunId()).orElseThrow().getCaseResults().size());
        assertEquals(1, store.loadAll().size());
        assertFalse(store.load("20261029-000000-ffffffff").isPresent());
    }

    @Test
    void rejectsPathTraversalRunId(@TempDir Path dir) {
        AgentProperties properties = new AgentProperties();
        properties.getEval().setReportDir(dir.toString());
        EvalReportStore store = new EvalReportStore(properties, new ObjectMapper());

        assertThrows(IllegalArgumentException.class, () -> store.load("../application-local"));
    }
}
