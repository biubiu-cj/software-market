package software_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import software_market.dto.request.post.*;
import software_market.entity.post.Post;
import software_market.entity.post.Tag;
import software_market.vo.post.GetPostNumVO;
import software_market.vo.post.GetPostTypeVO;
import software_market.vo.post.HeatResponseVO;

import java.util.List;

public interface PostService extends IService<Post> {
    Integer post(PostDTO postDTO);

    List<?> browse(@Valid BrowsePostDTO browsePostDTO);

    GetPostNumVO getPostNum(@Valid GetPostNumDTO getPostNumDTO);

    GetPostTypeVO getPostType(@Valid GetPostTypeDTO getPostTypeDTO);

    Object showDetails(@Valid ShowPostDetailsDTO showPostDetailsDTO);

    void updateBrowseNum(@Valid UpdateBrowseNumDTO updateBrowseNumDTO);

    void updateLike(@Valid UpdateDTO updateLikeDTO);

    void updateSave(@Valid UpdateDTO updateSaveDTO);

    void deletePost(@Valid DeletePostDTO deletePostDTO);

    void submitReport(@Valid SubmitReportDTO submitReportDTO);

    List<Tag> getTags();

    List<HeatResponseVO> calculateHeat();
}
