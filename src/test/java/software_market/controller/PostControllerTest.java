package software_market.controller;

import org.junit.jupiter.api.Test;
import software_market.dto.request.post.PostDTO;
import software_market.service.PostService;
import software_market.utils.Result;
import software_market.vo.post.PostCreateVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostControllerTest {

    @Test
    void postReturnsCreatedPostId() {
        PostService postService = mock(PostService.class);
        when(postService.post(any(PostDTO.class))).thenReturn(123);
        PostController controller = new PostController(postService);

        Result<PostCreateVO> result = controller.post(new PostDTO());

        assertEquals(200, result.getCode());
        assertEquals(123, result.getData().getPostId());
    }
}
