package software_market_agent.eval.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software_market_agent.eval.model.EvalBatchReport;
import software_market_agent.eval.model.EvalRunPage;
import software_market_agent.eval.model.EvalRunRequest;
import software_market_agent.eval.service.EvalExportService;
import software_market_agent.eval.service.RagEvalService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * RAG 评测接口。仅内网可访问，由 InternalTokenFilter 鉴权。
 */
@RestController
@RequestMapping("/internal/agent/eval")
@RequiredArgsConstructor
public class EvalController {

    private final RagEvalService ragEvalService;
    private final EvalExportService evalExportService;

    @PostMapping("/run")
    public EvalBatchReport run(@RequestBody(required = false) EvalRunRequest request) throws IOException {
        try {
            return ragEvalService.runBatch(request == null ? new EvalRunRequest() : request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/report/{runId}")
    public EvalBatchReport report(@PathVariable String runId,
                                  @RequestParam(defaultValue = "true") boolean includeCases) throws IOException {
        try {
            return ragEvalService.getReport(runId, includeCases);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/runs")
    public EvalRunPage runs(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int size) throws IOException {
        return ragEvalService.listRuns(page, size);
    }

    @GetMapping("/report/{runId}/export.csv")
    public void exportCsv(@PathVariable String runId, HttpServletResponse response) throws IOException {
        EvalBatchReport report;
        try {
            report = ragEvalService.getReport(runId, true);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        byte[] csv = evalExportService.toCsv(report).getBytes(StandardCharsets.UTF_8);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"eval-" + runId + ".csv\"");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        response.getOutputStream().write(csv);
    }
}
