package software_market.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software_market.config.security.UserContext;
import software_market.dto.request.comment.PostCcommentDTO;
import software_market.entity.User;
import software_market.entity.comment.Ccomment;
import software_market.entity.comment.Pcomment;
import software_market.entity.post.Post;
import software_market.event.NotificationEvent;
import software_market.event.RagIndexEventPublisher;
import software_market.mapper.NoticeMapper;
import software_market.mapper.UserMapper;
import software_market.mapper.comment.CcdenyMapper;
import software_market.mapper.comment.CclikeMapper;
import software_market.mapper.comment.CcommentMapper;
import software_market.mapper.comment.PcdenyMapper;
import software_market.mapper.comment.PclikeMapper;
import software_market.mapper.comment.PcommentMapper;
import software_market.mapper.post.PostMapper;
import software_market.service.RatingService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommentServiceImplNotificationTest {

    private final CcommentMapper ccommentMapper = mock(CcommentMapper.class);
    private final PcommentMapper pcommentMapper = mock(PcommentMapper.class);
    private final PostMapper postMapper = mock(PostMapper.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final RatingService ratingService = mock(RatingService.class);
    private final PclikeMapper pclikeMapper = mock(PclikeMapper.class);
    private final PcdenyMapper pcdenyMapper = mock(PcdenyMapper.class);
    private final CclikeMapper cclikeMapper = mock(CclikeMapper.class);
    private final CcdenyMapper ccdenyMapper = mock(CcdenyMapper.class);
    private final NoticeMapper noticeMapper = mock(NoticeMapper.class);
    private final KafkaProducerService kafkaProducerService = mock(KafkaProducerService.class);
    private final RagIndexEventPublisher ragIndexEventPublisher = mock(RagIndexEventPublisher.class);

    private final CommentServiceImpl service = new CommentServiceImpl(
            ccommentMapper, pcommentMapper, postMapper, userMapper, ratingService,
            pclikeMapper, pcdenyMapper, cclikeMapper, ccdenyMapper, noticeMapper,
            kafkaProducerService, ragIndexEventPublisher);

    @BeforeEach
    void setUp() {
        UserContext.setUserId(1);
        mockPostAndPcomment(2);
        when(ccommentMapper.countDuplicate(anyInt(), eq(20), eq("hi"))).thenReturn(0);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void topLevelReplyNotifiesCommentAuthor() {
        when(userMapper.selectById(2)).thenReturn(user(2, "bob"));

        service.postCcomment(dto(null));

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(kafkaProducerService).sendNotificationEvent(captor.capture());

        NotificationEvent event = captor.getValue();
        assertEquals(2, event.getReceiver());
        assertEquals(1, event.getSender());
        assertEquals("ccomment", event.getType());
        assertEquals(10, event.getTarget());
        assertTrue(event.getText().startsWith("回复了你的评论"));
    }

    @Test
    void nestedReplyNotifiesRepliedCommentAuthor() {
        when(userMapper.selectById(3)).thenReturn(user(3, "carol"));
        Ccomment reply = new Ccomment();
        reply.setCcommentId(30);
        reply.setUserId(3);
        when(ccommentMapper.selectById(30)).thenReturn(reply);

        service.postCcomment(dto(30));

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(kafkaProducerService).sendNotificationEvent(captor.capture());
        assertEquals(3, captor.getValue().getReceiver());
        assertEquals("ccomment", captor.getValue().getType());
    }

    @Test
    void selfReplyDoesNotNotify() {
        UserContext.setUserId(2);
        when(userMapper.selectById(2)).thenReturn(user(2, "bob"));

        service.postCcomment(dto(null));

        verifyNoInteractions(kafkaProducerService);
    }

    private void mockPostAndPcomment(int pcommentUserId) {
        Post post = new Post();
        post.setPostId(10);
        post.setUserId(99);
        post.setCommentNum(0);
        post.setHeat(0.0);
        when(postMapper.selectById(10)).thenReturn(post);

        Pcomment pcomment = new Pcomment();
        pcomment.setPcommentId(20);
        pcomment.setUserId(pcommentUserId);
        when(pcommentMapper.selectById(20)).thenReturn(pcomment);
    }

    private PostCcommentDTO dto(Integer replyCcommentId) {
        PostCcommentDTO dto = new PostCcommentDTO();
        dto.setPcommentID(20);
        dto.setPostID(10);
        dto.setContent("hi");
        dto.setCcommentID(replyCcommentId);
        return dto;
    }

    private User user(int userId, String name) {
        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        return user;
    }
}