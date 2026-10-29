package software_market_agent.eval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software_market_agent.config.AgentProperties;
import software_market_agent.eval.model.EvalBatchReport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 评测报告以 JSON 文件落盘，避免给只读数据库新增表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvalReportStore {

    private static final Pattern RUN_ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,80}");

    private final AgentProperties properties;
    private final ObjectMapper objectMapper;

    public void save(EvalBatchReport report) throws IOException {
        Path dir = reportDir();
        Files.createDirectories(dir);
        Path file = dir.resolve(report.getRunId() + ".json");
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }

    public Optional<EvalBatchReport> load(String runId) throws IOException {
        if (!RUN_ID_PATTERN.matcher(runId).matches()) {
            throw new IllegalArgumentException("非法的 runId: " + runId);
        }
        Path file = reportDir().resolve(runId + ".json");
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        return Optional.of(objectMapper.readValue(
                Files.readString(file, StandardCharsets.UTF_8), EvalBatchReport.class));
    }

    public List<EvalBatchReport> loadAll() throws IOException {
        Path dir = reportDir();
        if (!Files.exists(dir)) {
            return List.of();
        }
        List<Path> files;
        try (Stream<Path> stream = Files.list(dir)) {
            files = stream
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing((Path path) -> path.getFileName().toString()).reversed())
                    .toList();
        }
        List<EvalBatchReport> reports = new ArrayList<>();
        for (Path file : files) {
            try {
                reports.add(objectMapper.readValue(
                        Files.readString(file, StandardCharsets.UTF_8), EvalBatchReport.class));
            } catch (Exception e) {
                log.warn("跳过无法解析的评测报告: {}, error={}", file, e.getMessage());
            }
        }
        return reports;
    }

    private Path reportDir() {
        return Path.of(properties.getEval().getReportDir());
    }
}
