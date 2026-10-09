package software_market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software_market.dto.request.shop.GetProductDTO;
import software_market.dto.request.shop.PostProductDTO;
import software_market.dto.request.shop.ProductDetailDTO;
import software_market.dto.request.shop.UploadPhotoDTO;
import software_market.service.ShopService;
import software_market.utils.Result;
import software_market.vo.shop.ProductCountVO;
import software_market.vo.shop.ProductVO;
import software_market.vo.shop.UploadPhotoVO;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ShopController {


    private final ShopService shopService;

    /**
     * 发布商品
     * @param postProductDTO
     * @return
     */
    @PostMapping("/postProduct")
    public Result postProduct(@RequestBody PostProductDTO postProductDTO){
        shopService.postProduct(postProductDTO);
        return Result.success(null,"发布商品成功");
    }

    /**
     * 上传图片
     * @param uploadPhotoDTO
     * @return
     */
    @PostMapping("/uploadPhotos")
    public Result<UploadPhotoVO> uploadPhotos(UploadPhotoDTO uploadPhotoDTO) {
        UploadPhotoVO uploadPhotoVO= shopService.uploadPhotos(uploadPhotoDTO);
        return Result.success(uploadPhotoVO,"上传图片成功");
    }

    /**
     * 获取商品
     * @param getProductDTO
     * @return
     */
    @PostMapping("/getProducts")
    public List<ProductVO> getProducts(@RequestBody GetProductDTO getProductDTO){
        List<ProductVO> list= shopService.getProducts(getProductDTO);
        return list;
    }

    /**
     * 获取商品详情
     * @param
     * @return
     */
    @PostMapping("/getProductDetail")
    public ProductVO getProductDetail(@RequestBody ProductDetailDTO productDetailDTO){
        ProductVO productVO= shopService.getProductDetail(productDetailDTO);
        return productVO;
    }

    /**
     * 获取商品数量
     * @param getProductDTO
     * @return
     */
    @PostMapping("/getProductNum")
    public Result<ProductCountVO> getProductNum(@RequestBody GetProductDTO getProductDTO){
        ProductCountVO productCountVO= shopService.getProductNum(getProductDTO);
        return Result.success(productCountVO);
    }

    /**
     * 删除商品（软删除）
     * @param productDetailDTO
     * @return
     */
    @PostMapping("/deleteProduct")
    public Result deleteProduct(@RequestBody ProductDetailDTO productDetailDTO) {
        shopService.deleteProduct(productDetailDTO);
        return Result.success(null, "删除商品成功");
    }

    /**
     * 标记商品出售
     * @param productDetailDTO
     * @return
     */
    @PostMapping("/saleProduct")
    public Result saleProduct(@RequestBody ProductDetailDTO productDetailDTO) {
        shopService.saleProduct(productDetailDTO);
        return Result.success(null, "标记商品出售成功");
    }
}
