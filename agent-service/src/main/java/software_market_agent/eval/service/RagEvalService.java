package software_market_agent.eval.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software_market_agent.agentscope.ChatModelProvider;
import software_market_agent.config.AgentProperties;
import software_market_agent.eval.loader.EvalCaseLoader;
import software_market_agent.eval.metrics.LlmJudgeService;
import software_market_agent.eval.metrics.RetrievalMetricCalculator;
import software_market_agent.eval.model.EvalBatchReport;
import software_market_agent.eval.model.EvalCase;
import software_market_agent.eval.model.EvalCaseResult;
import software_market_agent.eval.model.EvalConfig;
import software_market_agent.eval.model.EvalRunPage;
import software_market_agent.eval.model.EvalRunRequest;
import software_market_agent.eval.model.EvalRunSummary;
import software_market_agent.eval.model.GenerationScores;
import software_market_agent.eval.model.RetrievalScores;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * RAG 评测编排：加载用例 → 批量推理 → 检索指标 + LLM Judge → 聚合报告 → 落盘。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagEvalService {

    private static final int MAX_CONCURRENCY = 8;

    private final AgentProperties properties;
    private final EvalCaseLoader evalCaseLoader;
    private final EvalInferenceService evalInferenceService;
    private final RetrievalMetricCalculator retrievalMetricCalculator;
    private final LlmJudgeService llmJudgeService;
    private final EvalReportStore reportStore;
    private final ChatModelProvider chatModelProvider;

    public EvalBatchReport runBatch(EvalRunRequest request) throws IOException {
        EvalConfig config = request != null && request.getConfig() != null
                ? request.getConfig()
                : new EvalConfig();
        String datasetPath = request != null && StringUtils.hasText(request.getDatasetPath())
                ? request.getDatasetPath().strip()
                : properties.getEval().getDefaultDataset();

        List<EvalCase> cases = evalCaseLoader.load(datasetPath);
        if (config.getMaxCases() > 0 && cases.size() > config.getMaxCases()) {
            cases = new ArrayList<>(cases.subList(0, config.getMaxCases()));
        }

        String runId = generateRunId();
        boolean judgeEnabled = config.isEnableLlmJudge() && chatModelProvider.isAvailable();
        int topK = config.getTopK() > 0 ? config.getTopK() : properties.getRag().getTopK();

        log.info("RAG 评测开始: runId={}, dataset={}, cases={}, generation={}, judge={}, concurrency={}",
                runId, datasetPath, cases.size(), config.isEnableGeneration(), judgeEnabled, config.getConcurrency());

        EvalBatchReport report = new EvalBatchReport();
        report.setRunId(runId);
        report.setDatasetPath(datasetPath);
        report.setConfig(config);
        report.setTotalCases(cases.size());
        report.setJudgeEnabled(judgeEnabled);
        report.setCreatedAt(LocalDateTime.now().toString());

        List<EvalCaseResult> caseResults = executeCases(cases, config, topK, judgeEnabled);
        report.setCaseResults(caseResults);
        for (EvalCaseResult caseResult : caseResults) {
            if (!caseResult.isSuccess()) {
                report.getFailedCaseIds().add(caseResult.getCaseId());
            }
        }

        aggregate(report, config);
        reportStore.save(report);
        log.info("RAG 评测完成: runId={}, hit@{}={}, faithfulness={}, relevancy={}, review={}, failed={}",
                runId, topK,
                formatDouble(report.getRetrieval().getHitAtK()),
                formatDouble(report.getGeneration().getFaithfulness()),
                formatDouble(report.getGeneration().getRelevancy()),
                report.getReviewCaseIds().size(),
                report.getFailedCaseIds().size());
        return report;
    }

    public EvalBatchReport getReport(String runId, boolean includeCases) throws IOException {
        EvalBatchReport report = reportStore.load(runId)
                .orElseThrow(() -> new IllegalArgumentException("评测报告不存在: " + runId));
        if (!includeCases) {
            report.setCaseResults(List.of());
        }
        return report;
    }

    public EvalRunPage listRuns(int page, int size) throws IOException {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, size));
        List<EvalBatchReport> reports = reportStore.loadAll();
        int from = Math.min((safePage - 1) * safeSize, reports.size());
        int to = Math.min(from + safeSize, reports.size());
        List<EvalRunSummary> items = new ArrayList<>();
        for (EvalBatchReport report : reports.subList(from, to)) {
            items.add(toSummary(report));
        }
        return new EvalRunPage(safePage, safeSize, reports.size(), items);
    }

    private List<EvalCaseResult> executeCases(List<EvalCase> cases,
                                              EvalConfig config,
                                              int topK,
                                              boolean judgeEnabled) {
        int concurrency = Math.min(MAX_CONCURRENCY, Math.max(1, config.getConcurrency()));
        if (concurrency <= 1) {
            List<EvalCaseResult> results = new ArrayList<>();
            for (EvalCase evalCase : cases) {
                results.add(processCase(evalCase, config, topK, judgeEnabled));
            }
            return results;
        }

        ExecutorService executor = Executors.newFixedThreadPool(concurrency, runnable -> {
            Thread thread = new Thread(runnable, "rag-eval-worker");
            thread.setDaemon(true);
            return thread;
        });
        try {
            List<CompletableFuture<EvalCaseResult>> futures = new ArrayList<>();
            for (EvalCase evalCase : cases) {
                futures.add(CompletableFuture.supplyAsync(
                        () -> processCase(evalCase, config, topK, judgeEnabled), executor));
            }
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            List<EvalCaseResult> results = new ArrayList<>(futures.size());
            for (CompletableFuture<EvalCaseResult> future : futures) {
                results.add(future.join());
            }
            return results;
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private EvalCaseResult processCase(EvalCase evalCase,
                                       EvalConfig config,
                                       int topK,
                                       boolean judgeEnabled) {
        EvalCaseResult caseResult = evalInferenceService.infer(evalCase, config);
        try {
            if (caseResult.isSuccess() && evalCase.hasRetrievalGroundTruth()) {
                RetrievalScores scores = retrievalMetricCalculator.calculate(
                        caseResult.getHits(), evalCase, topK);
                caseResult.setRetrievalScores(scores);
            }
            if (judgeEnabled) {
                caseResult.setGenerationScores(
                        llmJudgeService.judge(evalCase, caseResult, config.isEnableGeneration()));
            }
        } catch (Exception e) {
            log.warn("评测后处理失败: caseId={}, error={}", evalCase.getId(), e.getMessage());
        }
        return caseResult;
    }

    private void aggregate(EvalBatchReport report, EvalConfig config) {
        int retrievalCount = 0;
        double hitSum = 0;
        double mrrSum = 0;
        double recallSum = 0;

        int faithfulnessCount = 0;
        double faithfulnessSum = 0;
        int relevancyCount = 0;
        double relevancySum = 0;
        int correctnessCount = 0;
        double correctnessSum = 0;
        int contextPrecisionCount = 0;
        double contextPrecisionSum = 0;
        int contextRecallCount = 0;
        double contextRecallSum = 0;
        int generationEvaluatedCount = 0;

        List<Long> latencies = new ArrayList<>();
        Set<String> reviewCaseIds = new LinkedHashSet<>();

        for (EvalCaseResult caseResult : report.getCaseResults()) {
            latencies.add(caseResult.getLatencyMs());
            if (!caseResult.isSuccess()) {
                reviewCaseIds.add(caseResult.getCaseId());
                continue;
            }
            RetrievalScores retrievalScores = caseResult.getRetrievalScores();
            if (retrievalScores != null) {
                retrievalCount++;
                hitSum += retrievalScores.isHitAtK() ? 1.0 : 0.0;
                mrrSum += retrievalScores.getMrr();
                recallSum += retrievalScores.getRecallAtK();
                if (!retrievalScores.isHitAtK()
                        || retrievalScores.getMrr() < config.getReviewMrrThreshold()
                        || retrievalScores.getRecallAtK() < config.getReviewRecallAtKThreshold()) {
                    reviewCaseIds.add(caseResult.getCaseId());
                }
            }

            GenerationScores generationScores = caseResult.getGenerationScores();
            if (generationScores == null) {
                continue;
            }
            boolean hasScore = false;
            if (generationScores.getFaithfulness() != null) {
                hasScore = true;
                faithfulnessCount++;
                faithfulnessSum += generationScores.getFaithfulness();
                if (generationScores.getFaithfulness() < config.getReviewScoreThreshold()) {
                    reviewCaseIds.add(caseResult.getCaseId());
                }
            }
            if (generationScores.getRelevancy() != null) {
                hasScore = true;
                relevancyCount++;
                relevancySum += generationScores.getRelevancy();
                if (generationScores.getRelevancy() < config.getReviewScoreThreshold()) {
                    reviewCaseIds.add(caseResult.getCaseId());
                }
            }
            if (generationScores.getAnswerCorrectness() != null) {
                hasScore = true;
                correctnessCount++;
                correctnessSum += generationScores.getAnswerCorrectness();
                if (generationScores.getAnswerCorrectness() < config.getReviewScoreThreshold()) {
                    reviewCaseIds.add(caseResult.getCaseId());
                }
            }
            if (generationScores.getContextPrecision() != null) {
                hasScore = true;
                contextPrecisionCount++;
                contextPrecisionSum += generationScores.getContextPrecision();
                if (generationScores.getContextPrecision() < config.getReviewScoreThreshold()) {
                    reviewCaseIds.add(caseResult.getCaseId());
                }
            }
            if (generationScores.getContextRecall() != null) {
                hasScore = true;
                contextRecallCount++;
                contextRecallSum += generationScores.getContextRecall();
                if (generationScores.getContextRecall() < config.getReviewScoreThreshold()) {
                    reviewCaseIds.add(caseResult.getCaseId());
                }
            }
            if (hasScore) {
                generationEvaluatedCount++;
            }
        }

        report.setEvaluatedCases(retrievalCount);
        report.setGenerationEvaluatedCases(generationEvaluatedCount);

        EvalBatchReport.RetrievalMetricsSummary retrieval = report.getRetrieval();
        retrieval.setPresent(retrievalCount > 0);
        if (retrievalCount > 0) {
            retrieval.setHitAtK(hitSum / retrievalCount);
            retrieval.setMrr(mrrSum / retrievalCount);
            retrieval.setRecallAtK(recallSum / retrievalCount);
        }
        retrieval.setAvgLatencyMs(average(latencies));
        retrieval.setP50LatencyMs(percentile(latencies, 50));
        retrieval.setP95LatencyMs(percentile(latencies, 95));

        EvalBatchReport.GenerationMetricsSummary generation = report.getGeneration();
        generation.setFaithfulnessCount(faithfulnessCount);
        generation.setRelevancyCount(relevancyCount);
        generation.setAnswerCorrectnessCount(correctnessCount);
        if (faithfulnessCount > 0) {
            generation.setFaithfulness(faithfulnessSum / faithfulnessCount);
        }
        if (relevancyCount > 0) {
            generation.setRelevancy(relevancySum / relevancyCount);
        }
        if (correctnessCount > 0) {
            generation.setAnswerCorrectness(correctnessSum / correctnessCount);
        }

        EvalBatchReport.ContextMetricsSummary context = report.getContext();
        context.setContextPrecisionCount(contextPrecisionCount);
        context.setContextRecallCount(contextRecallCount);
        if (contextPrecisionCount > 0) {
            context.setContextPrecision(contextPrecisionSum / contextPrecisionCount);
        }
        if (contextRecallCount > 0) {
            context.setContextRecall(contextRecallSum / contextRecallCount);
        }

        report.setReviewCaseIds(new ArrayList<>(reviewCaseIds));
    }

    private EvalRunSummary toSummary(EvalBatchReport report) {
        EvalRunSummary summary = new EvalRunSummary();
        summary.setRunId(report.getRunId());
        summary.setDatasetPath(report.getDatasetPath());
        summary.setTotalCases(report.getTotalCases());
        summary.setCreatedAt(report.getCreatedAt());
        if (report.getRetrieval() != null) {
            summary.setHitAtK(report.getRetrieval().getHitAtK());
            summary.setMrr(report.getRetrieval().getMrr());
            summary.setRecallAtK(report.getRetrieval().getRecallAtK());
            summary.setAvgLatencyMs(report.getRetrieval().getAvgLatencyMs());
        }
        if (report.getGeneration() != null) {
            summary.setFaithfulness(report.getGeneration().getFaithfulness());
            summary.setRelevancy(report.getGeneration().getRelevancy());
            summary.setAnswerCorrectness(report.getGeneration().getAnswerCorrectness());
        }
        if (report.getContext() != null) {
            summary.setContextPrecision(report.getContext().getContextPrecision());
            summary.setContextRecall(report.getContext().getContextRecall());
        }
        return summary;
    }

    private long average(List<Long> values) {
        if (values.isEmpty()) {
            return 0L;
        }
        return Math.round(values.stream().mapToLong(Long::longValue).average().orElse(0.0));
    }

    private long percentile(List<Long> values, int percentile) {
        if (values.isEmpty()) {
            return 0L;
        }
        List<Long> sorted = values.stream().sorted().toList();
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }

    private String generateRunId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return timestamp + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String formatDouble(Double value) {
        return value == null ? "n/a" : String.format(Locale.ROOT, "%.4f", value);
    }
}
