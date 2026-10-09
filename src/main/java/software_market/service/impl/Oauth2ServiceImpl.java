package software_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software_market.config.security.JwtProvider;
import software_market.config.security.UserContext;
import software_market.dto.request.oauth2.AuthorizeDTO;
import software_market.dto.request.oauth2.CheckAppDTO;
import software_market.entity.Oauth2App;
import software_market.entity.Oauth2Code;
import software_market.entity.Oauth2Record;
import software_market.entity.User;
import software_market.exception.BizException;
import software_market.mapper.*;
import software_market.service.Oauth2Service;
import software_market.vo.oauth2.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class Oauth2ServiceImpl extends ServiceImpl<Oauth2CodeMapper, Oauth2Code> implements Oauth2Service {

    private final Oauth2AppMapper oauth2AppMapper;
    private final Oauth2CodeMapper oauth2CodeMapper;
    private final Oauth2RecordMapper oauth2RecordMapper;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;

    @Override
    public Oauth2AppVO checkApp(CheckAppDTO dto) {
        Oauth2App app = oauth2AppMapper.selectById(dto.getAppId());
        if (app == null) {
            throw new BizException("应用不存在");
        }
        return Oauth2AppVO.builder()
                .appId(app.getAppId())
                .appName(app.getAppName())
                .appIcon(app.getAppIcon())
                .description(app.getDescription())
                .build();
    }

    @Override
    public AuthorizeVO authorize(AuthorizeDTO dto) {
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException("用户未登录");
        }

        Oauth2App app = oauth2AppMapper.selectById(dto.getAppId());
        if (app == null) {
            throw new BizException("应用不存在");
        }

        if (dto.getRedirectUri() == null || !dto.getRedirectUri().startsWith(app.getRedirectUri())) {
            throw new BizException("redirect_uri 不匹配");
        }

        if (!"code".equals(dto.getResponseType())) {
            throw new BizException("不支持的 response_type");
        }

        String code = UUID.randomUUID().toString();

        Oauth2Code oauth2Code = new Oauth2Code();
        oauth2Code.setCode(code);
        oauth2Code.setAppId(dto.getAppId());
        oauth2Code.setUserId(userId);
        oauth2Code.setScope(dto.getScope() != null ? dto.getScope() : "read_basic");
        oauth2Code.setRedirectUri(dto.getRedirectUri());
        oauth2Code.setState(dto.getState());
        oauth2Code.setIsUsed(false);
        oauth2Code.setCreatedAt(LocalDateTime.now());
        oauth2Code.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        oauth2CodeMapper.insert(oauth2Code);

        // Upsert authorization record
        LambdaQueryWrapper<Oauth2Record> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(Oauth2Record::getUserId, userId)
                .eq(Oauth2Record::getAppId, dto.getAppId());
        Oauth2Record existingRecord = oauth2RecordMapper.selectOne(recordWrapper);
        if (existingRecord != null) {
            existingRecord.setScope(oauth2Code.getScope());
            existingRecord.setCreatedAt(LocalDateTime.now());
            oauth2RecordMapper.updateById(existingRecord);
        } else {
            Oauth2Record record = new Oauth2Record();
            record.setUserId(userId);
            record.setAppId(dto.getAppId());
            record.setScope(oauth2Code.getScope());
            record.setCreatedAt(LocalDateTime.now());
            oauth2RecordMapper.insert(record);
        }

        String redirectUrl = dto.getRedirectUri() + "?code=" + code;
        if (dto.getState() != null && !dto.getState().isEmpty()) {
            redirectUrl += "&state=" + dto.getState();
        }

        return AuthorizeVO.builder().redirectUrl(redirectUrl).build();
    }

    @Override
    public TokenVO exchangeToken(String code, String appId, String appSecret) {
        Oauth2App app = oauth2AppMapper.selectById(appId);
        if (app == null || !app.getAppSecret().equals(appSecret)) {
            throw new BizException("app_id 或 app_secret 错误");
        }

        Oauth2Code oauth2Code = oauth2CodeMapper.selectByCodeAndNotUsed(code);
        if (oauth2Code == null) {
            throw new BizException("授权码无效或已使用");
        }

        if (LocalDateTime.now().isAfter(oauth2Code.getExpiresAt())) {
            throw new BizException("授权码已过期");
        }

        if (!oauth2Code.getAppId().equals(appId)) {
            throw new BizException("授权码与 app_id 不匹配");
        }

        oauth2Code.setIsUsed(true);
        oauth2CodeMapper.updateById(oauth2Code);

        String accessToken = jwtProvider.generateOAuth2AccessToken(
                oauth2Code.getUserId(),
                oauth2Code.getAppId(),
                oauth2Code.getScope()
        );

        return TokenVO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .scope(oauth2Code.getScope())
                .build();
    }

    @Override
    public Oauth2UserInfoVO userInfo(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return Oauth2UserInfoVO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .intro(user.getIntro())
                .identity(user.getIdentity())
                .build();
    }

    @Override
    public RecordsPageVO records(Integer userId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Oauth2Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Oauth2Record::getUserId, userId)
                .orderByDesc(Oauth2Record::getCreatedAt);

        Page<Oauth2Record> pageObj = new Page<>(page, pageSize);
        Page<Oauth2Record> result = oauth2RecordMapper.selectPage(pageObj, wrapper);

        List<String> appIds = result.getRecords().stream()
                .map(Oauth2Record::getAppId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Oauth2App> appMap = oauth2AppMapper.selectBatchIds(appIds).stream()
                .collect(Collectors.toMap(Oauth2App::getAppId, a -> a));

        List<Oauth2RecordVO> recordVOs = result.getRecords().stream()
                .map(r -> {
                    Oauth2App app = appMap.get(r.getAppId());
                    return Oauth2RecordVO.builder()
                            .appId(r.getAppId())
                            .appName(app != null ? app.getAppName() : r.getAppId())
                            .appIcon(app != null ? app.getAppIcon() : "")
                            .scope(r.getScope())
                            .createdAt(r.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return RecordsPageVO.builder()
                .records(recordVOs)
                .total(result.getTotal())
                .page(page)
                .pageSize(pageSize)
                .totalPages(result.getPages())
                .build();
    }
}
