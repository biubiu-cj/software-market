package software_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import software_market.entity.Product;
import software_market.vo.shop.ProductVO;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    /**
     * 首页浏览：关键词搜索（不包含已删除的）
     */
    @Select("SELECT * FROM product " +
            "WHERE is_delete = 0 " +
            "  AND (product_name LIKE CONCAT('%', #{keyword}, '%') " +
            "    OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY product_id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Product> searchByKeyword(@Param("keyword") String keyword,
                                  @Param("limit") Integer limit,
                                  @Param("offset") Integer offset);

    /**
     * 首页浏览：无关键词，全量分页（不包含已删除的）
     */
    @Select("SELECT * FROM product " +
            "WHERE is_delete = 0 " +
            "ORDER BY product_id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Product> selectPage(@Param("limit") Integer limit,
                             @Param("offset") Integer offset);

    /**
     * 我的商品：当前用户发布的商品（不包含已删除的）
     */
    @Select("SELECT * FROM product " +
            "WHERE is_delete = 0 AND user_id = #{userId} " +
            "ORDER BY product_id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<Product> selectByUserId(@Param("userId") Integer userId,
                                 @Param("limit") Integer limit,
                                 @Param("offset") Integer offset);


    @Select("SELECT COUNT(*) FROM product WHERE is_delete = 0 " +
            "LIMIT #{limit} OFFSET #{offset}")
    Integer selectCount(@Param("limit") Integer limit,
                        @Param("offset") Integer offset);

    @Select("SELECT COUNT(*) FROM product WHERE is_delete = 0 " +
            "AND (product_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "LIMIT #{limit} OFFSET #{offset}")
    Integer searchCountByKeyword(@Param("keyword") String keyword,
                                 @Param("limit") Integer limit,
                                 @Param("offset") Integer offset);

    @Select("SELECT COUNT(*) FROM product WHERE is_delete = 0 AND user_id = #{userId} "+
            "LIMIT #{limit} OFFSET #{offset}")
    Integer selectCountByUserId(@Param("userId") Integer userId,
                                @Param("limit") Integer limit,
                                @Param("offset") Integer offset);

    @Delete("DELETE FROM product WHERE user_id = #{userId}")
    void hardDeleteByUserId(@Param("userId") Integer userId);
}
