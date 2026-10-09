package software_market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software_market.cache.MultiLevelCache;
import software_market.config.security.UserContext;
import software_market.dto.request.shop.GetProductDTO;
import software_market.dto.request.shop.PostProductDTO;
import software_market.dto.request.shop.ProductDetailDTO;
import software_market.dto.request.shop.UploadPhotoDTO;
import software_market.entity.Product;
import software_market.entity.User;
import software_market.exception.BizException;
import software_market.mapper.ProductMapper;
import software_market.mapper.UserMapper;
import software_market.service.ShopService;
import software_market.vo.shop.ProductCountVO;
import software_market.vo.shop.ProductVO;
import software_market.vo.shop.UploadPhotoVO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ProductMapper, Product> implements ShopService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final MultiLevelCache cache;

    private static final String PRODUCT_DETAIL_KEY = "product:detail:";
    private static final int PRODUCT_L2_TTL = 1800; // 30min

    @Override
    public void postProduct(PostProductDTO postProductDTO) {

        Integer userID = postProductDTO.getUserID();

        // 1. 校验用户是否存在
        User user = userMapper.selectById(userID);

        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 2. 创建商品对象
        Product product = new Product();
        product.setUserId(userID);
        product.setProductName(postProductDTO.getTitle());
        product.setDescription(postProductDTO.getContent());
        product.setPrice(postProductDTO.getPrice());

        // 3. 处理图片列表
        List<String> photos = postProductDTO.getPhotos();

        if (photos != null && !photos.isEmpty()) {

            // 去除空字符串
            List<String> validPhotos = photos.stream()
                    .filter(photo -> photo != null && !photo.trim().isEmpty())
                    .toList();

            // 转成逗号分隔字符串
            String photoStr = String.join(",", validPhotos);

            product.setPhotos(photoStr);
        }

        // 4. 是否匿名
        product.setIsAnonymous(postProductDTO.getIsAnonymous());

        // 5. 默认状态
        product.setIsSold(false);
        product.setIsDelete(false);

        // 6. 保存商品
        boolean success = save(product);

        if (!success) {
            throw new BizException("商品发布失败");
        }
    }

    private static final String AVATAR_URL_PREFIX = "/avatar/";

    @Value("${app.upload-dir}")
    private String uploadDir;

    private String getAvatarUploadDir() {
        return new File(uploadDir, "avatar").getAbsoluteFile() + File.separator;
    }
    @Override
    public UploadPhotoVO uploadPhotos(UploadPhotoDTO dto) {

        MultipartFile file = dto.getFile();

        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BizException("只能上传图片");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID() + suffix;

        String uploadDir = getAvatarUploadDir();
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            File dest = new File(dir, filename);
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("uploadPhotos文件保存失败", e);
            throw new BizException("文件保存失败");
        }
        UploadPhotoVO vo = new UploadPhotoVO();
        vo.setFileURL(AVATAR_URL_PREFIX + filename);
        vo.setMessage("上传成功");

        return vo;
    }

    @Override
    public List<ProductVO> getProducts(GetProductDTO getProductDTO) {
        Integer userId = UserContext.getUserId();
        String searchInfo = getProductDTO.getSearchInfo();
        String searchSort = getProductDTO.getSearchSort();
        Integer limit = getProductDTO.getLimit();
        Integer offset = getProductDTO.getOffset();
        List<Product> products;
        if ("home".equals(searchSort)) {
            if(searchInfo==null||searchInfo.isEmpty()){
                products = productMapper.selectPage(limit, offset);
            }else {
                products=productMapper.searchByKeyword(searchInfo,limit,offset);
            }
        }
        else{
            products = productMapper.selectByUserId(userId, limit, offset);
        }
        List<ProductVO> vos = new ArrayList<>();
        for (Product product : products) {
            ProductVO vo = new ProductVO();
            vo.setProductId(product.getProductId());
            vo.setPrice(product.getPrice());
            vo.setName(product.getProductName());
            vo.setDescription(product.getDescription());
            vo.setIsAnonymous(product.getIsAnonymous());
            vo.setIsSold(product.getIsSold());
            if (product.getPhotos() != null && !product.getPhotos().isEmpty()) {
                vo.setPhotos(Arrays.asList(product.getPhotos().split(",")));
            } else {
                vo.setPhotos(new ArrayList<>());
            }
            // 卖家信息
            if ("home".equals(searchSort)) {
                User seller = userMapper.selectById(product.getUserId());
                vo.setSellerId(seller != null ? seller.getUserId() : 0);
                vo.setSeller(seller != null ? seller.getName() : "未知用户");
            } else {
                vo.setSellerId(userId);
                User seller = userMapper.selectById(userId);
                vo.setSeller(seller != null ? seller.getName() : "未知用户");
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public ProductVO getProductDetail(ProductDetailDTO productDetailDTO) {
        Integer productId = productDetailDTO.getProductId();
        ProductVO vo = cache.get(PRODUCT_DETAIL_KEY + productId, ProductVO.class, PRODUCT_L2_TTL,
                () -> {
                    Product product = productMapper.selectById(productId);
                    if (product == null) return null;
                    User user = userMapper.selectById(product.getUserId());
                    ProductVO v = new ProductVO();
                    v.setSellerId(user != null ? user.getUserId() : 0);
                    v.setSeller(user != null ? user.getName() : "未知用户");
                    v.setProductId(product.getProductId());
                    v.setPrice(product.getPrice());
                    v.setName(product.getProductName());
                    v.setDescription(product.getDescription());
                    v.setIsAnonymous(product.getIsAnonymous());
                    v.setIsSold(product.getIsSold());
                    if (product.getPhotos() != null && !product.getPhotos().isEmpty()) {
                        v.setPhotos(Arrays.asList(product.getPhotos().split(",")));
                    } else {
                        v.setPhotos(new ArrayList<>());
                    }
                    return v;
                });
        if (vo == null) throw new BizException("商品不存在");
        return vo;
    }

    @Override
    public ProductCountVO getProductNum(GetProductDTO getProductDTO) {
        Integer limit = getProductDTO.getLimit();
        Integer offset = getProductDTO.getOffset();
        String searchInfo = getProductDTO.getSearchInfo();
        String searchSort = getProductDTO.getSearchSort();
        Integer userId = UserContext.getUserId();
        ProductCountVO vo = new ProductCountVO();
        if ("home".equals(searchSort)) {
            if(searchInfo==null||searchInfo.isEmpty()){
                vo.setProductCount(productMapper.selectCount(limit,offset));
            }else {
                vo.setProductCount(productMapper.searchCountByKeyword(searchInfo,limit,offset));
            }
        }
        else{
            vo.setProductCount(productMapper.selectCountByUserId(userId,limit,offset));
        }
        return vo;
    }

    @Override
    public void deleteProduct(ProductDetailDTO productDetailDTO) {
        Integer productId = productDetailDTO.getProductId();
        Product product = productMapper.selectById(productId);
        if(product==null||product.getIsDelete()){
            throw new BizException("商品不存在");
        }
        Integer currentUserId = UserContext.getUserId();
        if(!product.getUserId().equals(currentUserId)){
            throw new BizException("无权限删除该商品");
        }
        product.setIsDelete(true);
        productMapper.updateById(product);
        cache.evict(PRODUCT_DETAIL_KEY + productId);
    }

    @Override
    public void saleProduct(ProductDetailDTO productDetailDTO) {
        Integer productId = productDetailDTO.getProductId();
        Product product = productMapper.selectById(productId);
        if(product==null||product.getIsDelete()){
            throw new BizException("商品不存在");
        }
        Integer currentUserId = UserContext.getUserId();
        if(!product.getUserId().equals(currentUserId)){
            throw new BizException("无权限出售该商品");
        }
        if (Boolean.TRUE.equals(product.getIsSold())) {
            throw new BizException("商品已出售");
        }
        product.setIsSold(true);
        int i = productMapper.updateById(product);
        if (i == 0) {
            throw new BizException("出售失败");
        }
        cache.evict(PRODUCT_DETAIL_KEY + productId);
    }
}
