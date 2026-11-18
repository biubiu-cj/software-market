package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.Rating;
import software_market.vo.rating.PostRatingStatsVO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RatingMapper extends BaseMapper<Rating> {

    @Delete("DELETE FROM rating WHERE item_type = #{itemType} AND item_id = #{itemId}")
    void deleteByItem(@Param("itemType") String itemType, @Param("itemId") Integer itemId);

    @Delete("DELETE FROM rating WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Integer userId);

    /**
     * 批量统计一页帖子的评分：平均分 + 1~5 星分布。
     * 只返回有评分的帖子，没有评分的帖子由调用方补默认值。
     */
    @Select("""
            <script>
            SELECT item_id AS postId,
                   AVG(score) AS averageScore,
                   SUM(score = 1) AS star1,
                   SUM(score = 2) AS star2,
                   SUM(score = 3) AS star3,
                   SUM(score = 4) AS star4,
                   SUM(score = 5) AS star5
            FROM rating
            WHERE item_type = 'post'
              AND item_id IN
              <foreach collection="postIds" item="postId" open="(" separator="," close=")">
                #{postId}
              </foreach>
            GROUP BY item_id
            </script>
            """)
    List<PostRatingStatsVO> selectPostRatingStats(@Param("postIds") Collection<Integer> postIds);
}
