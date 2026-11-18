package software_market.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software_market.entity.Rating;
import software_market.mapper.RatingMapper;
import software_market.mapper.post.PostMapper;
import software_market.vo.rating.PostRatingStatsVO;
import software_market.vo.rating.RatingSummaryVO;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RatingServiceImplBatchTest {

    private final RatingMapper ratingMapper = mock(RatingMapper.class);
    private final RatingServiceImpl ratingService = new RatingServiceImpl(mock(PostMapper.class));

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ratingService, "baseMapper", ratingMapper);
    }

    @Test
    void aggregatesAverageStarsAndUserRatingInOnePage() {
        PostRatingStatsVO stats = new PostRatingStatsVO();
        stats.setPostId(1);
        stats.setAverageScore(4.333);
        stats.setStar1(0L);
        stats.setStar2(0L);
        stats.setStar3(0L);
        stats.setStar4(1L);
        stats.setStar5(2L);
        when(ratingMapper.selectPostRatingStats(List.of(1, 2))).thenReturn(List.of(stats));

        Rating userRating = new Rating();
        userRating.setItemId(1);
        userRating.setScore(5);
        when(ratingMapper.selectList(any())).thenReturn(List.of(userRating));

        Map<Integer, RatingSummaryVO> summaries =
                ratingService.getRatingSummaries(List.of(1, 2), 7);

        RatingSummaryVO first = summaries.get(1);
        assertNotNull(first);
        assertEquals(4.33, first.getAverageRating(), 0.001);
        assertEquals(2, first.getStars().getStar5());
        assertEquals(1, first.getStars().getStar4());
        assertEquals(5, first.getUserRating());

        RatingSummaryVO second = summaries.get(2);
        assertNotNull(second);
        assertEquals(0.0, second.getAverageRating(), 0.001);
        assertEquals(0, second.getStars().getStar1());
        assertEquals(0, second.getUserRating());
    }

    @Test
    void emptyPostIdsDoesNotHitDatabase() {
        Map<Integer, RatingSummaryVO> summaries = ratingService.getRatingSummaries(List.of(), 7);

        assertEquals(Map.of(), summaries);
        verifyNoInteractions(ratingMapper);
    }
}