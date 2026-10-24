package software_market_agent.datasource;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import software_market_agent.model.post.CommentSnapshot;
import software_market_agent.model.post.PostSnapshot;
import software_market_agent.model.post.ReplySnapshot;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 只读访问主应用数据库，只用于整帖重建 RAG 知识文档。
 * 生产环境应使用只有 SELECT 权限的账号。
 */
@Component
@RequiredArgsConstructor
public class RagSnapshotReader {

    private final JdbcTemplate jdbcTemplate;

    public PostSnapshot read(Integer postId) {
        List<PostSnapshot> posts = jdbcTemplate.query(
                "SELECT post_id, title, ptext, category, tag, post_time FROM post WHERE post_id = ?",
                (rs, rowNum) -> {
                    PostSnapshot post = new PostSnapshot();
                    post.setPostId(rs.getInt("post_id"));
                    post.setTitle(rs.getString("title"));
                    post.setContent(rs.getString("ptext"));
                    post.setCategory(rs.getString("category"));
                    post.setTag(rs.getString("tag"));
                    post.setCreatedAt(toLocalDateTime(rs.getTimestamp("post_time")));
                    return post;
                },
                postId
        );
        if (posts.isEmpty()) {
            return null;
        }
        PostSnapshot post = posts.get(0);

        List<CommentSnapshot> comments = jdbcTemplate.query(
                "SELECT pcomment_id, pctext, time FROM pcomment WHERE ptarget_id = ? ORDER BY pcomment_id",
                (rs, rowNum) -> {
                    CommentSnapshot comment = new CommentSnapshot();
                    comment.setCommentId(rs.getInt("pcomment_id"));
                    comment.setContent(rs.getString("pctext"));
                    comment.setCreatedAt(toLocalDateTime(rs.getTimestamp("time")));
                    return comment;
                },
                postId
        );

        if (!comments.isEmpty()) {
            Map<Integer, CommentSnapshot> byId = new LinkedHashMap<>();
            comments.forEach(comment -> byId.put(comment.getCommentId(), comment));

            jdbcTemplate.query(
                    """
                    SELECT cc.ccomment_id, cc.ctarget_id, cc.cctext, cc.time, cc.usertargetName
                    FROM ccomment cc
                    JOIN pcomment pc ON cc.ctarget_id = pc.pcomment_id
                    WHERE pc.ptarget_id = ?
                    ORDER BY cc.ccomment_id
                    """,
                    rs -> {
                        CommentSnapshot parent = byId.get(rs.getInt("ctarget_id"));
                        if (parent != null) {
                            ReplySnapshot reply = new ReplySnapshot();
                            reply.setReplyId(rs.getInt("ccomment_id"));
                            reply.setContent(rs.getString("cctext"));
                            reply.setTargetName(rs.getString("usertargetName"));
                            reply.setCreatedAt(toLocalDateTime(rs.getTimestamp("time")));
                            parent.getReplies().add(reply);
                        }
                    },
                    postId
            );
        }

        post.setComments(comments);
        return post;
    }

    public List<Integer> allPostIds() {
        return jdbcTemplate.query(
                "SELECT post_id FROM post ORDER BY post_id",
                (rs, rowNum) -> rs.getInt("post_id")
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
