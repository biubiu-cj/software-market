package software_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import software_market.config.security.JwtProvider;
import software_market.config.security.UserContext;
import software_market.constant.RedisKeyConstant;
import software_market.dto.request.user.*;
import software_market.dto.response.TokenPair;
import software_market.entity.Cdkey;
import software_market.entity.Notice;
import software_market.entity.Rating;
import software_market.entity.RefreshToken;
import software_market.entity.User;
import software_market.entity.comment.Ccomment;
import software_market.entity.comment.Ccdeny;
import software_market.entity.comment.Cclike;
import software_market.entity.comment.Pcdeny;
import software_market.entity.comment.Pclike;
import software_market.entity.comment.Pcomment;
import software_market.entity.post.Pbrowse;
import software_market.entity.post.Plike;
import software_market.entity.post.Psave;
import software_market.exception.BizException;
import software_market.mapper.CdkeyMapper;
import software_market.mapper.NoticeMapper;
import software_market.mapper.ProductMapper;
import software_market.mapper.RatingMapper;
import software_market.mapper.RefreshTokenMapper;
import software_market.mapper.UserMapper;
import software_market.mapper.comment.CcommentMapper;
import software_market.mapper.comment.CcdenyMapper;
import software_market.mapper.comment.CclikeMapper;
import software_market.mapper.comment.PcdenyMapper;
import software_market.mapper.comment.PclikeMapper;
import software_market.mapper.comment.PcommentMapper;
import software_market.mapper.post.PbrowseMapper;
import software_market.mapper.post.PlikeMapper;
import software_market.mapper.post.PostMapper;
import software_market.mapper.post.PsaveMapper;
import software_market.cache.MultiLevelCache;
import software_market.event.EmailEvent;
import software_market.service.MailService;
import software_market.service.UserService;
import software_market.vo.user.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final CdkeyMapper cdkeyMapper;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;
    private final RefreshTokenMapper refreshTokenMapper;
    private final MailService mailService;
    private final KafkaProducerService kafkaProducerService;
    private final PostMapper postMapper;
    private final ProductMapper productMapper;
    private final PlikeMapper plikeMapper;
    private final PsaveMapper psaveMapper;
    private final PbrowseMapper pbrowseMapper;
    private final RatingMapper ratingMapper;
    private final NoticeMapper noticeMapper;
    private final PcommentMapper pcommentMapper;
    private final CcommentMapper ccommentMapper;
    private final PclikeMapper pclikeMapper;
    private final PcdenyMapper pcdenyMapper;
    private final CclikeMapper cclikeMapper;
    private final CcdenyMapper ccdenyMapper;
    private final MultiLevelCache cache;

    private static final String USER_CACHE_KEY = "user:";
    private static final int MAX_VALIDATE_FAIL = 5;


    @Override
    public TokenPair register(RegisterDTO registerDTO) {
        // 1. 校验两次密码是否一致
        if (!registerDTO.getPassword()
                .equals(registerDTO.getPassword2())) {
            throw new BizException("两次密码不一致");
        }
        //2.校验邮箱
        String email = registerDTO.getEmail().trim();
        if(!email.contains("@")) {
            throw new BizException("邮箱格式不正确");
        }
        //3.检验邮箱唯一
        if (count(new QueryWrapper<User>().eq("email", email)) > 0) {
            throw new BizException("邮箱已注册");
        }
        //4.检验校验码
        String codeKey= RedisKeyConstant.VCODE+email;
        String code = stringRedisTemplate.opsForValue().get(codeKey);
        if(code==null||!code.equals(registerDTO.getValiCode())) {
            recordValidateFail(email);
            throw new BizException("验证码错误");
        }

        //5.校验CDkey
        String cdKey = registerDTO.getCdKey();
        if (!StringUtils.hasText(cdKey)) {
            throw new BizException("CDKey不能为空");
        }
        int update = cdkeyMapper.update(
                null,
                Wrappers.<Cdkey>lambdaUpdate()
                        .set(Cdkey::getStatus, 1)
                        .set(Cdkey::getUsedTime, LocalDateTime.now())
                        .eq(Cdkey::getContent, cdKey)
                        .eq(Cdkey::getStatus, 0)
        );
        if (update == 0) {
            throw new BizException("CDKey无效或已被使用");
        }
        // 6. 创建用户
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setName(StringUtils.hasText(registerDTO.getName()) ? registerDTO.getName() :
                RandomStringUtils.randomAlphabetic(10));
        user.setPhone(StringUtils.hasText(registerDTO.getPhone()) ?registerDTO.getPhone() : null);
        user.setIdentity("student");
        user.setDeleted(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return issueTokenPair(user);
    }

    @Override
    public void validateEmail(ValidateEmailDTO validateEmailDTO) {
        String email = validateEmailDTO.getEmail().trim();
        Integer mode = validateEmailDTO.getMode();

        // 1. 校验邮箱格式
        if (!email.contains("@")) {
            throw new BizException("邮箱格式不正确");
        }

        // 2. 查邮箱是否存在
        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, email)
        );

        // 3. mode 校验
        if (mode == 0 && count > 0) {
            throw new BizException("该邮箱已被注册");
        }

        if (mode == 1 && count == 0) {
            throw new BizException("该邮箱不存在");
        }

        // 4. 生成验证码
        String code = generateCode();

        // 5. 存入 Redis（5分钟）
        String key = RedisKeyConstant.VCODE + email;
        stringRedisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

        // 6. 通过 Kafka 异步发送邮件
        kafkaProducerService.sendEmailEvent(new EmailEvent(email, code));
    }

    @Override
    public TokenPair login(@RequestBody LoginDTO loginDTO) {
        String email = loginDTO.getEmail().trim();
        String password = loginDTO.getPassword();
        if (password.isEmpty()) {
            throw new BizException("密码不能为空");
        }
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, email)
        );
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException("密码错误");
        }
        return issueTokenPair(user);
    }

    @Override
    public void identityValidate(IdentityValidateDTO identityValidateDTO) {
        String valiCode = identityValidateDTO.getValiCode();
        String email = identityValidateDTO.getEmail().trim();
        String key = RedisKeyConstant.VCODE + email;
        if(!email.contains("@")){
            throw new BizException("邮箱格式不正确");
        }
        if(key==null){
            throw new BizException("验证码不存在");
        }
        String code = stringRedisTemplate.opsForValue().get(key);
        if(code==null||!code.equals(valiCode)){
            recordValidateFail(email);
            throw new BizException("验证码错误");
        }
        stringRedisTemplate.delete(key);
    }

    @Override
    public CurrentUserVO getCurrentUserInfo(Integer userId) {

        User user = userMapper.selectById(userId);

        if (user == null) {
            return null;
        }
        CurrentUserVO vo = new CurrentUserVO();
        vo.setUserId(user.getUserId());
        vo.setName(user.getName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setIntro(user.getIntro());
        vo.setScore(user.getScore());
        vo.setIdentity(user.getIdentity());
        vo.setBanend(user.getBanend());
        vo.setPunishNum(user.getPunishNum());
        vo.setEmailNotificationBlocked(user.getEmailNotificationBlocked());
        return vo;
    }

    @Override
    public TokenPair refresh(RefreshTokenDTO refreshTokenDTO) {
        Claims claims = jwtProvider.parseRefreshToken(refreshTokenDTO.getRefreshToken());
        RefreshToken refreshToken = refreshTokenMapper.selectByTokenAndNotUsed(refreshTokenDTO.getRefreshToken());
        if (refreshToken == null) {
            throw new BizException("refresh token无效");
        }
        if(LocalDateTime.now().isAfter(refreshToken.getExpiresAt())) {
            refreshToken.setIsUsed(true);
            refreshTokenMapper.updateById(refreshToken);
            throw new BizException("refresh token已过期");
        }
        Integer userId = Integer.parseInt(claims.get("userId", String.class));
        User user = userMapper.selectById(userId);
        if(user==null){
            throw new BizException("用户不存在");
        }
        return issueTokenPair(user);
    }

    @Override
    public void modifyPassword(ModifyPasswordDTO modifyPasswordDTO) {
        String email = modifyPasswordDTO.getEmail().trim();
        if(!email.contains("@")){
            throw new BizException("邮箱格式不正确");
        }
        String valiCode = modifyPasswordDTO.getValiCode();
        String key = RedisKeyConstant.VCODE + email;
        String code = stringRedisTemplate.opsForValue().get(key);
        if(code==null){
            throw new BizException("验证码已过期");
        }
        if(!valiCode.equals(code)){
            throw new BizException("验证码错误");
        }
        stringRedisTemplate.delete(key);
        String password = modifyPasswordDTO.getPassword();
        String password2 = modifyPasswordDTO.getPassword2();
        if(!password.equals(password2)){
            throw new BizException("两次密码不一致");
        }
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getEmail, email));
        if(user==null){
            throw new BizException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(password));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        refreshTokenMapper.delete(
                Wrappers.<RefreshToken>lambdaQuery()
                        .eq(RefreshToken::getUserId, user.getUserId())
        );
    }

    @Override
    public User getUserInfo(GetUserInfoDTO getUserInfoDTO) {
        String phone = getUserInfoDTO.getPhone();
        Integer userId = getUserInfoDTO.getUserId();
        if(phone==null&&userId==null){
            throw new BizException("参数错误");
        }
        if(phone!=null){
            return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone));
        }
        if(userId!=null){
            return userMapper.selectById(userId);
        }
        return null;
    }

    @Override
    public void updateUserInfo(UpdateUserInfoDTO dto) {
        Integer userId = UserContext.getUserId();

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatarURL() != null) {
            user.setAvatarUrl(dto.getAvatarURL());
        }
        if (dto.getIntro() != null) {
            user.setIntro(dto.getIntro());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        cache.evict(USER_CACHE_KEY + userId);
    }

    @Override
    public void changeEmailPush(ChangeEmailPushDTO dto) {

        Integer userId = UserContext.getUserId();

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        Boolean current = user.getEmailNotificationBlocked();
        user.setEmailNotificationBlocked(!current);

        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        cache.evict(USER_CACHE_KEY + userId);
    }

    @Override
    public AvatarVO getAvatar(GetAvatarDTO getAvatarDTO) {

        if (getAvatarDTO == null) {
            throw new BizException("参数不能为空");
        }
        String phone = getAvatarDTO.getPhone();
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BizException("手机号格式不正确");
        }
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .select(User::getPhone, User::getAvatarUrl)
                        .eq(User::getPhone, phone)
        );
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return new AvatarVO(phone, user.getAvatarUrl());
    }


    private static final String AVATAR_URL_PREFIX = "/avatar/";

    @Value("${app.upload-dir}")
    private String uploadDir;

    private String getAvatarUploadDir() {
        return new File(uploadDir, "avatar").getAbsoluteFile() + File.separator;
    }

    @Override
    public UploadAvatarVO uploadAvatar(UploadAvatarDTO uploadAvatarDTO) {
        MultipartFile file = uploadAvatarDTO.getFile();
        // 1. 文件为空
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        // 2. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BizException("只能上传图片");
        }

        // 3. 获取原文件名
        String originalFilename = file.getOriginalFilename();

        // 4. 获取后缀
        String suffix = "";

        if (originalFilename != null &&
                originalFilename.contains(".")) {

            suffix = originalFilename.substring(
                    originalFilename.lastIndexOf(".")
            );
        }
        // 5. 生成唯一文件名
        String filename = UUID.randomUUID() + suffix;
        // 6. 创建目录（用绝对路径）
        String uploadDir = getAvatarUploadDir();
        File dir = new File(uploadDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 7. 创建目标文件（绝对路径）
        File dest = new File(uploadDir + filename);
        try {
            // 8. 保存文件
            file.transferTo(dest);

        } catch (IOException e) {
            log.error("头像文件上传失败: {}", e.getMessage(), e);
            throw new BizException("文件上传失败");
        }
        // 9. 拼接访问路径
        String avatarUrl = AVATAR_URL_PREFIX + filename;

        // 10. 自动保存到当前用户记录
        Integer userId = UserContext.getUserId();
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setAvatarUrl(avatarUrl);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
                cache.evict(USER_CACHE_KEY + userId);
            }
        }

        // 11. 返回
        return new UploadAvatarVO(avatarUrl);
    }

    @Override
    public UpdateAvatarVO updateAvatar(UpdateAvatarDTO updateAvatarDTO) {

        MultipartFile file = updateAvatarDTO.getFile();
        String phone = updateAvatarDTO.getPhone();

        // 1️⃣ 校验手机号
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BizException("手机号格式不正确");
        }

        // 2️⃣ 校验文件
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BizException("只能上传图片");
        }

        // 3️⃣ 查询用户
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getPhone, phone)
        );
        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 4️⃣ 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + suffix;

        String uploadDir = getAvatarUploadDir();
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(uploadDir + filename);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("updateAvatar文件保存失败: {}", e.getMessage(), e);
            throw new BizException("文件保存失败");
        }

        // 6️⃣ 更新用户头像 URL
        String avatarUrl = AVATAR_URL_PREFIX + filename;
        user.setAvatarUrl(avatarUrl);

        int updated = userMapper.updateById(user);
        if (updated <= 0) {
            throw new BizException("更新用户头像失败");
        }
        cache.evict(USER_CACHE_KEY + user.getUserId());

        // 7️⃣ 返回 VO
        return new UpdateAvatarVO(avatarUrl, phone);
    }

    @Override
    public void deleteMe(DeleteMeDTO deleteMeDTO) {
        String vailCode = deleteMeDTO.getVailCode();
        String phone = deleteMeDTO.getPhone();
        String email = deleteMeDTO.getEmail();

        String key = RedisKeyConstant.VCODE + email;

        String code = stringRedisTemplate.opsForValue().get(key);

        if (code == null) {
            throw new BizException("验证码已过期");
        }
        if (!code.equals(vailCode)) {
            throw new BizException("验证码错误");
        }
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getPhone, phone)
                        .eq(User::getEmail, email)
        );
        if (user == null) {
            throw new BizException("用户不存在");
        }

        Integer userId = user.getUserId();

        // 级联删除用户的所有帖子及其关联数据
        List<Integer> postIds = postMapper.selectPostIdsByUserId(userId);

        if (!postIds.isEmpty()) {
            List<Integer> pcommentIds = pcommentMapper.selectObjs(
                    Wrappers.<Pcomment>lambdaQuery()
                            .select(Pcomment::getPcommentId)
                            .in(Pcomment::getPtargetId, postIds)
            ).stream().map(o -> (Integer) o).collect(Collectors.toList());

            List<Integer> ccommentIds = new ArrayList<>();
            if (!pcommentIds.isEmpty()) {
                ccommentIds = ccommentMapper.selectObjs(
                        Wrappers.<Ccomment>lambdaQuery()
                                .select(Ccomment::getCcommentId)
                                .in(Ccomment::getCtargetId, pcommentIds)
                ).stream().map(o -> (Integer) o).collect(Collectors.toList());
            }

            if (!ccommentIds.isEmpty()) {
                cclikeMapper.delete(Wrappers.<Cclike>lambdaQuery().in(Cclike::getCctargetId, ccommentIds));
                ccdenyMapper.delete(Wrappers.<Ccdeny>lambdaQuery().in(Ccdeny::getCctargetId, ccommentIds));
                ccommentMapper.deleteBatchIds(ccommentIds);
            }

            if (!pcommentIds.isEmpty()) {
                pclikeMapper.delete(Wrappers.<Pclike>lambdaQuery().in(Pclike::getPctargetId, pcommentIds));
                pcdenyMapper.delete(Wrappers.<Pcdeny>lambdaQuery().in(Pcdeny::getPctargetId, pcommentIds));
                pcommentMapper.deleteBatchIds(pcommentIds);
            }

            List<Integer> allTargetIds = new ArrayList<>();
            allTargetIds.addAll(postIds);
            allTargetIds.addAll(pcommentIds);
            allTargetIds.addAll(ccommentIds);
            noticeMapper.delete(Wrappers.<Notice>lambdaQuery().in(Notice::getTarget, allTargetIds));

            ratingMapper.delete(Wrappers.<Rating>lambdaQuery()
                    .eq(Rating::getItemType, "post")
                    .in(Rating::getItemId, postIds));

            plikeMapper.delete(Wrappers.<Plike>lambdaQuery().in(Plike::getPtargetId, postIds));
            psaveMapper.delete(Wrappers.<Psave>lambdaQuery().in(Psave::getPtargetId, postIds));
            pbrowseMapper.delete(Wrappers.<Pbrowse>lambdaQuery().in(Pbrowse::getPtargetId, postIds));

            postMapper.deleteBatchIds(postIds);
        }

        // 清理用户在其他帖子上的互动记录
        plikeMapper.deleteByUserId(userId);
        psaveMapper.deleteByUserId(userId);
        pbrowseMapper.deleteByUserId(userId);
        ratingMapper.deleteByUserId(userId);

        pcommentMapper.delete(Wrappers.<Pcomment>lambdaQuery().eq(Pcomment::getUserId, userId));
        ccommentMapper.delete(Wrappers.<Ccomment>lambdaQuery().eq(Ccomment::getUserId, userId));
        pclikeMapper.delete(Wrappers.<Pclike>lambdaQuery().eq(Pclike::getUserId, userId));
        pcdenyMapper.delete(Wrappers.<Pcdeny>lambdaQuery().eq(Pcdeny::getUserId, userId));
        cclikeMapper.delete(Wrappers.<Cclike>lambdaQuery().eq(Cclike::getUserId, userId));
        ccdenyMapper.delete(Wrappers.<Ccdeny>lambdaQuery().eq(Ccdeny::getUserId, userId));

        productMapper.hardDeleteByUserId(userId);
        refreshTokenMapper.deleteByUserId(userId);
        noticeMapper.deleteByUser(userId);

        int rows = userMapper.deleteById(userId);
        if (rows <= 0) {
            throw new BizException("注销失败");
        }
        stringRedisTemplate.delete(key);
    }

    @Override
    public UserStatisticsVO statistics() {

        // 1. 获取当前登录用户ID
        Integer userId = UserContext.getUserId();

        if (userId == null) {
            throw new BizException("用户未登录");
        }

        // 2. 查询用户
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 3. 创建 VO
        UserStatisticsVO vo = new UserStatisticsVO();

        // =========================
        // 用户基础信息
        // =========================
        vo.setName(user.getName());

        vo.setIntro(user.getIntro());

        vo.setScore(user.getScore());

        vo.setAvatarUrl(user.getAvatarUrl());

        // =========================
        // 注册时长
        // =========================
        if (user.getCreatedAt() != null) {

            Duration duration = Duration.between(
                    user.getCreatedAt(),
                    LocalDateTime.now()
            );

            vo.setDurationTime(duration);
        }

        // =========================
        // 下面这些暂时没做业务
        // 先默认 0
        // =========================
        //TODO 统计
        vo.setTotalPostCnt(0);
        vo.setTotalPostLikeNum(0);
        vo.setThisYearPostCnt(0);
        vo.setThisYearLikeNum(0);
        vo.setMaxLikeNum(0);
        vo.setMaxBrowseNum(0);
        vo.setMaxCommentNum(0);

        vo.setPCommentCnt(0);
        vo.setPCommentLikeNum(0);
        vo.setCcommentCnt(0);
        vo.setCcommentLikeNum(0);

        vo.setBeSavedCount(0);
        vo.setBeCommentedCount(0);
        vo.setRepliedCount(0);
        vo.setSavedCount(0);

        vo.setChatCount(0);

        return vo;
    }


    private String generateCode() {
        return String.valueOf((int)((Math.random() * 9 + 1) * 100000));
    }

    private TokenPair issueTokenPair(User user) {
        Integer userId = user.getUserId();
        String accessToken = jwtProvider.generateAccessToken(userId.toString());
        String refreshToken = jwtProvider.generateRefreshToken(userId.toString());
        // 入库记录 refresh token（一次性使用）
        RefreshToken record = new RefreshToken();
        record.setUserId(user.getUserId());
        record.setToken(refreshToken);
        record.setExpiresAt(LocalDateTime.now().plusDays(30));
        record.setIsUsed(false);
        record.setCreatedAt(LocalDateTime.now());
        refreshTokenMapper.insert(record);
        return new TokenPair(accessToken, refreshToken);
    }

    private void recordValidateFail(String email) {
        String failKey = RedisKeyConstant.VCODE_FAIL + email;
        Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
        stringRedisTemplate.expire(failKey, 5, TimeUnit.MINUTES);
        if (failCount != null && failCount >= MAX_VALIDATE_FAIL) {
            stringRedisTemplate.delete(RedisKeyConstant.VCODE + email);
        }
    }
}
