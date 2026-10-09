package software_market.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software_market.dto.request.comment.*;
import software_market.service.CommentService;
import software_market.utils.Result;
import software_market.vo.comment.CommentResponseVO;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    /**
     * 获取评论树
     * @param showPcommentsDTO
     * @return
     */
    @PostMapping("/showPcomments")
    public List<CommentResponseVO> showPcomments(@RequestBody @Valid ShowPcommentsDTO showPcommentsDTO){
        return commentService.showPcomments(showPcommentsDTO);
    }

    /**
     * 发表一级评论
     * @param postCommentDTO
     * @return
     */
    @PostMapping("/postPcomment")
    public Result<Void> postPcomment(@RequestBody @Valid PostCommentDTO postCommentDTO){
        commentService.postPcomment(postCommentDTO);
        return Result.success(null,"评论成功");
    }

    /**
     * 发表二级评论
     * @param postCcommentDTO
     * @return
     */
    @PostMapping("/postCcomment")
    public Result<Void> postCcomment(@RequestBody @Valid PostCcommentDTO postCcommentDTO){
        commentService.postCcomment(postCcommentDTO);
        return Result.success(null,"评论成功");
    }

    /**
     * 删除评论
     * @param deletePcommentDTO
     * @return
     */
    @PostMapping("/deletePcomment")
    public Result<Void> deletePcomment(@RequestBody @Valid PcommentDTO deletePcommentDTO){
        commentService.deletePcomment(deletePcommentDTO);
        return Result.success(null,"删除评论成功");
    }

    /**
     * 删除二级评论
     * @param deleteCcommentDTO
     * @return
     */
    @PostMapping("/deleteCcomment")
    public Result<Void> deleteCcomment(@RequestBody @Valid CcommentDTO deleteCcommentDTO){
        commentService.deleteCcomment(deleteCcommentDTO);
        return Result.success(null,"删除评论成功");
    }

    /**
     * 点赞评论
     * @param updatePcommentLikeDTO
     * @return
     */
    @PostMapping("/updatePcommentLike")
    public Result<Void> updatePcommentLike(@RequestBody @Valid PcommentDTO updatePcommentLikeDTO){
        commentService.updatePcommentLike(updatePcommentLikeDTO);
        return Result.success(null,"点赞成功");
    }

    /**
     * 点踩评论
     * @param updatePcommentDenyDTO
     * @return
     */
    @PostMapping("/updatePcommentDeny")
    public Result<Void> updatePcommentDeny(@RequestBody @Valid PcommentDTO updatePcommentDenyDTO) {
        commentService.updatePcommentDeny(updatePcommentDenyDTO);
        return Result.success(null, "点踩成功");
    }

    /**
     * 点赞二级评论
     * @param updateCcommentLikeDTO
     * @return
     */
    @PostMapping("/updateCcommentLike")
    public Result<Void> updateCcommentLike(@RequestBody @Valid CcommentDTO updateCcommentLikeDTO){
        commentService.updateCcommentLike(updateCcommentLikeDTO);
        return Result.success(null,"点赞成功");
    }

    /**
     * 点踩二级评论
     * @param updateCcommentDenyDTO
     * @return
     */
    @PostMapping("/updateCcommentDeny")
    public Result<Void> updateCcommentDeny(@RequestBody @Valid CcommentDTO updateCcommentDenyDTO) {
        commentService.updateCcommentDeny(updateCcommentDenyDTO);
        return Result.success(null, "点踩成功");
    }
}
