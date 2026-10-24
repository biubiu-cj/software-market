package software_market_agent.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software_market_agent.config.AgentProperties;
import software_market_agent.model.milvus.VectorDocument;
import software_market_agent.model.post.CommentSnapshot;
import software_market_agent.model.post.PostSnapshot;
import software_market_agent.model.post.ReplySnapshot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子 + 主评论 + 评论回复 -> 知识文档。
 * 一条主评论一个知识单元；没有可用评论时使用帖子正文单元。
 */
@Service
@RequiredArgsConstructor
public class RagDocumentService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String SOURCE_POST_BODY = "POST_BODY";
    private static final String SOURCE_POST_COMMENT = "POST_COMMENT";

    private final AgentProperties properties;

    public List<VectorDocument> build(PostSnapshot post, long docVersion) {
        List<VectorDocument> documents = new ArrayList<>();
        int minCommentLength = Math.max(1, properties.getRag().getMinCommentLength());

        List<CommentSnapshot> usableComments = post.getComments().stream()
                .filter(comment -> textLength(comment.getContent()) >= minCommentLength)
                .toList();

        if (usableComments.isEmpty()) {
            documents.addAll(buildBodyDocuments(post, docVersion));
            return documents;
        }

        for (CommentSnapshot comment : usableComments) {
            documents.addAll(buildCommentDocuments(post, comment, docVersion, minCommentLength));
        }
        return documents;
    }

    private List<VectorDocument> buildBodyDocuments(PostSnapshot post, long docVersion) {
        String header = header(post);
        String body = "【正文】" + safe(post.getContent());
        return toDocuments(
                post,
                0,
                SOURCE_POST_BODY,
                header,
                body,
                post.getCreatedAt(),
                docVersion,
                "post:" + post.getPostId() + ":body:v" + docVersion
        );
    }

    private List<VectorDocument> buildCommentDocuments(PostSnapshot post,
                                                       CommentSnapshot comment,
                                                       long docVersion,
                                                       int minCommentLength) {
        String header = header(post);
        StringBuilder body = new StringBuilder();
        body.append("【帖子正文】").append(truncate(safe(post.getContent()), 600));
        body.append("\n【主评论】").append(safe(comment.getContent()));
        List<ReplySnapshot> replies = comment.getReplies().stream()
                .filter(reply -> textLength(reply.getContent()) >= minCommentLength)
                .toList();
        if (!replies.isEmpty()) {
            body.append("\n【评论回复】");
            for (ReplySnapshot reply : replies) {
                body.append("\n- ");
                if (StringUtils.hasText(reply.getTargetName())) {
                    body.append("回复 ").append(reply.getTargetName()).append("：");
                }
                body.append(safe(reply.getContent()));
            }
        }
        String baseId = "post:" + post.getPostId() + ":comment:" + comment.getCommentId() + ":v" + docVersion;
        return toDocuments(
                post,
                comment.getCommentId(),
                SOURCE_POST_COMMENT,
                header,
                body.toString(),
                comment.getCreatedAt(),
                docVersion,
                baseId
        );
    }

    private List<VectorDocument> toDocuments(PostSnapshot post,
                                             long commentId,
                                             String sourceType,
                                             String header,
                                             String body,
                                             LocalDateTime createdAt,
                                             long docVersion,
                                             String baseId) {
        List<String> chunks = TextChunker.split(
                body,
                properties.getRag().getChunkSize(),
                properties.getRag().getChunkMaxSize(),
                properties.getRag().getChunkOverlap()
        );
        if (chunks.isEmpty()) {
            chunks = List.of(body);
        }
        List<VectorDocument> documents = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            // 每个分片都保留帖子标题、postId、分区和标签
            String content = header + "\n" + chunks.get(i);
            VectorDocument document = new VectorDocument();
            document.setId(chunks.size() == 1 ? baseId : baseId + ":chunk:" + i);
            document.setPostId(post.getPostId());
            document.setCommentId(commentId);
            document.setTitle(truncate(safe(post.getTitle()), 512));
            document.setContent(truncate(content, 8192));
            document.setCategory(truncate(safe(post.getCategory()), 64));
            document.setTag(truncate(safe(post.getTag()), 128));
            document.setSourceType(sourceType);
            document.setCreatedAt(toEpochMillis(createdAt));
            document.setStatus(STATUS_ACTIVE);
            document.setDocVersion(docVersion);
            document.setChunkIndex(i);
            documents.add(document);
        }
        return documents;
    }

    private String header(PostSnapshot post) {
        return "【帖子标题】" + safe(post.getTitle())
                + "\n【帖子ID】" + post.getPostId()
                + "\n【分区】" + safe(post.getCategory())
                + "\n【标签】" + safe(post.getTag());
    }

    private int textLength(String text) {
        return text == null ? 0 : text.strip().length();
    }

    private String safe(String text) {
        return text == null ? "" : text.strip();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private long toEpochMillis(LocalDateTime time) {
        if (time == null) {
            return 0L;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
