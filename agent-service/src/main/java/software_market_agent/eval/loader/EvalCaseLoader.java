package software_market_agent.eval.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software_market_agent.eval.model.EvalCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 JSONL 加载评测用例，支持 classpath: 前缀与本地文件。
 */
@Component
@RequiredArgsConstructor
public class EvalCaseLoader {

    private static final String CLASSPATH_PREFIX = "classpath:";

    private final ObjectMapper objectMapper;

    public List<EvalCase> load(String datasetPath) throws IOException {
        try (BufferedReader reader = openReader(datasetPath)) {
            List<EvalCase> cases = new ArrayList<>();
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                EvalCase evalCase;
                try {
                    evalCase = objectMapper.readValue(trimmed, EvalCase.class);
                } catch (Exception e) {
                    throw new IllegalArgumentException("第 " + lineNo + " 行解析失败: " + e.getMessage(), e);
                }
                if (evalCase == null || !StringUtils.hasText(evalCase.getQuestion())) {
                    throw new IllegalArgumentException("第 " + lineNo + " 行缺少 question 字段");
                }
                if (!StringUtils.hasText(evalCase.getId())) {
                    evalCase.setId("case-" + lineNo);
                }
                cases.add(evalCase);
            }
            if (cases.isEmpty()) {
                throw new IllegalArgumentException("测试集为空: " + datasetPath);
            }
            return cases;
        }
    }

    private BufferedReader openReader(String datasetPath) throws IOException {
        if (datasetPath.startsWith(CLASSPATH_PREFIX)) {
            String resourcePath = datasetPath.substring(CLASSPATH_PREFIX.length());
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                throw new IllegalArgumentException("classpath 资源不存在: " + resourcePath);
            }
            return new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        }
        Path path = Path.of(datasetPath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("文件不存在: " + datasetPath);
        }
        return Files.newBufferedReader(path, StandardCharsets.UTF_8);
    }
}
