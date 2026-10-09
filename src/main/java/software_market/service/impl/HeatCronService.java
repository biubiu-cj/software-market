package software_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software_market.constant.RedisKeyConstant;
import software_market.entity.post.Post;
import software_market.mapper.post.PostMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeatCronService {

    private static final int HEAT_DAY = 10;
    private static final String EXCLUDED_CATEGORY = "课程交流";

    private final PostMapper postMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 0 * * * *")
    public void recalculateHeat() {
        LocalDateTime since = LocalDateTime.now().minusDays(HEAT_DAY);
        LambdaQueryWrapper<Post> wrapper = Wrappers.lambdaQuery();
        wrapper.ge(Post::getPostTime, since)
                .ne(Post::getCategory, EXCLUDED_CATEGORY);
        List<Post> posts = postMapper.selectList(wrapper);

        var zsetOps = stringRedisTemplate.opsForZSet();
        String key = RedisKeyConstant.HOT_POSTS_ZSET;

        // 清空旧数据
        stringRedisTemplate.delete(key);

        for (Post post : posts) {
            double browseNum = nvl(post.getBrowseNum());
            double commentNum = nvl(post.getCommentNum());
            double likeNum = nvl(post.getLikeNum());

            double inner = browseNum + commentNum / 10.0 + likeNum / 2.0;
            if (inner <= 0) {
                continue;
            }

            long daysSincePost = ChronoUnit.DAYS.between(post.getPostTime(), LocalDateTime.now());
            double heat = Math.log10(inner) - daysSincePost;

            zsetOps.add(key, String.valueOf(post.getPostId()), heat);
        }

        log.info("热度重算完成，共 {} 条帖子", posts.size());
    }

    private static double nvl(Integer val) {
        return val == null ? 0 : val.doubleValue();
    }
}
