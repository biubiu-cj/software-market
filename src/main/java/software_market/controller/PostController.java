package software_market.controller;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import software_market.dto.request.post.*;
import software_market.entity.post.Tag;
import software_market.service.PostService;
import software_market.utils.Result;
import software_market.vo.post.GetPostNumVO;
import software_market.vo.post.GetPostTypeVO;
import software_market.vo.post.HeatResponseVO;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@AllArgsConstructor
public class PostController {
    private final PostService postService;

    /**
     * 发帖
     * @param postDTO
     * @return
     */
    @PostMapping("/post")
    public Result post(@RequestBody @Valid PostDTO postDTO) {
        postService.post(postDTO);
        return Result.success(null,"发帖成功");
    }

    /**
     * 查看帖子列表
     * @param browsePostDTO
     * @return
     */
    @PostMapping("/browse")
    public List<?> browse(@RequestBody @Valid BrowsePostDTO browsePostDTO) {
        List<?> list= postService.browse(browsePostDTO);
        return list;
    }

    /**
     * 查看帖子数量
     * @return
     */
    @PostMapping("/getPostNum")
    public Result<GetPostNumVO> getPostNum(@RequestBody @Valid GetPostNumDTO getPostNumDTO) {
        GetPostNumVO getPostNumVO = postService.getPostNum(getPostNumDTO);
        return Result.success(getPostNumVO,"获取帖子数量成功");
    }

    /**
     * 获取帖子类型
     * @param getPostTypeDTO
     * @return
     */
    @PostMapping("/getPostType")
    public Result<GetPostTypeVO> getPostType(@RequestBody @Valid GetPostTypeDTO getPostTypeDTO) {
        GetPostTypeVO getPostTypeVO = postService.getPostType(getPostTypeDTO);
        return Result.success(getPostTypeVO,"获取帖子类型成功");
    }

    /**
     * 展示商品详情
     * @param showPostDetailsDTO
     * @return
     */
    @PostMapping("/showDetails")
    public Result<Object> showDetails(@RequestBody @Valid ShowPostDetailsDTO showPostDetailsDTO) {
        Object postVO = postService.showDetails(showPostDetailsDTO);
        return Result.success(postVO,"获取帖子详情成功");
    }

    /**
     * 更新浏览量
     * @param updateBrowseNumDTO
     * @return
     */
    @PostMapping("/updateBrowseNum")
    public Result<Void> updateBrowseNum(@RequestBody @Valid UpdateBrowseNumDTO updateBrowseNumDTO) {
        postService.updateBrowseNum(updateBrowseNumDTO);
        return Result.success(null, "浏览量更新成功");
    }

    /**
     * 更新点赞状态
     * @param updateLikeDTO
     * @return
     */
    @PostMapping("/updateLike")
    public Result<Void> updateLike(@RequestBody @Valid UpdateDTO updateLikeDTO) {
        postService.updateLike(updateLikeDTO);
        return Result.success(null, "点赞状态更新成功");
    }

    /**
     * 更新收藏状态
     * @param updateSaveDTO
     * @return
     */
    @PostMapping("/updateSave")
    public Result<Void> updateSave(@RequestBody @Valid UpdateDTO updateSaveDTO) {
        postService.updateSave(updateSaveDTO);
        return Result.success(null, "收藏状态更新成功");
    }

    /**
     * 删除帖子
     * @param deletePostDTO
     * @return
     */
    @PostMapping("/deletePost")
    public Result<Void> deletePost(@RequestBody @Valid DeletePostDTO deletePostDTO) {
        postService.deletePost(deletePostDTO);
        return Result.success(null, "删除帖子成功");
    }

    /**
     * 举报帖子
     * @param submitReportDTO
     * @return
     */
    @PostMapping("/submitReport")
    public Result<Void> submitReport(@RequestBody @Valid SubmitReportDTO submitReportDTO) {
        postService.submitReport(submitReportDTO);
        return Result.success(null, "举报成功");
    }

    /**
     * 获取标签
     * @return
     */
    @GetMapping("/getTags")
    public Result<List<Tag>> getTags() {
        return Result.success(postService.getTags(), "获取标签成功");
    }

    @GetMapping("/calculateHeat")
    public Result<List<HeatResponseVO>> calculateHeat() {
        List<HeatResponseVO> list = postService.calculateHeat();
        return Result.success(list, "计算热度成功");
    }
}
