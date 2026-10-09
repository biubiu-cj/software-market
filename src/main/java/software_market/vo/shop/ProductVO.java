package software_market.vo.shop;

import lombok.Data;

import java.util.List;

@Data
public class ProductVO {
    private Integer productId;
    private Integer sellerId;
    private String seller;
    private Integer price;
    private String name;
    private String description;
    private List<String> photos;
    private Boolean isAnonymous;
    private Boolean isSold;
}
