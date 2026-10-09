package software_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import software_market.dto.request.comment.*;
import software_market.vo.comment.CommentResponseVO;

import java.util.List;

public interface CommentService  {
    List<CommentResponseVO> showPcomments(@Valid ShowPcommentsDTO showPcommentsDTO);

    void postPcomment(@Valid PostCommentDTO postCommentDTO);

    void postCcomment(@Valid PostCcommentDTO postCcommentDTO);

    void deletePcomment(@Valid PcommentDTO deletePcommentDTO);

    void deleteCcomment(@Valid CcommentDTO deleteCcommentDTO);

    void updatePcommentLike(@Valid PcommentDTO updatePcommentLikeDTO);

    void updatePcommentDeny(@Valid PcommentDTO updatePcommentDenyDTO);

    void updateCcommentLike(@Valid CcommentDTO updateCcommentLikeDTO);

    void updateCcommentDeny(@Valid CcommentDTO updateCcommentDenyDTO);
}
