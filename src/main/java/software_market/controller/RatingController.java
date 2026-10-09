package software_market.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software_market.dto.request.rating.RatingDTO;
import software_market.dto.request.rating.SubmitRatingDTO;
import software_market.service.RatingService;
import software_market.utils.Result;
import software_market.vo.rating.AverageRatingVO;
import software_market.vo.rating.GetStarsVO;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@AllArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * 对帖子评分
     * @param submitRatingDTO
     * @return
     */
    @PostMapping("/submitRating")
    public Result submitRating(@RequestBody SubmitRatingDTO submitRatingDTO) {
        ratingService.submitRating(submitRatingDTO);
        return Result.success(null, "评分成功");
    }

    /**
     * 获取用户评分
     * @param ratingDTO
     * @return
     */
    @PostMapping("/userPostRating")
    public Result<Integer> userPostRating(@RequestBody RatingDTO ratingDTO) {
        int rating = ratingService.userPostRating(ratingDTO);
        return Result.success(rating, "获取用户评分成功");
    }


    /**
     * 获取平均评分
     * @param ratingDTO
     * @return
     */
    @PostMapping("/getAverageRating")
    public Result<AverageRatingVO> getAverageRating(@RequestBody RatingDTO ratingDTO) {
        AverageRatingVO averageRatingVO = ratingService.getAverageRating(ratingDTO);
        return Result.success(averageRatingVO, "获取平均评分成功");
    }

    @PostMapping("/getStars")
    public Result<GetStarsVO> getStars(@RequestBody RatingDTO ratingDTO) {
        GetStarsVO getStarsVO = ratingService.getStars(ratingDTO);
        return Result.success(getStarsVO, "获取星级成功");
    }
}
