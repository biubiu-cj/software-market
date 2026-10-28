package software_market_agent.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextChunkerTest {

    @Test
    void respectsMaxSizeAndAddsOverlap() {
        String text = "这是一个用于测试分片的句子。".repeat(200);
        List<String> chunks = TextChunker.split(text, 600, 800, 80);

        assertTrue(chunks.size() > 1);
        chunks.forEach(chunk -> assertTrue(chunk.length() <= 800, "chunk too long: " + chunk.length()));
        assertFalse(chunks.get(0).isBlank());
        String firstTail = chunks.get(0).substring(Math.max(0, chunks.get(0).length() - 20));
        assertTrue(chunks.get(1).contains(firstTail.substring(firstTail.length() - 10)));
    }

    @Test
    void returnsSingleChunkForShortText() {
        List<String> chunks = TextChunker.split("短文本", 600, 800, 80);
        assertEquals(1, chunks.size());
        assertEquals("短文本", chunks.get(0));
    }
}
