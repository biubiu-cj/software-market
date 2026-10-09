package software_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import software_market.dto.request.shop.GetProductDTO;
import software_market.dto.request.shop.PostProductDTO;
import software_market.dto.request.shop.ProductDetailDTO;
import software_market.dto.request.shop.UploadPhotoDTO;
import software_market.entity.Product;
import software_market.vo.shop.ProductCountVO;
import software_market.vo.shop.ProductVO;
import software_market.vo.shop.UploadPhotoVO;

import java.util.List;

public interface ShopService extends IService<Product> {
    void postProduct(PostProductDTO postProductDTO);

    UploadPhotoVO uploadPhotos(UploadPhotoDTO uploadPhotoDTO);

    List<ProductVO> getProducts(GetProductDTO getProductDTO);

    ProductVO getProductDetail(ProductDetailDTO productDetailDTO);

    ProductCountVO getProductNum(GetProductDTO getProductDTO);

    void deleteProduct(ProductDetailDTO productDetailDTO);

    void saleProduct(ProductDetailDTO productDetailDTO);
}
