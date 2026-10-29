package software_market_agent.eval.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software_market_agent.eval.model.EvalCase;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvalCaseLoaderTest {

    private final EvalCaseLoader loader = new EvalCaseLoader(new ObjectMapper());

    @Test
    void loadsJsonlSkippingBlankAndCommentLines(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("cases.jsonl");
        Files.writeString(file, """
                # 这一行是注释

                {"id":"c1","question":"校园卡丢了怎么补办","groundTruthPostIds":[1,2]}
                {"question":"补考怎么申请","groundTruthAnswer":"教务系统提交"}
                """, StandardCharsets.UTF_8);

        List<EvalCase> cases = loader.load(file.toString());

        assertEquals(2, cases.size());
        assertEquals("c1", cases.get(0).getId());
        assertEquals(List.of(1L, 2L), cases.get(0).getGroundTruthPostIds());
        assertEquals("case-4", cases.get(1).getId());
        assertEquals("教务系统提交", cases.get(1).getGroundTruthAnswer());
    }

    @Test
    void rejectsCaseWithoutQuestion(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("bad.jsonl");
        Files.writeString(file, "{\"id\":\"c1\"}\n", StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class, () -> loader.load(file.toString()));
    }

    @Test
    void rejectsUnknownDatasetPath() {
        assertThrows(IllegalArgumentException.class, () -> loader.load("classpath:eval/not-exists.jsonl"));
    }
}
