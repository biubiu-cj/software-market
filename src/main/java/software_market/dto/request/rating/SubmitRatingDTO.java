package software_market.dto.request.rating;

import lombok.Data;

@Data
public class SubmitRatingDTO {
    private Integer postID;
    private Integer rating;
}
