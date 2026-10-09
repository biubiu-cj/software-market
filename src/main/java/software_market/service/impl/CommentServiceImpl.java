package software_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software_market.config.security.UserContext;
import software_market.dto.request.comment.*;
import software_market.dto.request.rating.SubmitRatingDTO;
import software_market.entity.User;
import software_market.entity.comment.*;
import software_market.entity.post.Post;
import software_market.event.NotificationEvent;
import software_market.exception.BizException;
import software_market.mapper.NoticeMapper;
import software_market.mapper.UserMapper;
import software_market.mapper.comment.*;
import software_market.mapper.post.PostMapper;
import software_market.service.CommentService;
import software_market.service.RatingService;
import software_market.vo.comment.CommentResponseVO;
import software_market.vo.comment.RCommentResponseVO;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CcommentMapper ccommentMapper;
    private final PcommentMapper pcommentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final RatingService ratingService;
    private final PclikeMapper pclikeMapper;
    private final PcdenyMapper pcdenyMapper;
    private final CclikeMapper cclikeMapper;
    private final CcdenyMapper ccdenyMapper;
    private final NoticeMapper noticeMapper;
    private final KafkaProducerService kafkaProducerService;
    @Override
    public List<CommentResponseVO> showPcomments(ShowPcommentsDTO dto) {
        Integer postID = dto.getPostID();
        Post post = postMapper.selectById(postID);
        if (post == null) {
            throw new BizException("帖子不存在");
        }

        Integer currentUserId = UserContext.getUserId();

        // 查询一级评论
        List<Pcomment> parentComments = pcommentMapper.selectByPostId(postID);

        // 批量查当前用户对一级评论的点赞/点踩状态
        List<Integer> pcommentIds = parentComments.stream().map(Pcomment::getPcommentId).toList();
        boolean hasPcommentIds = !pcommentIds.isEmpty() && currentUserId != null;
        Set<Integer> likedPcommentIds = hasPcommentIds
                ? pclikeMapper.selectList(
                        Wrappers.<Pclike>lambdaQuery()
                                .eq(Pclike::getUserId, currentUserId)
                                .in(Pclike::getPctargetId, pcommentIds))
                        .stream().map(Pclike::getPctargetId).collect(Collectors.toSet())
                : Set.of();
        Set<Integer> deniedPcommentIds = hasPcommentIds
                ? pcdenyMapper.selectList(
                        Wrappers.<Pcdeny>lambdaQuery()
                                .eq(Pcdeny::getUserId, currentUserId)
                                .in(Pcdeny::getPctargetId, pcommentIds))
                        .stream().map(Pcdeny::getPctargetId).collect(Collectors.toSet())
                : Set.of();

        // 收集所有二级评论ID，用于批量查点赞/点踩
        List<Integer> allCcommentIds = new ArrayList<>();
        for (Pcomment parent : parentComments) {
            List<Ccomment> children = ccommentMapper.selectPcId(parent.getPcommentId());
            for (Ccomment child : children) {
                allCcommentIds.add(child.getCcommentId());
            }
        }
        boolean hasCcommentIds = !allCcommentIds.isEmpty() && currentUserId != null;
        Set<Integer> likedCcommentIds = hasCcommentIds
                ? cclikeMapper.selectList(
                        Wrappers.<Cclike>lambdaQuery()
                                .eq(Cclike::getUserId, currentUserId)
                                .in(Cclike::getCctargetId, allCcommentIds))
                        .stream().map(Cclike::getCctargetId).collect(Collectors.toSet())
                : Set.of();
        Set<Integer> deniedCcommentIds = hasCcommentIds
                ? ccdenyMapper.selectList(
                        Wrappers.<Ccdeny>lambdaQuery()
                                .eq(Ccdeny::getUserId, currentUserId)
                                .in(Ccdeny::getCctargetId, allCcommentIds))
                        .stream().map(Ccdeny::getCctargetId).collect(Collectors.toSet())
                : Set.of();

        List<CommentResponseVO> vos = new ArrayList<>();
        for (Pcomment parent : parentComments) {
            CommentResponseVO vo = new CommentResponseVO();
            vo.setCommentId(parent.getPcommentId());
            Integer parentUserId = parent.getUserId();
            User parentUser = userMapper.selectById(parentUserId);
            vo.setUserId(parentUserId);
            vo.setUsername(parentUser.getName());
            vo.setContent(parent.getPctext());
            vo.setCreateTime(parent.getTime());
            vo.setLikeNum(parent.getLikeNum());
            vo.setDenyNum(parent.getDenyNum());
            vo.setIsLiked(likedPcommentIds.contains(parent.getPcommentId()));
            vo.setIsDenied(deniedPcommentIds.contains(parent.getPcommentId()));

            // 查询该一级评论下面的二级评论
            List<Ccomment> childComments = ccommentMapper.selectPcId(parent.getPcommentId());
            List<RCommentResponseVO> children = childComments.stream().map(child -> {
                RCommentResponseVO childVO = new RCommentResponseVO();
                childVO.setCommentId(child.getCcommentId());
                Integer childUserId = child.getUserId();
                User childUser = userMapper.selectById(childUserId);
                childVO.setUserId(childUserId);
                childVO.setUsername(childUser.getName());
                childVO.setReplyUsername(child.getUsertargetName());
                childVO.setContent(child.getCctext());
                childVO.setCreateTime(child.getTime());
                childVO.setLikeNum(child.getLikeNum());
                childVO.setDenyNum(child.getDenyNum());
                childVO.setIsLiked(likedCcommentIds.contains(child.getCcommentId()));
                childVO.setIsDenied(deniedCcommentIds.contains(child.getCcommentId()));
                return childVO;
            }).toList();
            vo.setChildren(children);
            vos.add(vo);
        }
        return vos;
    }

    @Transactional
    @Override
    public void postPcomment(PostCommentDTO dto) {
        Integer postID = dto.getPostID();
        Post post = postMapper.selectById(postID);
        if (post == null) {
            throw new BizException("帖子不存在");
        }

        Integer userId = UserContext.getUserId();
        String content = dto.getContent();
        if (pcommentMapper.countDuplicate(userId, postID, content) > 0) {
            throw new BizException("请勿重复评论");
        }

        Pcomment pcomment = new Pcomment();
        pcomment.setPtargetId(postID);
        pcomment.setPctext(content);
        pcomment.setTime(LocalDateTime.now());
        pcomment.setUserId(userId);
        pcommentMapper.insert(pcomment);
        post.setCommentNum(post.getCommentNum() + 1);
        post.setHeat(post.getHeat() + 6);
        postMapper.updateById(post);
        // 有评分则提交评分
        if (dto.getRating() != null && dto.getRating() > 0) {
            SubmitRatingDTO ratingDTO = new SubmitRatingDTO();
            ratingDTO.setPostID(postID);
            ratingDTO.setRating(dto.getRating());
            ratingService.submitRating(ratingDTO);
        }

        // 通过 Kafka 异步通知帖子作者（自己评论自己帖子不通知）
        if (!userId.equals(post.getUserId())) {
            String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
            NotificationEvent event = new NotificationEvent(
                    post.getUserId(), userId, "pcomment",
                    "评论了你的帖子: " + preview, postID);
            kafkaProducerService.sendNotificationEvent(event);
        }
    }

    @Transactional
    @Override
    public void postCcomment(PostCcommentDTO dto) {
        Integer pcommentID = dto.getPcommentID();
        Integer postID = dto.getPostID();
        Integer replyCcommentID = dto.getCcommentID();

        Post post = postMapper.selectById(postID);
        if (post == null) {
            throw new BizException("帖子不存在");
        }

        Pcomment pcomment = pcommentMapper.selectById(pcommentID);
        if (pcomment == null) {
            throw new BizException("一级评论不存在");
        }

        Integer userId = UserContext.getUserId();
        String content = dto.getContent();
        if (ccommentMapper.countDuplicate(userId, pcommentID, content) > 0) {
            throw new BizException("请勿重复评论");
        }

        Ccomment ccomment = new Ccomment();
        ccomment.setUserId(userId);
        ccomment.setCctext(content);
        ccomment.setTime(LocalDateTime.now());
        ccomment.setCtargetId(pcommentID);

        // 回复一级评论
        if (replyCcommentID == null) {
            User targetUser = userMapper.selectById(pcomment.getUserId());
            ccomment.setUsertargetName(targetUser.getName());
        }
        // 回复二级评论
        else {
            Ccomment replyComment = ccommentMapper.selectById(replyCcommentID);
            if (replyComment == null) {
                throw new BizException("被回复评论不存在");
            }
            User targetUser = userMapper.selectById(replyComment.getUserId());
            ccomment.setUsertargetName(targetUser.getName());
        }
        ccommentMapper.insert(ccomment);
        post.setCommentNum(post.getCommentNum() + 1);
        post.setHeat(post.getHeat() + 6);
        postMapper.updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePcomment(PcommentDTO deletePcommentDTO) {
        Integer pcommentID = deletePcommentDTO.getPcommentID();
        Pcomment pcomment = pcommentMapper.selectById(pcommentID);
        if (pcomment == null) {
            throw new BizException("一级评论不存在");
        }
        Integer userId = UserContext.getUserId();
        if (!pcomment.getUserId().equals(userId)) {
            throw new BizException("无权删除该评论");
        }

        // 查子评论ID列表
        List<Integer> ccommentIds = ccommentMapper.selectList(
                Wrappers.<Ccomment>lambdaQuery()
                        .select(Ccomment::getCcommentId)
                        .eq(Ccomment::getCtargetId, pcommentID))
                .stream().map(Ccomment::getCcommentId).toList();
        int childCount = ccommentIds.size();

        // 更新帖子评论数和热度
        Post post = postMapper.selectById(pcomment.getPtargetId());
        if (post != null) {
            post.setCommentNum(Math.max(post.getCommentNum() - childCount - 1, 0));
            long days = ChronoUnit.DAYS.between(pcomment.getTime(), LocalDateTime.now());
            double heatDeduction;
            if (days > 0) {
                double deleteHeat = Math.pow(6, Math.pow(0.5, days));
                heatDeduction = deleteHeat + childCount;
            } else {
                heatDeduction = 6 + childCount * 6;
            }
            post.setHeat(Math.max(post.getHeat() - heatDeduction, 0));
            postMapper.updateById(post);
        }

        // 删子评论的点赞/点踩 → 删子评论
        if (!ccommentIds.isEmpty()) {
            cclikeMapper.delete(Wrappers.<Cclike>lambdaQuery().in(Cclike::getCctargetId, ccommentIds));
            ccdenyMapper.delete(Wrappers.<Ccdeny>lambdaQuery().in(Ccdeny::getCctargetId, ccommentIds));
            ccommentMapper.delete(Wrappers.<Ccomment>lambdaQuery().in(Ccomment::getCcommentId, ccommentIds));
        }

        // 删一级评论的点赞/点踩 → 删一级评论
        pclikeMapper.delete(Wrappers.<Pclike>lambdaQuery().eq(Pclike::getPctargetId, pcommentID));
        pcdenyMapper.delete(Wrappers.<Pcdeny>lambdaQuery().eq(Pcdeny::getPctargetId, pcommentID));
        pcommentMapper.deleteById(pcommentID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCcomment(CcommentDTO deleteCcommentDTO) {
        Integer ccommentID = deleteCcommentDTO.getCcommentID();
        Ccomment ccomment = ccommentMapper.selectById(ccommentID);
        if (ccomment == null) {
            throw new BizException("二级评论不存在");
        }
        Integer userId = UserContext.getUserId();
        if (!ccomment.getUserId().equals(userId)) {
            throw new BizException("无权删除该评论");
        }
        Pcomment pcomment = pcommentMapper.selectById(ccomment.getCtargetId());
        if (pcomment == null) {
            throw new BizException("一级评论不存在");
        }
        Post post = postMapper.selectById(pcomment.getPtargetId());
        if (post != null) {
            post.setCommentNum(Math.max(post.getCommentNum() - 1, 0));
            long days = ChronoUnit.DAYS.between(ccomment.getTime(), LocalDateTime.now());
            double decreaseHeat;
            if (days == 0) {
                decreaseHeat = 6;
            } else {
                decreaseHeat = Math.pow(6, Math.pow(0.5, days));
            }
            post.setHeat(Math.max(post.getHeat() - decreaseHeat, 0));
            postMapper.updateById(post);
        }

        // 删二级评论的点赞/点踩 → 删二级评论
        cclikeMapper.delete(Wrappers.<Cclike>lambdaQuery().eq(Cclike::getCctargetId, ccommentID));
        ccdenyMapper.delete(Wrappers.<Ccdeny>lambdaQuery().eq(Ccdeny::getCctargetId, ccommentID));
        ccommentMapper.deleteById(ccommentID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePcommentLike(PcommentDTO updatePcommentLikeDTO) {
        Integer pcommentID = updatePcommentLikeDTO.getPcommentID();
        Pcomment pcomment = pcommentMapper.selectById(pcommentID);
        if (pcomment == null) {
            throw  new BizException("一级评论不存在");
        }
        Integer userId = UserContext.getUserId();
        LambdaQueryWrapper<Pclike> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Pclike::getUserId, userId).eq(Pclike::getPctargetId, pcommentID);
        Pclike oldLike = pclikeMapper.selectOne(wrapper);
        if(oldLike!=null){
            pclikeMapper.deleteById(oldLike.getPclikeId());
            pcomment.setLikeNum(Math.max(pcomment.getLikeNum()-1,0));
            pcommentMapper.updateById(pcomment);
            return;
        }
        Pclike pclike = new Pclike();
        pclike.setUserId(userId);
        pclike.setPctargetId(pcommentID);
        pclikeMapper.insert(pclike);
        pcomment.setLikeNum(pcomment.getLikeNum()+1);
        pcommentMapper.updateById(pcomment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePcommentDeny(PcommentDTO updatePcommentDenyDTO) {
        Integer pcommentID = updatePcommentDenyDTO.getPcommentID();
        Pcomment pcomment = pcommentMapper.selectById(pcommentID);
        if (pcomment == null) {
            throw  new BizException("一级评论不存在");
        }
        Integer userId = UserContext.getUserId();
        LambdaQueryWrapper<Pcdeny> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Pcdeny::getUserId, userId).eq(Pcdeny::getPctargetId, pcommentID);
        Pcdeny oldDeny = pcdenyMapper.selectOne(wrapper);
        if(oldDeny!=null){
            pcdenyMapper.deleteById(oldDeny.getPcdenyId());
            pcomment.setDenyNum(Math.max(pcomment.getDenyNum()-1,0));
            pcommentMapper.updateById(pcomment);
            return;
        }
        Pcdeny pcdeny = new Pcdeny();
        pcdeny.setUserId(userId);
        pcdeny.setPctargetId(pcommentID);
        pcdenyMapper.insert(pcdeny);
        pcomment.setDenyNum(pcomment.getDenyNum()+1);
        pcommentMapper.updateById(pcomment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCcommentLike(CcommentDTO updateCcommentLikeDTO) {
        Integer ccommentID = updateCcommentLikeDTO.getCcommentID();
        Ccomment ccomment = ccommentMapper.selectById(ccommentID);
        if(ccomment==null){
            throw new BizException("二级评论不存在");
        }
        Integer userId = UserContext.getUserId();
        LambdaQueryWrapper<Cclike> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Cclike::getUserId, userId).eq(Cclike::getCctargetId, ccommentID);
        Cclike oldLike = cclikeMapper.selectOne(wrapper);
        if (oldLike != null) {
            cclikeMapper.deleteById(oldLike.getCclikeId());
            ccomment.setLikeNum(Math.max(ccomment.getLikeNum() - 1, 0));
            ccommentMapper.updateById(ccomment);
            return;
        }
        Cclike cclike = new Cclike();
        cclike.setUserId(userId);
        cclike.setCctargetId(ccommentID);
        cclikeMapper.insert(cclike);
        ccomment.setLikeNum(ccomment.getLikeNum() + 1);
        ccommentMapper.updateById(ccomment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCcommentDeny(CcommentDTO updateCcommentDenyDTO) {
        Integer ccommentID = updateCcommentDenyDTO.getCcommentID();
        Ccomment ccomment = ccommentMapper.selectById(ccommentID);
        if (ccomment == null) {
            throw new BizException("二级评论不存在");
        }
        Integer userId = UserContext.getUserId();
        LambdaQueryWrapper<Ccdeny> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Ccdeny::getUserId, userId).eq(Ccdeny::getCctargetId, ccommentID);
        Ccdeny oldDeny = ccdenyMapper.selectOne(wrapper);
        if (oldDeny != null) {
            ccdenyMapper.deleteById(oldDeny.getCcdenyId());
            ccomment.setDenyNum(Math.max(ccomment.getDenyNum() - 1, 0));
            ccommentMapper.updateById(ccomment);
            return;
        }
        Ccdeny ccdeny = new Ccdeny();
        ccdeny.setUserId(userId);
        ccdeny.setCctargetId(ccommentID);
        ccdenyMapper.insert(ccdeny);
        ccomment.setDenyNum(ccomment.getDenyNum() + 1);
        ccommentMapper.updateById(ccomment);
    }

}
