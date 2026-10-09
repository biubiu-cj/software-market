package software_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import software_market.dto.request.rating.RatingDTO;
import software_market.dto.request.rating.SubmitRatingDTO;
import software_market.entity.Rating;
import software_market.vo.rating.AverageRatingVO;
import software_market.vo.rating.GetStarsVO;

public interface RatingService extends IService<Rating> {
    void submitRating(SubmitRatingDTO submitRatingDTO);

    int userPostRating(RatingDTO ratingDTO);

    AverageRatingVO getAverageRating(RatingDTO ratingDTO);

    GetStarsVO getStars(RatingDTO ratingDTO);
}
