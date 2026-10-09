package software_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software_market.config.security.UserContext;
import software_market.dto.request.rating.RatingDTO;
import software_market.dto.request.rating.SubmitRatingDTO;
import software_market.entity.post.Post;
import software_market.entity.Rating;
import software_market.exception.BizException;
import software_market.mapper.post.PostMapper;
import software_market.mapper.RatingMapper;
import software_market.service.RatingService;
import software_market.vo.rating.AverageRatingVO;
import software_market.vo.rating.GetStarsVO;

import java.util.List;

@Service
@AllArgsConstructor
public class RatingServiceImpl extends ServiceImpl<RatingMapper, Rating> implements RatingService {

    private final PostMapper postMapper;

    @Override
    public void submitRating(SubmitRatingDTO submitRatingDTO) {

        Integer rating = submitRatingDTO.getRating();
        Integer postID = submitRatingDTO.getPostID();
        if (rating == null) {
            throw new BizException("评分不能为空");
        }
        if (rating < 1 || rating > 5) {
            throw new BizException("评分必须在1-5之间");
        }
        if (postID == null) {
            throw new BizException("帖子ID不能为空");
        }
        Post post = postMapper.selectById(postID);
        if (post == null) {
            throw new BizException("帖子不存在");
        }
        Integer userId = UserContext.getUserId();
        LambdaQueryWrapper<Rating> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Rating::getUserId, userId)
                .eq(Rating::getItemType, "post")
                .eq(Rating::getItemId, postID);

        Rating oldRating = getOne(wrapper);

        // 已评分 -> 修改评分
        if (oldRating != null) {
            oldRating.setScore(rating);
            updateById(oldRating);
            return;
        }

        // 新评分
        postMapper.updateHeat(postID, 6.0);
        Rating rt = new Rating();

        rt.setUserId(userId);
        rt.setScore(rating);
        rt.setItemId(postID);
        rt.setItemType("post");

        save(rt);
    }

    @Override
    public int userPostRating(RatingDTO ratingDTO) {
        Integer postID = ratingDTO.getPostID();
        if (postID == null) {
            throw new BizException("帖子ID不能为空");
        }
        Post post = postMapper.selectById(postID);
        if (post == null) {
            throw new BizException("帖子不存在");
        }
        Integer userId = UserContext.getUserId();
        LambdaQueryWrapper<Rating> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Rating::getUserId, userId)
                .eq(Rating::getItemType, "post")
                .eq(Rating::getItemId, postID);
        Rating rating = getOne(wrapper);
        // 未评分返回 0
        if (rating == null) {
            return 0;
        }
        return rating.getScore();
    }

    @Override
    public AverageRatingVO getAverageRating(RatingDTO ratingDTO) {
        Integer postID = ratingDTO.getPostID();
        if (postID == null) {
            throw new BizException("帖子ID不能为空");
        }
        Post post = postMapper.selectById(postID);
        if (post == null) {
            throw new BizException("帖子不存在");
        }
        LambdaQueryWrapper<Rating> wrapper = Wrappers.lambdaQuery();

        wrapper.eq(Rating::getItemType, "post")
                .eq(Rating::getItemId, postID);
        List<Rating> lists = list(wrapper);
        AverageRatingVO averageRatingVO = new AverageRatingVO();
        // 暂无评分
        if (lists.isEmpty()) {
            averageRatingVO.setAverageRating(0.0);
            return averageRatingVO;
        }
        double averageRating = 0;
        for (Rating rating : lists) {
            averageRating += rating.getScore();
        }
        averageRating /= lists.size();
        // 保留两位小数（推荐）
        averageRating = Math.round(averageRating * 100.0) / 100.0;
        averageRatingVO.setAverageRating(averageRating);
        return averageRatingVO;
    }

    @Override
    public GetStarsVO getStars(RatingDTO ratingDTO) {
        Integer postID = ratingDTO.getPostID();
        if (postID == null) {
            throw new BizException("帖子ID不能为空");
        }
        Post post = postMapper.selectById(postID);
        if (post == null) {
            throw new BizException("帖子不存在");
        }
        LambdaQueryWrapper<Rating> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Rating::getItemType, "post")
                .eq(Rating::getItemId, postID);
        List<Rating> lists = list(wrapper);
        int[] stars = new int[5];
        for (Rating rating : lists) {
            Integer score = rating.getScore();
            if (score >= 1 && score <= 5) {
                stars[score - 1]++;
            }
        }
        GetStarsVO getStarsVO = new GetStarsVO();
        getStarsVO.setStar1(stars[0]);
        getStarsVO.setStar2(stars[1]);
        getStarsVO.setStar3(stars[2]);
        getStarsVO.setStar4(stars[3]);
        getStarsVO.setStar5(stars[4]);
        return getStarsVO;
    }
}