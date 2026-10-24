package software_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software_market.config.security.UserContext;
import software_market.dto.request.post.*;
import software_market.dto.request.rating.RatingDTO;
import software_market.dto.request.post.UpdateBrowseNumDTO;
import software_market.entity.post.*;
import software_market.entity.Notice;
import software_market.event.NotificationEvent;
import software_market.event.RagIndexEventPublisher;
import software_market.entity.User;
import software_market.entity.comment.Ccomment;
import software_market.entity.comment.Ccdeny;
import software_market.entity.comment.Cclike;
import software_market.entity.comment.Pcdeny;
import software_market.entity.comment.Pclike;
import software_market.entity.comment.Pcomment;
import software_market.exception.BizException;
import software_market.mapper.post.*;
import software_market.mapper.NoticeMapper;
import software_market.mapper.RatingMapper;
import software_market.mapper.comment.CcommentMapper;
import software_market.mapper.comment.CcdenyMapper;
import software_market.mapper.comment.CclikeMapper;
import software_market.mapper.comment.PcdenyMapper;
import software_market.mapper.comment.PclikeMapper;
import software_market.mapper.comment.PcommentMapper;
import software_market.mapper.UserMapper;
import software_market.service.PostService;
import software_market.service.RatingService;
import software_market.vo.post.*;
import software_market.vo.rating.AverageRatingVO;
import software_market.vo.rating.GetStarsVO;
import software_market.cache.MultiLevelCache;
import software_market.constant.RedisKeyConstant;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    private final UserMapper userMapper;
    private final PlikeMapper plikeMapper;
    private final PsaveMapper psaveMapper;
    private final PbrowseMapper pbrowseMapper;
    private final PostMapper postMapper;
    private final RatingService ratingService;
    private final NoticeMapper noticeMapper;
    private final RatingMapper ratingMapper;
    private final PcommentMapper pcommentMapper;
    private final CcommentMapper ccommentMapper;
    private final PclikeMapper pclikeMapper;
    private final PcdenyMapper pcdenyMapper;
    private final CclikeMapper cclikeMapper;
    private final CcdenyMapper ccdenyMapper;
    private final SueMapper sueMapper;
    private final KafkaProducerService kafkaProducerService;
    private final TagMapper tagMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MultiLevelCache cache;
    private final RagIndexEventPublisher ragIndexEventPublisher;

    private static final String USER_CACHE_KEY = "user:";
    private static final String TAGS_CACHE_KEY = "tags:all";
    private static final int USER_L2_TTL = 3600;   // 1h
    private static final int TAGS_L2_TTL = 7200;    // 2h

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer post(PostDTO postDTO) {
        Integer userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(postDTO.getTitle().trim());
        post.setPtext(postDTO.getContent().trim());
        post.setCategory(postDTO.getCategory().trim());
        if (StringUtils.isNotBlank(postDTO.getPhotos())) {
            post.setPhotos(postDTO.getPhotos().trim());
        }
        if (StringUtils.isNotBlank(postDTO.getTagList())) {
            post.setTag(postDTO.getTagList().trim());
        }
        post.setHeat(0.0);
        boolean success = save(post);
        if (!success) {
            throw new BizException("发布失败");
        }
        // 事务提交后异步写入 RAG 索引，不影响发帖主流程
        ragIndexEventPublisher.reindex(post.getPostId());
        return post.getPostId();
    }

    //主逻辑
    @Override
    public List<?> browse(BrowsePostDTO browsePostDTO) {
        String searchsort = browsePostDTO.getSearchSort();  //home / save / history / rating
        String category = browsePostDTO.getCategory();
        String searchinfo = browsePostDTO.getSearchInfo();
        String tag = browsePostDTO.getTag();
        Integer limit = browsePostDTO.getLimit();
        Integer offset = browsePostDTO.getOffset();
        Integer userId = UserContext.getUserId();
        if(searchsort.equals("rating")) {
            return browseRatingPosts(category, searchinfo, tag, limit, offset, userId);
        }
        List<Post> posts=new ArrayList<>();
        if ("save".equals(searchsort)) {
            // searchsort = "save" → 查询当前用户的收藏列表
            posts = browseSavedPosts(category,searchinfo,tag,userId, limit, offset);
        } else if ("history".equals(searchsort)) {
            // searchsort = "history" → 查询当前用户的发帖记录
            posts = browseHistory(category,searchinfo,tag,userId, limit, offset);
        } else {
            // searchsort = "home" → 主页浏览/搜索
            posts = browseHomePosts(category, searchinfo, tag, limit, offset);
        }
        return buildPostVOs(posts, userId);
    }

    @Override
    public GetPostNumVO getPostNum(GetPostNumDTO dto) {

        String searchinfo = dto.getSearchinfo();
        String category = dto.getCategory();
        String searchsort = dto.getSearchsort();
        Integer userId = UserContext.getUserId();
        LambdaQueryWrapper<Post> wrapper =
                Wrappers.lambdaQuery();
        switch (searchsort) {
            // 收藏
            case "save":
                List<Psave> psaves =
                        psaveMapper.selectByUserId(userId);
                List<Integer> saveIds = psaves.stream()
                        .map(Psave::getPtargetId)
                        .distinct()
                        .toList();
                if (saveIds.isEmpty()) {
                    return new GetPostNumVO(0L);
                }
                wrapper.in(Post::getPostId, saveIds);
                break;
            // 发帖历史（用户自己发的帖子）
            case "history":
                wrapper.eq(Post::getUserId, userId);
                break;
            // 评分帖
            case "rating":
                wrapper.eq(Post::getCategory, "打分");
                // 防止外部 category 冲突
                category = null;
                break;
            // 首页
            case "home":
            default:
                break;
        }
        // 分类
        if (StringUtils.isNotBlank(category)
                && !"主页".equals(category)
                && !"历史".equals(category)
                && !"收藏".equals(category)) {

            wrapper.eq(Post::getCategory, category);
        }
        // 搜索
        if (StringUtils.isNotBlank(searchinfo)) {
            wrapper.and(w -> w
                    .like(Post::getTitle, searchinfo)
                    .or()
                    .like(Post::getPtext, searchinfo));
        }
        long count = count(wrapper);
        return new GetPostNumVO(count);
    }

    @Override
    public GetPostTypeVO getPostType(GetPostTypeDTO getPostTypeDTO) {
        Integer postID = getPostTypeDTO.getPostID();
        Post post = getById(postID);
        if(post==null){
            throw new BizException("帖子不存在");
        }
        String postType;
        if ("打分".equals(post.getCategory())) {
            postType = "rating";
        } else {
            postType = "post";
        }

        return new GetPostTypeVO(postType);
    }

    @Override
    public Object showDetails(ShowPostDetailsDTO dto) {

        Integer postId = dto.getPostID();

        Integer currentUserId = UserContext.getUserId();

        // 查询帖子
        Post post = postMapper.selectById(postId);

        if (post == null) {
            throw new BizException("帖子不存在");
        }
        // 查询作者
        User user = userMapper.selectById(post.getUserId());
        // 当前用户点赞状态
        boolean liked = plikeMapper.countByUserAndPost(currentUserId, postId) > 0;
        // 当前用户收藏状态
        boolean saved = psaveMapper.countByUserAndPost(currentUserId, postId) > 0;
        // =========================================================
        // 评分帖子
        // =========================================================
        if ("打分".equals(post.getCategory())) {
            ReviewVO vo = new ReviewVO();
            // 填充公共字段
            fillBasePostInfo(vo, post, user, liked, saved);
            // 评分信息
            RatingDTO ratingDTO = new RatingDTO();

            ratingDTO.setPostID(postId);
            // 平均分
            AverageRatingVO averageRatingVO = ratingService.getAverageRating(ratingDTO);
            // 星级统计
            GetStarsVO getStarsVO = ratingService.getStars(ratingDTO);
            // 当前用户评分
            Integer userRating = ratingService.userPostRating(ratingDTO);
            int[] stars = new int[]{
                    getStarsVO.getStar1(),
                    getStarsVO.getStar2(),
                    getStarsVO.getStar3(),
                    getStarsVO.getStar4(),
                    getStarsVO.getStar5()
            };
            vo.setRating(averageRatingVO.getAverageRating());
            vo.setStars(stars);
            vo.setUserRating(userRating);
            return vo;
        }
        // =========================================================
        // 普通帖子
        // =========================================================
        PostVO vo = new PostVO();
        fillBasePostInfo(vo, post, user, liked, saved);
        return vo;
    }


    /**
     * 填充帖子公共信息
     */
    private void fillBasePostInfo(PostVO vo, Post post, User user, boolean liked, boolean saved) {
        vo.setPostId(post.getPostId());
        vo.setCategory(post.getCategory());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getPtext());
        vo.setLike(post.getLikeNum());
        vo.setComment(post.getCommentNum());
        vo.setBrowse(post.getBrowseNum());
        vo.setHeat(post.getHeat());
        vo.setPostTime(post.getPostTime());
        vo.setPhotos(post.getPhotos());
        vo.setTag(post.getTag());
        vo.setLiked(liked);
        vo.setSaved(saved);
        // 用户信息
        if (user != null) {
            vo.setUserId(user.getUserId());
            vo.setUserName(user.getName());
            vo.setUserAvatar(user.getAvatarUrl());
            vo.setUserIdentity(user.getIdentity());
            vo.setUserScore(user.getScore());
        } else {
            // 管理员帖子兜底
            vo.setUserId(0);
            vo.setUserName("管理员");
            vo.setUserAvatar("");
            vo.setUserIdentity("admin");
            vo.setUserScore(0);
        }
    }

    private List<Post> browseHomePosts(
            String category,
            String searchinfo,
            String tag,
            Integer limit,
            Integer offset) {
        LambdaQueryWrapper<Post> wrapper = Wrappers.lambdaQuery();
        // 主页：不加 category 条件
        boolean isHome = StringUtils.isBlank(category) || "主页".equals(category);
        // 优质贴
        if ("优质贴".equals(category)) {
            wrapper.ge(Post::getHeat, 100);
        } else if (!isHome) {
            wrapper.eq(Post::getCategory, category);
        }
        // 搜索关键词
        if (StringUtils.isNotBlank(searchinfo)) {
            wrapper.and(w -> w
                    .like(Post::getTitle, searchinfo)
                    .or()
                    .like(Post::getPtext, searchinfo));
        }
        // 标签
        if (StringUtils.isNotBlank(tag)) {
            wrapper.like(Post::getTag, tag);
        }
        // 排序
        wrapper.orderByDesc(Post::getPostTime);
        // 分页
        wrapper.last("LIMIT " + offset + "," + limit);
        return list(wrapper);
    }

    private List<Post> browseHistory(String category, String searchinfo, String tag,
            Integer userId, Integer limit, Integer offset) {
        LambdaQueryWrapper<Post> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Post::getUserId, userId);
        if (StringUtils.isNotBlank(category) && !"历史".equals(category)) {
            wrapper.eq(Post::getCategory, category);
        }
        if (StringUtils.isNotBlank(searchinfo)) {
            wrapper.and(w -> w
                    .like(Post::getTitle, searchinfo)
                    .or()
                    .like(Post::getPtext, searchinfo));
        }
        if (StringUtils.isNotBlank(tag)) {
            wrapper.like(Post::getTag, tag);
        }
        wrapper.orderByDesc(Post::getPostTime);
        wrapper.last("LIMIT " + offset + "," + limit);
        return list(wrapper);
    }


    //收藏相关帖子
    private List<Post> browseSavedPosts(String category, String searchinfo, String tag, Integer userId, Integer limit, Integer offset) {
        List<Psave> psaves = psaveMapper.selectByUserId(userId);
        List<Integer> ids = psaves.stream()
                .map(Psave::getPtargetId)
                .toList();
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Post> wrapper = Wrappers.lambdaQuery();
        wrapper.in(Post::getPostId, ids);
        if (StringUtils.isNotBlank(category) && !"收藏".equals(category)) {
            wrapper.eq(Post::getCategory, category);
        }
        if (StringUtils.isNotBlank(searchinfo)) {
            wrapper.and(w -> w
                    .like(Post::getTitle, searchinfo)
                    .or()
                    .like(Post::getPtext, searchinfo));
        }
        if (StringUtils.isNotBlank(tag)) {
            wrapper.like(Post::getTag, tag);
        }
        wrapper.orderByDesc(Post::getPostId);
        wrapper.last("LIMIT " + offset + "," + limit);
        return list(wrapper);
    }

    // ============================================================
    //  组装 PostVO（普通帖 + save/history/home）
    // ============================================================

    private List<PostVO> buildPostVOs(List<Post> posts, Integer userId) {
        List<PostVO> vos = new ArrayList<>();
        for (Post post : posts) {
            if (post == null || post.getPostId() == null) continue;

            User user = getUser(post.getUserId());

            boolean isSaved = psaveMapper.countByUserAndPost(userId, post.getPostId()) > 0;
            boolean isLiked = plikeMapper.countByUserAndPost(userId, post.getPostId()) > 0;

            PostVO vo = new PostVO();
            vo.setPostId(post.getPostId());
            vo.setUserId(post.getUserId());
            vo.setUserName(user != null ? user.getName() : "管理员");
            vo.setUserScore(user != null ? user.getScore() : 0);
            vo.setUserAvatar(user != null ? user.getAvatarUrl() : "");
            vo.setUserIdentity(user != null ? user.getIdentity() : "");
            vo.setTitle(post.getTitle());
            vo.setContent(post.getPtext());
            vo.setLike(post.getLikeNum());
            vo.setComment(post.getCommentNum());
            vo.setBrowse(post.getBrowseNum());
            vo.setHeat(post.getHeat());
            vo.setPostTime(post.getPostTime());
            vo.setSaved(isSaved);
            vo.setLiked(isLiked);
            vo.setPhotos(post.getPhotos());
            vo.setTag(post.getTag());
            vos.add(vo);
        }
        return vos;
    }

    //评分相关帖子
    private List<ReviewVO> browseRatingPosts(String category, String searchinfo, String tag, Integer limit, Integer offset, Integer userId) {

        LambdaQueryWrapper<Post> wrapper= Wrappers.lambdaQuery();
        wrapper.eq(Post::getCategory, "打分");
        if (StringUtils.isNotBlank(searchinfo)) {
            wrapper.and(w -> w
                    .like(Post::getTitle, searchinfo)
                    .or()
                    .like(Post::getPtext, searchinfo));
        }
        if (StringUtils.isNotBlank(tag)) {
            wrapper.like(Post::getTag, tag);
        }
        wrapper.orderByDesc(Post::getPostId);
        wrapper.last("limit " + offset + "," + limit);
        List<Post> posts=list(wrapper);
        return buildReviewVOs(posts, userId);
    }

    private List<ReviewVO> buildReviewVOs(List<Post> posts, Integer userId) {
        List<ReviewVO> vos = new ArrayList<>();
        for (Post post : posts) {
            if (post == null || post.getPostId() == null) continue;

            User user = getUser(post.getUserId());
            boolean isLiked = plikeMapper.countByUserAndPost(userId, post.getPostId()) > 0;

            // 查询评分统计
            RatingDTO ratingDTO = new RatingDTO();
            ratingDTO.setPostID(post.getPostId());
            AverageRatingVO avgVO = ratingService.getAverageRating(ratingDTO);
            GetStarsVO starsVO = ratingService.getStars(ratingDTO);
            int userRating = ratingService.userPostRating(ratingDTO);
            int[] stars = new int[]{
                starsVO.getStar1(), starsVO.getStar2(), starsVO.getStar3(),
                starsVO.getStar4(), starsVO.getStar5()
            };

            ReviewVO vo = new ReviewVO();
            vo.setPostId(post.getPostId());
            vo.setUserId(post.getUserId());
            vo.setUserName(user != null ? user.getName() : "管理员");
            vo.setUserScore(user != null ? user.getScore() : 0);
            vo.setUserAvatar(user != null ? user.getAvatarUrl() : "");
            vo.setUserIdentity(user != null ? user.getIdentity() : "");
            vo.setTitle(post.getTitle());
            vo.setContent(post.getPtext());
            vo.setLike(post.getLikeNum());
            vo.setComment(post.getCommentNum());
            vo.setBrowse(post.getBrowseNum());
            vo.setHeat(post.getHeat());
            vo.setPostTime(post.getPostTime());
            vo.setSaved(false);
            vo.setLiked(isLiked);
            vo.setPhotos(post.getPhotos());
            vo.setTag(post.getTag());
            vo.setRating(avgVO.getAverageRating());
            vo.setStars(stars);
            vo.setUserRating(userRating);
            vos.add(vo);
        }
        return vos;
    }


    @Override
    public void updateBrowseNum(UpdateBrowseNumDTO dto) {
        Integer postId = dto.getPostID();
        Integer userId = UserContext.getUserId();

        if (pbrowseMapper.countByUserAndPost(userId, postId) == 0) {
            postMapper.updateBrowseNum(postId);
            postMapper.updateHeat(postId, 1.0);
        }

        Pbrowse pbrowse = new Pbrowse();
        pbrowse.setPtargetId(postId);
        pbrowse.setUserId(userId);
        pbrowse.setTime(LocalDateTime.now());
        pbrowseMapper.upsert(pbrowse);
    }

    @Override
    @Transactional
    public void updateLike(UpdateDTO updateLikeDTO) {
        Integer postID = updateLikeDTO.getPostID();
        Integer userId = UserContext.getUserId();
        Post post = postMapper.selectById(postID);
        if(post==null){
            throw  new BizException("帖子不存在");
        }
        if (plikeMapper.countByUserAndPost(userId, postID) > 0) {
            plikeMapper.deleteByUserAndPost(userId, postID);
            postMapper.updateLikeNum(postID, -1);
        } else {
            Plike plike = new Plike();
            plike.setUserId(userId);
            plike.setPtargetId(postID);
            plike.setTime(LocalDateTime.now());
            plikeMapper.insert(plike);
            postMapper.updateLikeNum(postID, 1);
            postMapper.updateHeat(postID, 3.0);

            // 通过 Kafka 异步通知帖子作者（自己赞自己不通知）
            if (!userId.equals(post.getUserId())) {
                NotificationEvent event = new NotificationEvent(
                        post.getUserId(), userId, "post",
                        "赞了你的帖子", postID);
                kafkaProducerService.sendNotificationEvent(event);
            }
        }
    }

    @Override
    public void updateSave(UpdateDTO updateSaveDTO) {
        Integer postID = updateSaveDTO.getPostID();
        Integer userId = UserContext.getUserId();
        Post post = postMapper.selectById(postID);
        if(post==null){
            throw  new BizException("帖子不存在");
        }
        if (psaveMapper.countByUserAndPost(userId, postID) > 0) {
            psaveMapper.deleteByUserAndPost(userId, postID);
        } else {
            Psave psave = new Psave();
            psave.setUserId(userId);
            psave.setPtargetId(postID);
            psaveMapper.insert(psave);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(DeletePostDTO deletePostDTO) {
        Integer postID = deletePostDTO.getPostID();
        Integer userId = UserContext.getUserId();
        Post post = postMapper.selectById(postID);
        if (post == null) {
            throw new BizException("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BizException("没有权限删除该帖子");
        }

        LambdaQueryWrapper<Pcomment> pcommentWrapper = Wrappers.lambdaQuery();
        pcommentWrapper.select(Pcomment::getPcommentId).eq(Pcomment::getPtargetId, postID);
        List<Integer> pcommentIds = pcommentMapper.selectObjs(pcommentWrapper)
                .stream().map(o -> (Integer) o).collect(Collectors.toList());

        List<Integer> ccommentIds = new ArrayList<>();
        if (!pcommentIds.isEmpty()) {
            LambdaQueryWrapper<Ccomment> ccommentWrapper = Wrappers.lambdaQuery();
            ccommentWrapper.select(Ccomment::getCcommentId).in(Ccomment::getCtargetId, pcommentIds);
            ccommentIds = ccommentMapper.selectObjs(ccommentWrapper)
                    .stream().map(o -> (Integer) o).collect(Collectors.toList());
        }

        if (!ccommentIds.isEmpty()) {
            cclikeMapper.delete(Wrappers.<Cclike>lambdaQuery().in(Cclike::getCctargetId, ccommentIds));
            ccdenyMapper.delete(Wrappers.<Ccdeny>lambdaQuery().in(Ccdeny::getCctargetId, ccommentIds));
        }

        if (!ccommentIds.isEmpty()) {
            ccommentMapper.deleteBatchIds(ccommentIds);
        }

        if (!pcommentIds.isEmpty()) {
            pclikeMapper.delete(Wrappers.<Pclike>lambdaQuery().in(Pclike::getPctargetId, pcommentIds));
            pcdenyMapper.delete(Wrappers.<Pcdeny>lambdaQuery().in(Pcdeny::getPctargetId, pcommentIds));
        }

        if (!pcommentIds.isEmpty()) {
            pcommentMapper.deleteBatchIds(pcommentIds);
        }

        List<Integer> allTargetIds = new ArrayList<>();
        allTargetIds.add(postID);
        allTargetIds.addAll(pcommentIds);
        allTargetIds.addAll(ccommentIds);
        noticeMapper.delete(Wrappers.<Notice>lambdaQuery().in(Notice::getTarget, allTargetIds));

        ratingMapper.deleteByItem("post", postID);

        plikeMapper.deleteByPostId(postID);
        psaveMapper.deleteByPostId(postID);
        pbrowseMapper.deleteByPostId(postID);

        postMapper.deleteById(postID);
        try {
            stringRedisTemplate.delete(RedisKeyConstant.AI_ANALYSIS_CACHE + postID);
        } catch (Exception e) {
            log.warn("清理 AI 分析缓存失败，不影响帖子删除: postId={}", postID, e);
        }

        // 事务提交后删除该帖在 Milvus 中的全部向量
        ragIndexEventPublisher.delete(postID);
    }

    @Override
    public void submitReport(SubmitReportDTO submitReportDTO) {

        Integer targetID = submitReportDTO.getTargetID();
        String targetType = submitReportDTO.getTargetType();
        String reason = submitReportDTO.getReason();

        LambdaQueryWrapper<Sue> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Sue::getUserId,UserContext.getUserId())
                .eq(Sue::getTargetId, targetID)
                .eq(Sue::getTargettype, targetType)
                .eq(Sue::getStatus, "wait");
        if (sueMapper.selectCount(wrapper) > 0) {
            throw new BizException("请勿重复举报");
        }

        if ("post".equals(targetType)) {
            Post post = postMapper.selectById(targetID);
            if (post == null) {
                throw new BizException("帖子不存在");
            }
        } else if ("pcomment".equals(targetType)) {
            Pcomment pcomment = pcommentMapper.selectById(targetID);
            if (pcomment == null) {
                throw new BizException("评论不存在");
            }
        } else if ("ccomment".equals(targetType)) {
            Ccomment ccomment = ccommentMapper.selectById(targetID);
            if (ccomment == null) {
                throw new BizException("评论不存在");
            }
        } else {
            throw new BizException("类型错误");
        }
        Sue sue = new Sue();
        sue.setTargetId(targetID);
        sue.setTargettype(targetType);
        sue.setReason(reason);
        sue.setStatus("wait");
        sue.setTime(LocalDateTime.now());
        sue.setUserId(UserContext.getUserId());
        sueMapper.insert(sue);
    }

    @Override
    public List<Tag> getTags() {
        return cache.get(TAGS_CACHE_KEY,
                new com.alibaba.fastjson2.TypeReference<List<Tag>>() {}.getType(),
                TAGS_L2_TTL,
                () -> tagMapper.selectList(null));
    }

    @Override
    public List<HeatResponseVO> calculateHeat() {
        var zsetOps = stringRedisTemplate.opsForZSet();
        var entries = zsetOps.reverseRangeWithScores(RedisKeyConstant.HOT_POSTS_ZSET, 0, 9);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Integer> postIds = entries.stream()
                .map(e -> Integer.valueOf(e.getValue()))
                .toList();
        var postMap = listByIds(postIds).stream()
                .collect(Collectors.toMap(Post::getPostId, Post::getTitle));
        return entries.stream().map(entry -> {
            HeatResponseVO vo = new HeatResponseVO();
            vo.setPostID(Integer.valueOf(entry.getValue()));
            vo.setHeat(entry.getScore());
            vo.setTitle(postMap.get(Integer.valueOf(entry.getValue())));
            return vo;
        }).toList();
    }

    private User getUser(Integer userId) {
        if (userId == null || userId == 0) {
            User admin = new User();
            admin.setName("管理员");
            admin.setScore(0);
            admin.setAvatarUrl("");
            admin.setIdentity("official");
            return admin;
        }
        User user = cache.get(USER_CACHE_KEY + userId, User.class, USER_L2_TTL,
                () -> userMapper.selectById(userId));
        if (user == null) {
            User deletedUser = new User();
            deletedUser.setName("用户已注销");
            deletedUser.setScore(0);
            deletedUser.setAvatarUrl("");
            deletedUser.setIdentity("");
            return deletedUser;
        }
        return user;
    }
}
