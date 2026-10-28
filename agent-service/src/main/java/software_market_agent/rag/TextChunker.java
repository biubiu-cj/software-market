package software_market_agent.rag;

import java.util.ArrayList;
import java.util.List;

/**
 * 中文友好的定长切分：优先在段落/句号处断开，保留重叠。
 */
public final class TextChunker {

    private static final String BREAK_CHARS = "\n。！？；!?;.";

    private TextChunker() {
    }

    public static List<String> split(String text, int targetSize, int maxSize, int overlap) {
        String normalized = text == null ? "" : text.strip();
        if (normalized.isEmpty()) {
            return List.of();
        }
        if (normalized.length() <= maxSize) {
            return List.of(normalized);
        }
        int target = Math.max(1, Math.min(targetSize, maxSize));
        int safeOverlap = Math.max(0, Math.min(overlap, maxSize / 2));

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int hardEnd = Math.min(start + maxSize, normalized.length());
            int end = hardEnd;
            if (hardEnd < normalized.length()) {
                int minBreak = start + Math.max(target - 100, maxSize / 2);
                int breakPos = findBreak(normalized, minBreak, hardEnd);
                if (breakPos > start) {
                    end = breakPos;
                }
            }
            String chunk = normalized.substring(start, end).strip();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end >= normalized.length()) {
                break;
            }
            int nextStart = end - safeOverlap;
            start = nextStart > start ? nextStart : end;
        }
        return chunks;
    }

    private static int findBreak(String text, int min, int max) {
        for (int i = Math.min(max, text.length()) - 1; i >= min; i--) {
            if (BREAK_CHARS.indexOf(text.charAt(i)) >= 0) {
                return i + 1;
            }
        }
        return -1;
    }
}
