package software_market.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software_market.config.security.JwtProvider;
import software_market.config.security.UserContext;
import software_market.dto.request.admin.*;
import software_market.entity.User;
import software_market.entity.comment.Ccomment;
import software_market.entity.comment.Pcomment;
import software_market.entity.post.Post;
import software_market.entity.post.Sue;
import software_market.event.RagIndexEventPublisher;
import software_market.exception.BizException;
import software_market.mapper.UserMapper;
import software_market.mapper.comment.CcommentMapper;
import software_market.mapper.comment.PcommentMapper;
import software_market.mapper.post.PostMapper;
import software_market.mapper.post.SueMapper;
import software_market.service.AdminService;
import software_market.vo.admin.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final SueMapper sueMapper;
    private final PostMapper postMapper;
    private final PcommentMapper pcommentMapper;
    private final CcommentMapper ccommentMapper;
    private final RagIndexEventPublisher ragIndexEventPublisher;

    @Override
    public Map<String, String> login(AdminLoginDTO dto) {
        String account = dto.getAccount().trim();
        String password = dto.getPassword();

        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, account)
        );

        if (user == null) {
            throw new BizException("账号或密码错误");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException("账号或密码错误");
        }

        if (!"admin".equals(user.getIdentity()) && !"official".equals(user.getIdentity())) {
            throw new BizException("无管理员权限");
        }

        String token = jwtProvider.generateAccessToken(String.valueOf(user.getUserId()));
        return Map.of("token", token);
    }

    @Override
    public AdminInfoVO getAdminInfo(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("管理员不存在");
        }

        if (!"admin".equals(user.getIdentity()) && !"official".equals(user.getIdentity())) {
            throw new BizException("无管理员权限");
        }

        AdminVO adminVO = new AdminVO();
        adminVO.setAccount(user.getEmail());
        adminVO.setName(user.getName());
        adminVO.setAvatarUrl(user.getAvatarUrl());
        adminVO.setIdentity(user.getIdentity());

        AdminInfoVO vo = new AdminInfoVO();
        vo.setAdmin(adminVO);
        return vo;
    }

    @Override
    public AddAdminVO addAdmin(AddAdminDTO dto) {
        Integer callerId = UserContext.getUserId();
        User caller = userMapper.selectById(callerId);
        if (caller == null || (!"admin".equals(caller.getIdentity()) && !"official".equals(caller.getIdentity()))) {
            throw new BizException("无管理员权限");
        }

        String account = dto.getAccount().trim();
        String password1 = dto.getPassword1();
        String password2 = dto.getPassword2();

        if (!password1.equals(password2)) {
            throw new BizException("两次密码不一致");
        }

        if (!account.contains("@")) {
            throw new BizException("账号必须为邮箱格式");
        }

        Long count = userMapper.selectCount(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, account)
        );
        if (count > 0) {
            throw new BizException("账号已存在");
        }

        User user = new User();
        user.setEmail(account);
        user.setPassword(passwordEncoder.encode(password1));
        user.setName(account.substring(0, account.indexOf("@")));
        user.setIdentity("admin");
        user.setEmailNotificationBlocked(true);
        user.setDeleted(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        return new AddAdminVO(user.getUserId(), user.getEmail());
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        String account = dto.getAccount().trim();
        String password1 = dto.getPassword1();
        String password2 = dto.getPassword2();

        if (!password1.equals(password2)) {
            throw new BizException("两次密码不一致");
        }

        if (password1.length() < 6) {
            throw new BizException("密码不能少于6位");
        }

        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, account)
        );

        if (user == null) {
            throw new BizException("账号不存在");
        }

        if (!"admin".equals(user.getIdentity()) && !"official".equals(user.getIdentity())) {
            throw new BizException("目标账号不是管理员");
        }

        user.setPassword(passwordEncoder.encode(password1));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updateAdminInfo(UpdateAdminInfoDTO dto) {
        String account = dto.getAccount().trim();

        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, account)
        );

        if (user == null) {
            throw new BizException("账号不存在");
        }

        if (!"admin".equals(user.getIdentity()) && !"official".equals(user.getIdentity())) {
            throw new BizException("目标账号不是管理员");
        }

        boolean changed = false;

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName().trim());
            changed = true;
        }

        if (dto.getPassword1() != null && !dto.getPassword1().isBlank()) {
            if (dto.getPassword2() == null || !dto.getPassword1().equals(dto.getPassword2())) {
                throw new BizException("两次密码不一致");
            }
            if (dto.getPassword1().length() < 6) {
                throw new BizException("密码不能少于6位");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword1()));
            changed = true;
        }

        if (!changed) {
            throw new BizException("没有需要修改的信息");
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(DeleteUserDTO dto) {
        String name = dto.getName().trim();

        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getName, name)
        );

        if (user == null) {
            throw new BizException("用户不存在");
        }

        if ("admin".equals(user.getIdentity()) || "official".equals(user.getIdentity())) {
            throw new BizException("不能通过此接口删除管理员，请使用删除管理员接口");
        }

        user.setDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void deleteAdmin(DeleteAdminDTO dto) {
        String account = dto.getAccount().trim();

        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getEmail, account)
        );

        if (user == null) {
            throw new BizException("管理员不存在");
        }

        if (!"admin".equals(user.getIdentity()) && !"official".equals(user.getIdentity())) {
            throw new BizException("目标账号不是管理员");
        }

        user.setIdentity("student");
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public List<UserVO> showUsers(ShowUsersDTO dto) {
        var wrapper = Wrappers.<User>lambdaQuery();

        if (dto.getName() != null && !dto.getName().isBlank()) {
            wrapper.like(User::getName, dto.getName().trim());
        }

        if (dto.getIdPass() != null && dto.getIdPass() != -1) {
            if (dto.getIdPass() == 1) {
                wrapper.isNotNull(User::getPhone).ne(User::getPhone, "");
            } else if (dto.getIdPass() == 0) {
                wrapper.and(w -> w.isNull(User::getPhone).or().eq(User::getPhone, ""));
            }
        }

        List<User> users = userMapper.selectList(wrapper);

        List<UserVO> result = new ArrayList<>();
        for (User user : users) {
            UserVO vo = new UserVO();
            vo.setUserId(user.getUserId());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setName(user.getName());
            vo.setIntro(user.getIntro());
            vo.setAvatarUrl(user.getAvatarUrl());
            vo.setIdentity(user.getIdentity());
            vo.setScore(user.getScore());
            vo.setPunishNum(user.getPunishNum());
            vo.setBanend(user.getBanend());
            vo.setCreatedAt(user.getCreatedAt());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<SueVO> getSues() {
        List<Sue> sues = sueMapper.selectList(
                Wrappers.<Sue>lambdaQuery()
                        .eq(Sue::getStatus, "wait")
                        .orderByDesc(Sue::getTime)
        );

        List<SueVO> result = new ArrayList<>();
        for (Sue sue : sues) {
            SueVO vo = new SueVO();
            vo.setSueId(sue.getSueId());
            vo.setTargettype(sue.getTargettype());
            vo.setTargetId(sue.getTargetId());
            vo.setUserId(sue.getUserId());
            vo.setReason(sue.getReason());
            vo.setTime(sue.getTime());
            vo.setStatus(sue.getStatus());
            vo.setFinish(sue.getFinish());

            User reporter = userMapper.selectById(sue.getUserId());
            if (reporter != null) {
                vo.setReporterName(reporter.getName());
            }

            if ("post".equals(sue.getTargettype())) {
                Post post = postMapper.selectById(sue.getTargetId());
                if (post != null) {
                    vo.setTargetTitle(post.getTitle());
                }
            } else if ("pcomment".equals(sue.getTargettype())) {
                Pcomment pcomment = pcommentMapper.selectById(sue.getTargetId());
                if (pcomment != null) {
                    vo.setTargetTitle(pcomment.getPctext());
                }
            } else if ("ccomment".equals(sue.getTargettype())) {
                Ccomment ccomment = ccommentMapper.selectById(sue.getTargetId());
                if (ccomment != null) {
                    vo.setTargetTitle(ccomment.getCctext());
                }
            }

            result.add(vo);
        }
        return result;
    }

    @Override
    public void noViolation(SueActionDTO dto) {
        Sue sue = sueMapper.selectById(dto.getSueID());
        if (sue == null) {
            throw new BizException("举报不存在");
        }
        sue.setStatus("nosin");
        sue.setFinish(true);
        sueMapper.updateById(sue);
    }

    @Override
    public void violation(SueActionDTO dto) {
        Sue sue = sueMapper.selectById(dto.getSueID());
        if (sue == null) {
            throw new BizException("举报不存在");
        }

        sue.setStatus("ok");
        sue.setFinish(true);
        sueMapper.updateById(sue);

        Integer offenderId = null;
        if ("post".equals(sue.getTargettype())) {
            Post post = postMapper.selectById(sue.getTargetId());
            if (post != null) {
                offenderId = post.getUserId();
            }
        } else if ("pcomment".equals(sue.getTargettype())) {
            Pcomment pcomment = pcommentMapper.selectById(sue.getTargetId());
            if (pcomment != null) {
                offenderId = pcomment.getUserId();
            }
        } else if ("ccomment".equals(sue.getTargettype())) {
            Ccomment ccomment = ccommentMapper.selectById(sue.getTargetId());
            if (ccomment != null) {
                offenderId = ccomment.getUserId();
            }
        }

        if (offenderId != null) {
            User offender = userMapper.selectById(offenderId);
            if (offender != null) {
                int newCount = (offender.getPunishNum() == null ? 0 : offender.getPunishNum()) + 1;
                offender.setPunishNum(newCount);
                offender.setUpdatedAt(LocalDateTime.now());

                if (newCount >= 3) {
                    offender.setBanend(LocalDateTime.now().plusDays(7));
                }

                userMapper.updateById(offender);
            }
        }
    }

    @Override
    public void adminPost(AdminPostDTO dto) {
        Integer userId = UserContext.getUserId();

        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle().trim());
        post.setPtext(dto.getContent().trim());
        post.setCategory(dto.getPartition().trim());
        post.setPhotos(dto.getPhotos() != null ? dto.getPhotos().trim() : null);
        post.setTag(dto.getTagList() != null ? dto.getTagList().trim() : null);
        post.setPostTime(LocalDateTime.now());
        post.setCommentNum(0);
        post.setLikeNum(0);
        post.setBrowseNum(0);
        post.setHeat(0.0);
        postMapper.insert(post);
        ragIndexEventPublisher.reindex(post.getPostId());
    }
}
