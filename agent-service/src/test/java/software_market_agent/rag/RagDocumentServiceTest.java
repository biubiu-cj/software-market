package software_market_agent.rag;

import org.junit.jupiter.api.Test;
import software_market_agent.config.AgentProperties;
import software_market_agent.model.milvus.VectorDocument;
import software_market_agent.model.post.CommentSnapshot;
import software_market_agent.model.post.PostSnapshot;
import software_market_agent.model.post.ReplySnapshot;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RagDocumentServiceTest {

    private final RagDocumentService service = new RagDocumentService(properties());

    @Test
    void buildsCommentDocumentAndFiltersShortNoise() {
        PostSnapshot post = post(1);
        CommentSnapshot shortComment = comment(10, "太短了");
        CommentSnapshot longComment = comment(20, "这是一个足够长的历史回复内容，可以用来回答当前问题。");
        longComment.getReplies().add(reply(100, "短"));
        longComment.getReplies().add(reply(101, "这是另一个足够长的回复内容，也应该被保留下来。"));
        post.setComments(List.of(shortComment, longComment));

        List<VectorDocument> docs = service.build(post, 1L);

        assertEquals(1, docs.size());
        VectorDocument doc = docs.get(0);
        assertEquals("post:1:comment:20:v1", doc.getId());
        assertEquals("POST_COMMENT", doc.getSourceType());
        assertEquals(20, doc.getCommentId());
        assertTrue(doc.getContent().contains("【帖子标题】测试帖子"));
        assertTrue(doc.getContent().contains("【帖子ID】1"));
        assertTrue(doc.getContent().contains("这是一个足够长的历史回复内容"));
        assertTrue(doc.getContent().contains("这是另一个足够长的回复内容"));
        assertFalse(doc.getContent().contains("短"));
    }

    @Test
    void fallsBackToBodyDocumentWhenNoUsableComments() {
        PostSnapshot post = post(2);
        post.setComments(List.of(comment(11, "短")));

        List<VectorDocument> docs = service.build(post, 3L);

        assertEquals(1, docs.size());
        VectorDocument doc = docs.get(0);
        assertEquals("post:2:body:v3", doc.getId());
        assertEquals("POST_BODY", doc.getSourceType());
        assertEquals(0, doc.getCommentId());
        assertTrue(doc.getContent().contains("【正文】"));
    }

    @Test
    void keepsHeaderInEveryChunk() {
        PostSnapshot post = post(3);
        CommentSnapshot comment = comment(30, "很长的评论内容。".repeat(300));
        post.setComments(List.of(comment));

        List<VectorDocument> docs = service.build(post, 7L);

        assertTrue(docs.size() > 1);
        for (VectorDocument doc : docs) {
            assertTrue(doc.getContent().contains("【帖子标题】测试帖子"));
            assertTrue(doc.getContent().contains("【帖子ID】3"));
            assertTrue(doc.getId().contains("post:3:comment:30:v7"));
        }
    }

    private AgentProperties properties() {
        AgentProperties properties = new AgentProperties();
        properties.getRag().setMinCommentLength(8);
        properties.getRag().setChunkSize(600);
        properties.getRag().setChunkMaxSize(800);
        properties.getRag().setChunkOverlap(80);
        return properties;
    }

    private PostSnapshot post(int id) {
        PostSnapshot post = new PostSnapshot();
        post.setPostId(id);
        post.setTitle("测试帖子");
        post.setContent("这是帖子正文，用于测试知识文档构建。");
        post.setCategory("打听求助");
        post.setTag("测试");
        post.setCreatedAt(LocalDateTime.now());
        return post;
    }

    private CommentSnapshot comment(int id, String content) {
        CommentSnapshot comment = new CommentSnapshot();
        comment.setCommentId(id);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }

    private ReplySnapshot reply(int id, String content) {
        ReplySnapshot reply = new ReplySnapshot();
        reply.setReplyId(id);
        reply.setContent(content);
        reply.setTargetName("同学A");
        reply.setCreatedAt(LocalDateTime.now());
        return reply;
    }
}
