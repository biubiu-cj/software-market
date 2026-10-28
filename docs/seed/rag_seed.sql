-- 由 docs/seed/generate_rag_seed.py 自动生成，请勿手工修改。
-- RAG 演示数据：多主题、同义提问、回复中包含标准答案。
SET NAMES utf8mb4;
START TRANSACTION;

-- ============ 清理上一次导入的种子数据 ============
CREATE TEMPORARY TABLE IF NOT EXISTS _seed_users (user_id INT PRIMARY KEY);
DELETE FROM _seed_users;
INSERT INTO _seed_users SELECT user_id FROM user WHERE email LIKE 'ragseed_%';
CREATE TEMPORARY TABLE IF NOT EXISTS _seed_posts (post_id INT PRIMARY KEY);
DELETE FROM _seed_posts;
INSERT INTO _seed_posts SELECT post_id FROM post WHERE user_id IN (SELECT user_id FROM _seed_users);
CREATE TEMPORARY TABLE IF NOT EXISTS _seed_pcomments (pcomment_id INT PRIMARY KEY);
DELETE FROM _seed_pcomments;
INSERT INTO _seed_pcomments SELECT pcomment_id FROM pcomment WHERE ptarget_id IN (SELECT post_id FROM _seed_posts) OR user_id IN (SELECT user_id FROM _seed_users);
CREATE TEMPORARY TABLE IF NOT EXISTS _seed_ccomments (ccomment_id INT PRIMARY KEY);
DELETE FROM _seed_ccomments;
INSERT INTO _seed_ccomments SELECT ccomment_id FROM ccomment WHERE ctarget_id IN (SELECT pcomment_id FROM _seed_pcomments) OR user_id IN (SELECT user_id FROM _seed_users);
DELETE FROM cclike WHERE cctarget_id IN (SELECT ccomment_id FROM _seed_ccomments);
DELETE FROM ccdeny WHERE cctarget_id IN (SELECT ccomment_id FROM _seed_ccomments);
DELETE FROM pclike WHERE pctarget_id IN (SELECT pcomment_id FROM _seed_pcomments);
DELETE FROM pcdeny WHERE pctarget_id IN (SELECT pcomment_id FROM _seed_pcomments);
DELETE FROM notice WHERE target IN (SELECT post_id FROM _seed_posts) OR target IN (SELECT pcomment_id FROM _seed_pcomments) OR target IN (SELECT ccomment_id FROM _seed_ccomments);
DELETE FROM rating WHERE item_type = 'post' AND item_id IN (SELECT post_id FROM _seed_posts);
DELETE FROM plike WHERE ptarget_id IN (SELECT post_id FROM _seed_posts);
DELETE FROM psave WHERE ptarget_id IN (SELECT post_id FROM _seed_posts);
DELETE FROM pbrowse WHERE ptarget_id IN (SELECT post_id FROM _seed_posts);
DELETE FROM ccomment WHERE ccomment_id IN (SELECT ccomment_id FROM _seed_ccomments);
DELETE FROM pcomment WHERE pcomment_id IN (SELECT pcomment_id FROM _seed_pcomments);
DELETE FROM post WHERE post_id IN (SELECT post_id FROM _seed_posts);
DELETE FROM user WHERE email LIKE 'ragseed_%';
DROP TEMPORARY TABLE _seed_ccomments;
DROP TEMPORARY TABLE _seed_pcomments;
DROP TEMPORARY TABLE _seed_posts;
DROP TEMPORARY TABLE _seed_users;

-- ============ 种子用户 ============
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_linxiao@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '林晓', '软件工程大三，喜欢折腾后端', 'student', 128, '', 0, 0, 0);
SET @u0 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_chenziang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '陈子昂', '计算机大二，Java 学习中', 'student', 96, '', 0, 0, 0);
SET @u1 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_wangyutong@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王雨桐', '软件工程研一，方向是数据挖掘', 'student', 210, '', 0, 0, 0);
SET @u2 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_lihang@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李航', '计算机大四，已保研', 'student', 356, '', 0, 0, 0);
SET @u3 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_zhangwei@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张伟', '计算机学院教师，讲授组成原理', 'teacher', 500, '', 0, 0, 0);
SET @u4 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_liusiyuan@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '刘思远', '网络工程大三', 'student', 74, '', 0, 0, 0);
SET @u5 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_zhaoyiming@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '赵一鸣', '计科大一，新生一枚', 'student', 22, '', 0, 0, 0);
SET @u6 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_sunyue@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '孙悦', '软件工程大二，爱逛食堂', 'student', 65, '', 0, 0, 0);
SET @u7 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_zhoukexin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '周可欣', '数字媒体大二', 'student', 48, '', 0, 0, 0);
SET @u8 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_wuhao@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '吴昊', '计算机大三，会一点运维', 'student', 152, '', 0, 0, 0);
SET @u9 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_zhengxiaolin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '郑晓琳', '软件工程大四，在实习', 'student', 268, '', 0, 0, 0);
SET @u10 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_heluo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '何洛', '计科大三，后端方向', 'student', 134, '', 0, 0, 0);
SET @u11 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_xujia@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '徐嘉', '软件工程大二', 'student', 58, '', 0, 0, 0);
SET @u12 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_mateng@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '马腾', '人工智能大一', 'student', 18, '', 0, 0, 0);
SET @u13 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_lijing@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李静', '教务处老师，负责学籍与考试', 'teacher', 460, '', 0, 0, 0);
SET @u14 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_wangyinuo@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王一诺', '软件工程大三，考研准备中', 'student', 88, '', 0, 0, 0);
SET @u15 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_tangkeke@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '唐可可', '计科大四，准备找工作', 'student', 176, '', 0, 0, 0);
SET @u16 := LAST_INSERT_ID();
INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) VALUES ('ragseed_gaoyuan@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '高远', '网络空间安全大三', 'student', 122, '', 0, 0, 0);
SET @u17 := LAST_INSERT_ID();

-- ============ 课程标签（课程专区/打分帖使用） ============
INSERT IGNORE INTO tag (name, value, type, num) VALUES ('张伟', 'zhangwei@example.edu.cn', 'course', 1);
INSERT IGNORE INTO tag (name, value, type, num) VALUES ('李静', 'lijing@example.edu.cn', 'course', 2);
INSERT IGNORE INTO tag (name, value, type, num) VALUES ('王强', 'wangqiang@example.edu.cn', 'course', 3);
INSERT IGNORE INTO tag (name, value, type, num) VALUES ('刘敏', 'liumin@example.edu.cn', 'course', 4);
INSERT IGNORE INTO tag (name, value, type, num) VALUES ('陈杰', 'chenjie@example.edu.cn', 'course', 5);

-- ============ 帖子 / 评论 / 二级回复 / 评分 ============

-- 补考申请截止时间
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '院务', '补考申请截止时间', '请问补考最晚什么时候申请，错过了还能补办吗？', 2, 5, 55, DATE_SUB(NOW(), INTERVAL 172965 MINUTE), 100.0, '', '考试');
SET @p0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p0, 28, 0, '先确认成绩是否已录入，然后在教务系统提交补考申请，截止时间是成绩公布后一周。错过时间联系辅导员走线下流程。', DATE_SUB(NOW(), INTERVAL 172805 MINUTE));
SET @c0_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p0, 1, 0, '同问，我也想知道', DATE_SUB(NOW(), INTERVAL 172836 MINUTE));
SET @c0_1 := LAST_INSERT_ID();

-- 校园网账号密码是什么
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u10, '其他', '校园网账号密码是什么', '新生的校园网账号和初始密码是什么？', 2, 98, 665, DATE_SUB(NOW(), INTERVAL 171467 MINUTE), 975.0, '', '校园网');
SET @p1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p1, 11, 0, '校园网账号就是学号，密码初始为身份证后六位，改过的用新密码。掉线一般是设备数超了，去自助服务解绑。', DATE_SUB(NOW(), INTERVAL 171334 MINUTE));
SET @c1_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p1, 5, 0, '感谢分享', DATE_SUB(NOW(), INTERVAL 171258 MINUTE));
SET @c1_1 := LAST_INSERT_ID();

-- 选课系统什么时候开放
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u0, '课程专区', '选课系统什么时候开放', '下学期选课什么时候开始？热门课很难抢吗？', 2, 8, 51, DATE_SUB(NOW(), INTERVAL 169879 MINUTE), 103.0, '', '选课');
SET @p2 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p2, 17, 0, '教务系统整点开放，提前10分钟登录，用校园网更稳。热门课拼手速，没抢到就等第一周退补选，会有人退。', DATE_SUB(NOW(), INTERVAL 169674 MINUTE));
SET @c2_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p2, 4, 0, '感谢学长', DATE_SUB(NOW(), INTERVAL 169860 MINUTE));
SET @c2_1 := LAST_INSERT_ID();

-- 简历没有实习怎么办
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u3, '求职招募', '简历没有实习怎么办', '没有实习经历，简历怎么突出项目？', 3, 2, 51, DATE_SUB(NOW(), INTERVAL 168737 MINUTE), 95.0, '', '简历');
SET @p3 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p3, 22, 0, '校招简历一页足够，按「基本信息-技能-项目-实习-教育」排。项目要能讲清楚难点，别堆名词。', DATE_SUB(NOW(), INTERVAL 168716 MINUTE));
SET @c3_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p3, 3, 0, '感谢内推信息', DATE_SUB(NOW(), INTERVAL 168699 MINUTE));
SET @c3_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p3, 17, 0, '一页纸，项目用 STAR 法则写：背景、任务、行动、结果，尽量量化。技术栈放显眼位置，不要编造经历。', DATE_SUB(NOW(), INTERVAL 168574 MINUTE));
SET @c3_2 := LAST_INSERT_ID();

-- 宿舍热水又停了
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u5, '日常吐槽', '宿舍热水又停了', '晚上洗澡洗到一半变冷水，后勤说在检修。', 2, 48, 279, DATE_SUB(NOW(), INTERVAL 166789 MINUTE), 455.0, '', '宿舍');
SET @p4 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p4, 4, 0, '我们楼也是，说明天恢复', DATE_SUB(NOW(), INTERVAL 166560 MINUTE));
SET @c4_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p4, 3, 0, '先去楼下公共浴室吧', DATE_SUB(NOW(), INTERVAL 166771 MINUTE));
SET @c4_1 := LAST_INSERT_ID();

-- 没抢到课怎么办
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u1, '课程专区', '没抢到课怎么办', '选课没抢到心仪的课，后面还有机会吗？', 4, 3, 25, DATE_SUB(NOW(), INTERVAL 165718 MINUTE), 66.0, '', '选课');
SET @p5 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p5, 32, 0, '选课在教务系统，开放时间看通知。建议列好备选课，第一周退补选是捡漏的好机会。', DATE_SUB(NOW(), INTERVAL 165597 MINUTE));
SET @c5_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p5, 5, 0, '已收藏', DATE_SUB(NOW(), INTERVAL 165563 MINUTE));
SET @c5_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p5, 17, 0, '抢课前把课程代码准备好，开放瞬间提交。选不上先加候选，开学第一周盯紧退课名额。', DATE_SUB(NOW(), INTERVAL 165591 MINUTE));
SET @c5_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u1, @c5_0, 4, 0, '明白了，谢谢学长！', DATE_SUB(NOW(), INTERVAL 165243 MINUTE), '唐可可');

-- Git冲突怎么解决
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u16, '学习交流', 'Git冲突怎么解决', 'pull 的时候提示冲突，怎么处理才不会丢代码？', 2, 5, 44, DATE_SUB(NOW(), INTERVAL 164068 MINUTE), 87.0, '', 'Git');
SET @p6 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p6, 14, 0, '先 git pull --rebase 同步，冲突文件手动解决后 git add，再 git rebase --continue。千万别直接 force push 到 master。', DATE_SUB(NOW(), INTERVAL 164021 MINUTE));
SET @c6_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p6, 7, 0, '这个回答很全，收藏了', DATE_SUB(NOW(), INTERVAL 164005 MINUTE));
SET @c6_1 := LAST_INSERT_ID();

-- 暑期实习怎么找
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u5, '求职招募', '暑期实习怎么找', '大二升大三，想找暑期实习，应该从哪开始？', 3, 43, 633, DATE_SUB(NOW(), INTERVAL 162314 MINUTE), 781.0, '', '实习');
SET @p7 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p7, 36, 0, '暑期实习3-4月集中投递，牛客、官网和内推都试试。准备八股、算法和项目，小厂练手、大厂冲刺。', DATE_SUB(NOW(), INTERVAL 162299 MINUTE));
SET @c7_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p7, 8, 0, '先码住', DATE_SUB(NOW(), INTERVAL 162093 MINUTE));
SET @c7_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u5, @c7_0, 0, 0, '谢谢，信息很及时。', DATE_SUB(NOW(), INTERVAL 161866 MINUTE), '吴昊');

-- 本地Redis怎么启动
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u13, '学习交流', '本地Redis怎么启动', 'Windows 上怎么启动 Redis，项目要连 localhost:6379。', 2, 3, 29, DATE_SUB(NOW(), INTERVAL 160903 MINUTE), 70.0, '', 'Redis');
SET @p8 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p8, 36, 0, 'Redis 报连接拒绝一般是没启动或端口不对。改配置后记得重启，密码要和应用配置一致。', DATE_SUB(NOW(), INTERVAL 160664 MINUTE));
SET @c8_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p8, 3, 0, '这个回答很全，收藏了', DATE_SUB(NOW(), INTERVAL 160674 MINUTE));
SET @c8_1 := LAST_INSERT_ID();

-- 校内电动车要上牌吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u13, '其他', '校内电动车要上牌吗', '现在学校查不查无牌电动车，必须上牌吗？', 2, 4, 59, DATE_SUB(NOW(), INTERVAL 159388 MINUTE), 97.0, '', '二手');
SET @p9 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p9, 32, 0, '闲鱼同城约在校门口试车，检查刹车、灯和电池。学校现在查无牌车，尽量买带牌的。', DATE_SUB(NOW(), INTERVAL 159358 MINUTE));
SET @c9_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p9, 5, 0, '学到了', DATE_SUB(NOW(), INTERVAL 159336 MINUTE));
SET @c9_1 := LAST_INSERT_ID();

-- 清灰换硅脂多少钱
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '其他', '清灰换硅脂多少钱', '电脑想清灰换硅脂，学校维修点大概多少钱？', 3, 38, 341, DATE_SUB(NOW(), INTERVAL 158222 MINUTE), 488.0, '', '电脑');
SET @p10 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p10, 23, 0, '小问题先去信息楼B座维修点，价格透明。保修期内的品牌机建议走官方售后，别自己拆。', DATE_SUB(NOW(), INTERVAL 158053 MINUTE));
SET @c10_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p10, 5, 0, '帮顶', DATE_SUB(NOW(), INTERVAL 158216 MINUTE));
SET @c10_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p10, 11, 0, '电脑进水、黑屏先断电，送信息楼B座检测。清灰、换硬盘这些学生价不贵，数据记得先备份。', DATE_SUB(NOW(), INTERVAL 158045 MINUTE));
SET @c10_2 := LAST_INSERT_ID();

-- Redis密码怎么配
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '学习交流', 'Redis密码怎么配', 'Redis 设置了密码，Spring 配置怎么写？', 4, 43, 348, DATE_SUB(NOW(), INTERVAL 156242 MINUTE), 506.0, '', 'Redis');
SET @p11 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p11, 15, 0, '本地开发可以用 Docker 起 Redis，或者 WSL 里 redis-server。Spring 配置 host/port/password 要和实际一致。', DATE_SUB(NOW(), INTERVAL 156218 MINUTE));
SET @c11_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p11, 3, 0, '学到了，感谢分享', DATE_SUB(NOW(), INTERVAL 156147 MINUTE));
SET @c11_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p11, 7, 0, '先 redis-cli ping 看有没有 PONG。没起就启动服务；连不上检查 bind 和 protected-mode，WSL 里启动后 Windows 用 localhost 访问。', DATE_SUB(NOW(), INTERVAL 156193 MINUTE));
SET @c11_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u15, @c11_0, 3, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 155941 MINUTE), '何洛');

-- Spring Boot连不上数据库
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u7, '学习交流', 'Spring Boot连不上数据库', '启动提示数据库连接失败，配置检查过没问题。', 3, 1, 14, DATE_SUB(NOW(), INTERVAL 155169 MINUTE), 42.0, '', 'SpringBoot');
SET @p12 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p12, 10, 0, '启动失败九成是依赖没起：MySQL、Redis、Kafka 挨个确认。端口冲突就改 server.port 或杀掉占用进程。', DATE_SUB(NOW(), INTERVAL 155017 MINUTE));
SET @c12_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p12, 5, 0, '学到了，感谢分享', DATE_SUB(NOW(), INTERVAL 155017 MINUTE));
SET @c12_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u7, @c12_0, 2, 0, '好的，我这就去试试。', DATE_SUB(NOW(), INTERVAL 154825 MINUTE), '李航');

-- 成绩单在哪打印
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u7, '院务', '成绩单在哪打印', '要申请出国，需要中英文成绩单，去哪里打印比较快？', 4, 5, 36, DATE_SUB(NOW(), INTERVAL 153700 MINUTE), 90.0, '', '成绩单');
SET @p13 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p13, 30, 0, '行政楼2楼有自助打印机，刷校园卡就能打中英文成绩单，每份2元。出国申请需要盖章版的话去教务处窗口，提前3个工作日。', DATE_SUB(NOW(), INTERVAL 153588 MINUTE));
SET @c13_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p13, 1, 0, '帮顶，蹲一个准确答案', DATE_SUB(NOW(), INTERVAL 153560 MINUTE));
SET @c13_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p13, 17, 0, '自助打印机在行政楼二楼大厅，中英文都能打。要官方盖章的到教务处106，带学生证，一般当天能取；高峰期建议提前3天。', DATE_SUB(NOW(), INTERVAL 153542 MINUTE));
SET @c13_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u7, @c13_0, 1, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 153412 MINUTE), '王一诺');

-- 大件快递怎么取
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '其他', '大件快递怎么取', '买了个显示器，大件快递会送到哪里？', 3, 6, 36, DATE_SUB(NOW(), INTERVAL 151912 MINUTE), 88.0, '', '快递');
SET @p14 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p14, 35, 0, '东门学生服务中心一楼是菜鸟驿站，21:30关门。大件会送到南门，收到短信再过去。', DATE_SUB(NOW(), INTERVAL 151890 MINUTE));
SET @c14_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p14, 2, 0, '同问', DATE_SUB(NOW(), INTERVAL 151903 MINUTE));
SET @c14_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p14, 13, 0, '菜鸟驿站在东门学生服务中心一楼，营业到21:30；顺丰和京东在西门快递柜；大件行李到南门货运点。取件带取件码就行。', DATE_SUB(NOW(), INTERVAL 151733 MINUTE));
SET @c14_2 := LAST_INSERT_ID();

-- 英文成绩单怎么开
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '院务', '英文成绩单怎么开', '学校能开英文成绩单吗？需要盖章的话走什么流程？', 2, 105, 811, DATE_SUB(NOW(), INTERVAL 150385 MINUTE), 1151.0, '', '成绩单');
SET @p15 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p15, 22, 0, '自助打印机在行政楼二楼大厅，中英文都能打。要官方盖章的到教务处106，带学生证，一般当天能取；高峰期建议提前3天。', DATE_SUB(NOW(), INTERVAL 150381 MINUTE));
SET @c15_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p15, 5, 0, '帮顶，蹲一个准确答案', DATE_SUB(NOW(), INTERVAL 150370 MINUTE));
SET @c15_1 := LAST_INSERT_ID();

-- 考研自习室在哪
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '学习交流', '考研自习室在哪', '准备考研，学校哪里可以长期自习？', 4, 3, 29, DATE_SUB(NOW(), INTERVAL 148809 MINUTE), 65.0, '', '考研');
SET @p16 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p16, 14, 0, '图书馆4楼有考研专区，9月开放长期座位申请，需要预约。教学楼空教室晚上开到23点，考试周会延长。', DATE_SUB(NOW(), INTERVAL 148592 MINUTE));
SET @c16_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p16, 6, 0, '蹲一个后续', DATE_SUB(NOW(), INTERVAL 148601 MINUTE));
SET @c16_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p16, 4, 0, '长期座位在图书馆4楼，开学初在系统申请，先到先得。平时空教室也能自习，看门口课表挑没课的。', DATE_SUB(NOW(), INTERVAL 148754 MINUTE));
SET @c16_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u15, @c16_0, 3, 0, '好的，我这就去试试。', DATE_SUB(NOW(), INTERVAL 148591 MINUTE), '李航');

-- 简历一页够吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u2, '求职招募', '简历一页够吗', '应届生简历写一页还是两页，技术栈放哪里？', 3, 0, 25, DATE_SUB(NOW(), INTERVAL 147728 MINUTE), 57.0, '', '简历');
SET @p17 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p17, 22, 0, '简历突出项目和实习，写清楚你负责什么、用了什么技术、解决了什么问题、有什么数据结果。', DATE_SUB(NOW(), INTERVAL 147574 MINUTE));
SET @c17_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p17, 1, 0, '感谢内推信息', DATE_SUB(NOW(), INTERVAL 147668 MINUTE));
SET @c17_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p17, 13, 0, '校招简历一页足够，按「基本信息-技能-项目-实习-教育」排。项目要能讲清楚难点，别堆名词。', DATE_SUB(NOW(), INTERVAL 147536 MINUTE));
SET @c17_2 := LAST_INSERT_ID();

-- 捡到一串钥匙
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u11, '其他', '捡到一串钥匙', '在图书馆一楼捡到一串钥匙，挂着蓝色卡套，失主联系我。', 3, 4, 56, DATE_SUB(NOW(), INTERVAL 145899 MINUTE), 92.0, '', '失物招领');
SET @p18 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u8, @p18, 4, 0, '帮顶，希望失主看到', DATE_SUB(NOW(), INTERVAL 145868 MINUTE));
SET @c18_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p18, 6, 0, '可以交给图书馆前台', DATE_SUB(NOW(), INTERVAL 145757 MINUTE));
SET @c18_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u11, @c18_0, 2, 0, '谢谢，信息很及时。', DATE_SUB(NOW(), INTERVAL 145459 MINUTE), '周可欣');

-- 体测800米怎么练
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u13, '学习交流', '体测800米怎么练', '下个月体测，800米每次都跑不下来，求训练方法。', 2, 23, 175, DATE_SUB(NOW(), INTERVAL 144665 MINUTE), 272.0, '', '体测');
SET @p19 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p19, 15, 0, '每周跑三次，先慢跑两公里，两周就能适应', DATE_SUB(NOW(), INTERVAL 144551 MINUTE));
SET @c19_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p19, 8, 0, '跑前热身，跟着队伍节奏别冲太快', DATE_SUB(NOW(), INTERVAL 144551 MINUTE));
SET @c19_1 := LAST_INSERT_ID();

-- 计算机组成原理
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u16, '打分', '计算机组成原理', '内容偏硬核，期末考试要认真准备。', 2, 51, 809, DATE_SUB(NOW(), INTERVAL 143121 MINUTE), 983.0, '', '张伟');
SET @p20 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p20, 3, 0, '这门课我也上过，整体体验和楼主差不多。', DATE_SUB(NOW(), INTERVAL 142987 MINUTE));
SET @c20_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p20, 4, 0, '中肯', DATE_SUB(NOW(), INTERVAL 142951 MINUTE));
SET @c20_1 := LAST_INSERT_ID();
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u5, 'post', @p20, 4, DATE_SUB(NOW(), INTERVAL 142521 MINUTE), DATE_SUB(NOW(), INTERVAL 142521 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u0, 'post', @p20, 3, DATE_SUB(NOW(), INTERVAL 142521 MINUTE), DATE_SUB(NOW(), INTERVAL 142521 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u15, 'post', @p20, 4, DATE_SUB(NOW(), INTERVAL 142521 MINUTE), DATE_SUB(NOW(), INTERVAL 142521 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u1, 'post', @p20, 4, DATE_SUB(NOW(), INTERVAL 142521 MINUTE), DATE_SUB(NOW(), INTERVAL 142521 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u0, 'post', @p20, 5, DATE_SUB(NOW(), INTERVAL 142521 MINUTE), DATE_SUB(NOW(), INTERVAL 142521 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u5, 'post', @p20, 3, DATE_SUB(NOW(), INTERVAL 142521 MINUTE), DATE_SUB(NOW(), INTERVAL 142521 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u15, 'post', @p20, 4, DATE_SUB(NOW(), INTERVAL 142521 MINUTE), DATE_SUB(NOW(), INTERVAL 142521 MINUTE));

-- 开学买书攻略
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u17, '其他', '开学买书攻略', '开学买教材有什么省钱的办法？', 2, 24, 153, DATE_SUB(NOW(), INTERVAL 141824 MINUTE), 240.0, '', '教材');
SET @p21 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p21, 12, 0, '专业课教材建议买二手，公共课用电子版也行。开学前在群里问，毕业季最便宜。', DATE_SUB(NOW(), INTERVAL 141638 MINUTE));
SET @c21_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p21, 8, 0, '同问', DATE_SUB(NOW(), INTERVAL 141641 MINUTE));
SET @c21_1 := LAST_INSERT_ID();

-- 学校食堂几点人少
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '日常吐槽', '学校食堂几点人少', '想避开高峰期，一般几点去食堂比较空？', 2, 1, 8, DATE_SUB(NOW(), INTERVAL 140010 MINUTE), 30.0, '', '食堂');
SET @p22 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p22, 11, 0, '三食堂新开的麻辣烫可以，一食堂二楼的铁板饭份量足。高峰期排队10-15分钟，能错峰尽量错峰。', DATE_SUB(NOW(), INTERVAL 139971 MINUTE));
SET @c22_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p22, 5, 0, '笑死，我也是', DATE_SUB(NOW(), INTERVAL 139876 MINUTE));
SET @c22_1 := LAST_INSERT_ID();

-- 保研需要哪些条件
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u11, '学习交流', '保研需要哪些条件', '想问保研的基本条件，绩点和六级要求是多少？', 4, 134, 821, DATE_SUB(NOW(), INTERVAL 138657 MINUTE), 1257.0, '', '保研');
SET @p23 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p23, 32, 0, '看前6学期绩点排名和六级，425是硬线，科研竞赛能加分。夏令营4-6月报名，9月推免系统填报，名额一般专业前15%。', DATE_SUB(NOW(), INTERVAL 138621 MINUTE));
SET @c23_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p23, 3, 0, '同问，最近也在纠结这个', DATE_SUB(NOW(), INTERVAL 138581 MINUTE));
SET @c23_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p23, 18, 0, '保研核心是绩点排名，其次六级和科研。大三下开始准备夏令营，材料包括成绩单、推荐信、个人陈述。', DATE_SUB(NOW(), INTERVAL 138456 MINUTE));
SET @c23_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u11, @c23_0, 0, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 138277 MINUTE), '李航');

-- 食堂阿姨手抖
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u5, '日常吐槽', '食堂阿姨手抖', '打菜的时候肉都抖没了，只剩汤。', 2, 9, 147, DATE_SUB(NOW(), INTERVAL 137021 MINUTE), 194.0, '', '食堂');
SET @p24 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u13, @p24, 6, 0, '哈哈哈每个学校都有', DATE_SUB(NOW(), INTERVAL 136839 MINUTE));
SET @c24_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u8, @p24, 3, 0, '可以去人少的窗口，阿姨手稳一点', DATE_SUB(NOW(), INTERVAL 136892 MINUTE));
SET @c24_1 := LAST_INSERT_ID();

-- Spring Boot启动报错
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '学习交流', 'Spring Boot启动报错', '项目启动直接失败，日志很长，应该从哪看？', 4, 87, 584, DATE_SUB(NOW(), INTERVAL 135476 MINUTE), 876.0, '', 'SpringBoot');
SET @p25 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p25, 19, 0, '先看日志里的 Caused by。常见是端口被占用、数据库连不上、Redis/Kafka 没启动。按第一个异常往下查，别只看最后一行。', DATE_SUB(NOW(), INTERVAL 135308 MINUTE));
SET @c25_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p25, 2, 0, '这个回答很全，收藏了', DATE_SUB(NOW(), INTERVAL 135313 MINUTE));
SET @c25_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p25, 8, 0, '启动失败九成是依赖没起：MySQL、Redis、Kafka 挨个确认。端口冲突就改 server.port 或杀掉占用进程。', DATE_SUB(NOW(), INTERVAL 135255 MINUTE));
SET @c25_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u6, @c25_0, 0, 0, '好的，我这就去试试。', DATE_SUB(NOW(), INTERVAL 135064 MINUTE), '何洛');

-- 怎么申请借教室
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u10, '院务', '怎么申请借教室', '社团想周末借间教室搞活动，流程是什么，要提前几天？', 2, 1, 12, DATE_SUB(NOW(), INTERVAL 133988 MINUTE), 38.0, '', '教室');
SET @p26 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p26, 17, 0, '企业微信→办事大厅→教室借用，填时间、人数和用途，需要指导老师审批，至少提前3个工作日。晚上18点后的活动还要报备保卫处。', DATE_SUB(NOW(), INTERVAL 133972 MINUTE));
SET @c26_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p26, 4, 0, '帮顶，蹲一个准确答案', DATE_SUB(NOW(), INTERVAL 133781 MINUTE));
SET @c26_1 := LAST_INSERT_ID();

-- 软件工程
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '打分', '软件工程', '小组项目多，给分比较友好。', 2, 26, 738, DATE_SUB(NOW(), INTERVAL 132683 MINUTE), 838.0, '', '李静');
SET @p27 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p27, 4, 0, '这门课我也上过，整体体验和楼主差不多。', DATE_SUB(NOW(), INTERVAL 132588 MINUTE));
SET @c27_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u13, @p27, 2, 0, '中肯', DATE_SUB(NOW(), INTERVAL 132553 MINUTE));
SET @c27_1 := LAST_INSERT_ID();
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u17, 'post', @p27, 5, DATE_SUB(NOW(), INTERVAL 132083 MINUTE), DATE_SUB(NOW(), INTERVAL 132083 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u16, 'post', @p27, 4, DATE_SUB(NOW(), INTERVAL 132083 MINUTE), DATE_SUB(NOW(), INTERVAL 132083 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u5, 'post', @p27, 5, DATE_SUB(NOW(), INTERVAL 132083 MINUTE), DATE_SUB(NOW(), INTERVAL 132083 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u13, 'post', @p27, 4, DATE_SUB(NOW(), INTERVAL 132083 MINUTE), DATE_SUB(NOW(), INTERVAL 132083 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u3, 'post', @p27, 5, DATE_SUB(NOW(), INTERVAL 132083 MINUTE), DATE_SUB(NOW(), INTERVAL 132083 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u17, 'post', @p27, 4, DATE_SUB(NOW(), INTERVAL 132083 MINUTE), DATE_SUB(NOW(), INTERVAL 132083 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u6, 'post', @p27, 5, DATE_SUB(NOW(), INTERVAL 132083 MINUTE), DATE_SUB(NOW(), INTERVAL 132083 MINUTE));

-- Java项目怎么部署到服务器
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u2, '学习交流', 'Java项目怎么部署到服务器', 'Spring Boot 项目想部署到云服务器，一般怎么搞？', 4, 48, 339, DATE_SUB(NOW(), INTERVAL 131589 MINUTE), 518.0, '', '部署');
SET @p28 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p28, 33, 0, '前端 pnpm build 后把 dist 丢给 nginx，后端打 fat jar 用 systemd 托管。注意环境变量、端口和 HTTPS，反向代理别漏了 /api 前缀。', DATE_SUB(NOW(), INTERVAL 131380 MINUTE));
SET @c28_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p28, 4, 0, '蹲一个后续', DATE_SUB(NOW(), INTERVAL 131486 MINUTE));
SET @c28_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p28, 17, 0, '典型部署：nginx 托管前端静态文件并反代 /api 到 127.0.0.1:8080，Spring Boot 用 systemd 起 jar，配置走环境变量。', DATE_SUB(NOW(), INTERVAL 131507 MINUTE));
SET @c28_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u2, @c28_0, 3, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 131218 MINUTE), '何洛');

-- 图书馆座位怎么预约
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u2, '学习交流', '图书馆座位怎么预约', '考研想去图书馆自习，座位是当天约还是提前约？', 4, 73, 534, DATE_SUB(NOW(), INTERVAL 130036 MINUTE), 779.0, '', '图书馆');
SET @p29 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p29, 34, 0, '用「图书馆座位预约」小程序，前一天20:00开放第二天座位。到馆后扫码签到，离开超过30分钟座位会被释放。考研专区需要人脸认证。', DATE_SUB(NOW(), INTERVAL 129849 MINUTE));
SET @c29_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p29, 7, 0, '学到了，感谢分享', DATE_SUB(NOW(), INTERVAL 129838 MINUTE));
SET @c29_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p29, 9, 0, '座位提前一天晚上8点在预约小程序抢，热门位置很快没。签到后临时离开记得点「暂离」，否则30分钟自动释放。', DATE_SUB(NOW(), INTERVAL 129881 MINUTE));
SET @c29_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u2, @c29_0, 0, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 129653 MINUTE), '唐可可');

-- 求推荐学校附近理发店
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '其他', '求推荐学校附近理发店', '想剪个干净利落的短发，学校附近哪家好？', 2, 16, 228, DATE_SUB(NOW(), INTERVAL 128140 MINUTE), 302.0, '', '理发');
SET @p30 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p30, 9, 0, '东门那家不错，25块，跟师傅说清楚要求', DATE_SUB(NOW(), INTERVAL 128009 MINUTE));
SET @c30_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p30, 2, 0, '别去步行街第一家，剪得不行', DATE_SUB(NOW(), INTERVAL 127903 MINUTE));
SET @c30_1 := LAST_INSERT_ID();

-- 校园卡丢了怎么补办
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u0, '院务', '校园卡丢了怎么补办', '昨天在二食堂吃完饭发现校园卡不见了，里面有刚充的200块，急！', 3, 2, 52, DATE_SUB(NOW(), INTERVAL 127228 MINUTE), 90.0, '', '校园卡');
SET @p31 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p31, 31, 0, '第一步先在「校园卡服务」小程序里挂失，避免被别人捡去刷。然后带身份证或学生证到行政楼103一卡通中心补办，工本费20元，现场制卡当天就能拿。', DATE_SUB(NOW(), INTERVAL 127139 MINUTE));
SET @c31_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p31, 5, 0, '同问，我也想知道', DATE_SUB(NOW(), INTERVAL 126993 MINUTE));
SET @c31_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u0, @c31_0, 4, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 126828 MINUTE), '李航');

-- 宿舍水管坏了怎么报修
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u13, '院务', '宿舍水管坏了怎么报修', '卫生间水管漏水，报修应该找谁？大概多久能来？', 2, 19, 467, DATE_SUB(NOW(), INTERVAL 125404 MINUTE), 550.0, '', '报修');
SET @p32 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p32, 14, 0, '企业微信搜「智慧后勤」→在线报修，选楼栋、房间和故障类型。水电急修直接打楼下值班室电话，一般24小时内上门，不要自己拆。', DATE_SUB(NOW(), INTERVAL 125400 MINUTE));
SET @c32_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p32, 1, 0, '帮顶，蹲一个准确答案', DATE_SUB(NOW(), INTERVAL 125361 MINUTE));
SET @c32_1 := LAST_INSERT_ID();

-- 高数补考难吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '学习交流', '高数补考难吗', '高数补考难度怎么样，刷往年题有用吗？', 3, 105, 861, DATE_SUB(NOW(), INTERVAL 124148 MINUTE), 1199.0, '', '高数');
SET @p33 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p33, 18, 0, '高数补考以基础题为主，极限、导数、积分三大块拿下就稳。找老师要复习提纲，别只看难题。', DATE_SUB(NOW(), INTERVAL 124141 MINUTE));
SET @c33_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p33, 7, 0, '这个回答很全，收藏了', DATE_SUB(NOW(), INTERVAL 123969 MINUTE));
SET @c33_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p33, 18, 0, '先申请补考，然后按期末重点复习，往年题重复不少。补考不过就重修，重修成绩单会有标记。', DATE_SUB(NOW(), INTERVAL 124039 MINUTE));
SET @c33_2 := LAST_INSERT_ID();

-- 宿舍报修流程
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '院务', '宿舍报修流程', '宿舍灯坏了，后勤报修在哪提交？', 3, 53, 689, DATE_SUB(NOW(), INTERVAL 122784 MINUTE), 884.0, '', '报修');
SET @p34 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p34, 20, 0, '后勤小程序上报修最快，拍照描述清楚。正常维修1-2天，漏水断电这类急修打值班室电话，师傅当天来。', DATE_SUB(NOW(), INTERVAL 122556 MINUTE));
SET @c34_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p34, 1, 0, '同问，我也想知道', DATE_SUB(NOW(), INTERVAL 122733 MINUTE));
SET @c34_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u15, @c34_0, 3, 0, '明白了，谢谢学长！', DATE_SUB(NOW(), INTERVAL 122358 MINUTE), '王雨桐');

-- 操作系统实验难吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u10, '课程专区', '操作系统实验难吗', '下学期要上操作系统，实验是基于xv6吗？', 3, 29, 816, DATE_SUB(NOW(), INTERVAL 120956 MINUTE), 927.0, '', '王强');
SET @p35 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p35, 26, 0, '实验基于xv6，环境用课程给的Docker镜像。先做lab1系统调用，报告要有代码、运行截图和分析，抄袭查得很严。', DATE_SUB(NOW(), INTERVAL 120794 MINUTE));
SET @c35_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p35, 7, 0, '这个信息很有用', DATE_SUB(NOW(), INTERVAL 120919 MINUTE));
SET @c35_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p35, 15, 0, '操作系统实验是xv6的lab系列，环境配好Docker就不折腾。每个lab先看懂框架再改，报告按模板写清楚。', DATE_SUB(NOW(), INTERVAL 120946 MINUTE));
SET @c35_2 := LAST_INSERT_ID();

-- 找羽毛球搭子
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '其他', '找羽毛球搭子', '每周三晚上体育馆打球，有没有一起的？水平入门。', 3, 3, 33, DATE_SUB(NOW(), INTERVAL 119889 MINUTE), 79.0, '', '羽毛球');
SET @p36 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p36, 5, 0, '我我我，正好想找人打球', DATE_SUB(NOW(), INTERVAL 119727 MINUTE));
SET @c36_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p36, 3, 0, '加一个，周三几点？', DATE_SUB(NOW(), INTERVAL 119678 MINUTE));
SET @c36_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u15, @c36_0, 1, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 119470 MINUTE), '高远');

-- B座和A座是同一栋吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u7, '其他', 'B座和A座是同一栋吗', '上课地点写B203，和A座在一起吗？', 4, 33, 726, DATE_SUB(NOW(), INTERVAL 118404 MINUTE), 850.0, '', '校园');
SET @p37 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p37, 15, 0, '上课地点看楼栋字母：A/B座在图书馆北侧，二教南门方向，三教在东门。提前10分钟出门基本够。', DATE_SUB(NOW(), INTERVAL 118332 MINUTE));
SET @c37_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p37, 1, 0, '学到了', DATE_SUB(NOW(), INTERVAL 118385 MINUTE));
SET @c37_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p37, 7, 0, '主楼A座在图书馆北侧，二教从南门进直走200米。课表里的 A101 就是 A座1层101，B开头是B座。', DATE_SUB(NOW(), INTERVAL 118200 MINUTE));
SET @c37_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u7, @c37_0, 1, 0, '谢谢，信息很及时。', DATE_SUB(NOW(), INTERVAL 118168 MINUTE), '刘思远');

-- 校内兼职怎么找
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u0, '求职招募', '校内兼职怎么找', '想找校内勤工助学，在哪里申请？', 3, 12, 359, DATE_SUB(NOW(), INTERVAL 116998 MINUTE), 419.0, '', '兼职');
SET @p38 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p38, 29, 0, '校内勤工助学在学工系统申请，图书馆、实验室助理都有。校外兼职注意签协议，任何要交押金的都别去。', DATE_SUB(NOW(), INTERVAL 116826 MINUTE));
SET @c38_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p38, 6, 0, '感谢内推信息', DATE_SUB(NOW(), INTERVAL 116955 MINUTE));
SET @c38_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p38, 17, 0, '校内岗位关注学工处通知，按月发补贴。校外家教、门店兼职要确认结算方式，保护个人信息。', DATE_SUB(NOW(), INTERVAL 116794 MINUTE));
SET @c38_2 := LAST_INSERT_ID();

-- MySQL服务启动不了
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u11, '学习交流', 'MySQL服务启动不了', 'MySQL 服务起不来，端口也连不上，怎么排查？', 3, 81, 488, DATE_SUB(NOW(), INTERVAL 115409 MINUTE), 764.0, '', 'MySQL');
SET @p39 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p39, 33, 0, '本地连不上先看服务状态（Windows: net start mysql）；远程连接检查 bind-address=0.0.0.0 和用户的 host 权限。', DATE_SUB(NOW(), INTERVAL 115337 MINUTE));
SET @c39_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p39, 7, 0, '这个回答很全，收藏了', DATE_SUB(NOW(), INTERVAL 115261 MINUTE));
SET @c39_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u11, @c39_0, 2, 0, '好的，我这就去试试。', DATE_SUB(NOW(), INTERVAL 115067 MINUTE), '唐可可');

-- 加几个社团比较合适
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u5, '日常吐槽', '加几个社团比较合适', '社团加太多会不会忙不过来，推荐加几个？', 4, 15, 208, DATE_SUB(NOW(), INTERVAL 113705 MINUTE), 286.0, '', '社团');
SET @p40 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p40, 36, 0, '社团招新集中在开学前两周，可以先加群观察。别一次报太多，时间会不够用。', DATE_SUB(NOW(), INTERVAL 113589 MINUTE));
SET @c40_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p40, 2, 0, '哈哈太真实了', DATE_SUB(NOW(), INTERVAL 113636 MINUTE));
SET @c40_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u8, @p40, 6, 0, '想学技术就找有项目的社团，想放松就选兴趣类。报名后问清楚会费和活动安排。', DATE_SUB(NOW(), INTERVAL 113473 MINUTE));
SET @c40_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u5, @c40_0, 0, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 113493 MINUTE), '孙悦');

-- 学校打印店在哪
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u16, '其他', '学校打印店在哪', '要打印课程论文，哪里打印便宜，能微信支付吗？', 2, 4, 36, DATE_SUB(NOW(), INTERVAL 112586 MINUTE), 60.0, '', '打印');
SET @p41 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p41, 11, 0, '图书馆一楼自助打印0.1元/页，微信扫码；论文装订去东门文印中心，普通装订20元起，半小时可取。', DATE_SUB(NOW(), INTERVAL 112480 MINUTE));
SET @c41_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p41, 5, 0, '帮顶', DATE_SUB(NOW(), INTERVAL 112432 MINUTE));
SET @c41_1 := LAST_INSERT_ID();

-- 高数挂科了怎么办
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u13, '学习交流', '高数挂科了怎么办', '高数期末没过，补考和重修有什么区别？', 4, 21, 231, DATE_SUB(NOW(), INTERVAL 111131 MINUTE), 338.0, '', '高数');
SET @p42 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p42, 21, 0, '补考难度一般低于期末，把往年补考卷刷熟，重点看老师划的范围。补考通过成绩记60，重修会覆盖但标注。', DATE_SUB(NOW(), INTERVAL 111058 MINUTE));
SET @c42_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p42, 8, 0, '学到了，感谢分享', DATE_SUB(NOW(), INTERVAL 110966 MINUTE));
SET @c42_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p42, 19, 0, '高数补考以基础题为主，极限、导数、积分三大块拿下就稳。找老师要复习提纲，别只看难题。', DATE_SUB(NOW(), INTERVAL 110993 MINUTE));
SET @c42_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u13, @c42_0, 3, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 110749 MINUTE), '李航');

-- 转计算机难不难
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u10, '院务', '转计算机难不难', '转计算机专业竞争大吗，需要提前准备什么？', 3, 33, 233, DATE_SUB(NOW(), INTERVAL 109465 MINUTE), 350.0, '', '转专业');
SET @p43 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p43, 22, 0, '5月份教务系统报名，提交成绩单和个人陈述，然后参加学院考核。热门专业基本要专业前10%才稳。', DATE_SUB(NOW(), INTERVAL 109299 MINUTE));
SET @c43_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p43, 4, 0, '收藏了，谢谢', DATE_SUB(NOW(), INTERVAL 109251 MINUTE));
SET @c43_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p43, 18, 0, '大一结束申请，要求绩点排名专业前30%，5月教务处发布通知。流程是网申+笔试+面试，计算机这类热门专业竞争很激烈。', DATE_SUB(NOW(), INTERVAL 109405 MINUTE));
SET @c43_2 := LAST_INSERT_ID();

-- 早八真的起不来
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '日常吐槽', '早八真的起不来', '闹钟响了五遍还是不想起，到教室已经迟到了十分钟，早八到底怎么坚持啊。', 3, 62, 397, DATE_SUB(NOW(), INTERVAL 108471 MINUTE), 608.0, '', '早八');
SET @p44 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p44, 8, 0, '同感，早八是一天中最痛苦的时刻', DATE_SUB(NOW(), INTERVAL 108293 MINUTE));
SET @c44_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p44, 5, 0, '建议前一晚早点睡，我最近11点睡好多了', DATE_SUB(NOW(), INTERVAL 108417 MINUTE));
SET @c44_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u6, @c44_0, 3, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 108063 MINUTE), '徐嘉');

-- XX科技暑期实习内推
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u10, '求职招募', 'XX科技暑期实习内推', '部门在招后端开发实习生，base 杭州，简历发我邮箱。', 2, 80, 452, DATE_SUB(NOW(), INTERVAL 106979 MINUTE), 721.0, '', '内推');
SET @p45 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p45, 3, 0, '已发，感谢', DATE_SUB(NOW(), INTERVAL 106970 MINUTE));
SET @c45_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p45, 2, 0, '请问还招前端吗', DATE_SUB(NOW(), INTERVAL 106951 MINUTE));
SET @c45_1 := LAST_INSERT_ID();

-- 图书馆考研座位怎么申请
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u16, '学习交流', '图书馆考研座位怎么申请', '听说四楼有考研专区，长期座位怎么申请？', 2, 98, 611, DATE_SUB(NOW(), INTERVAL 105175 MINUTE), 926.0, '', '考研');
SET @p46 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p46, 16, 0, '长期座位在图书馆4楼，开学初在系统申请，先到先得。平时空教室也能自习，看门口课表挑没课的。', DATE_SUB(NOW(), INTERVAL 105114 MINUTE));
SET @c46_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p46, 8, 0, '蹲一个后续', DATE_SUB(NOW(), INTERVAL 104960 MINUTE));
SET @c46_1 := LAST_INSERT_ID();

-- 成绩单盖章流程
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u9, '院务', '成绩单盖章流程', '找工作要交成绩单，自助机打的能直接用吗？', 3, 10, 254, DATE_SUB(NOW(), INTERVAL 103745 MINUTE), 317.0, '', '成绩单');
SET @p47 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p47, 13, 0, '普通成绩单用自助机，刷校园卡即打；英文版和盖章版去教务处办。已经毕业的校友走线上申请邮寄。', DATE_SUB(NOW(), INTERVAL 103516 MINUTE));
SET @c47_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p47, 1, 0, '帮顶，蹲一个准确答案', DATE_SUB(NOW(), INTERVAL 103547 MINUTE));
SET @c47_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p47, 18, 0, '行政楼2楼有自助打印机，刷校园卡就能打中英文成绩单，每份2元。出国申请需要盖章版的话去教务处窗口，提前3个工作日。', DATE_SUB(NOW(), INTERVAL 103658 MINUTE));
SET @c47_2 := LAST_INSERT_ID();

-- 简历怎么写项目经历
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u1, '求职招募', '简历怎么写项目经历', '校招简历里的项目经历怎么写才有亮点？', 4, 35, 537, DATE_SUB(NOW(), INTERVAL 102412 MINUTE), 667.0, '', '简历');
SET @p48 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p48, 20, 0, '一页纸，项目用 STAR 法则写：背景、任务、行动、结果，尽量量化。技术栈放显眼位置，不要编造经历。', DATE_SUB(NOW(), INTERVAL 102282 MINUTE));
SET @c48_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p48, 7, 0, '先码住', DATE_SUB(NOW(), INTERVAL 102362 MINUTE));
SET @c48_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p48, 10, 0, '简历突出项目和实习，写清楚你负责什么、用了什么技术、解决了什么问题、有什么数据结果。', DATE_SUB(NOW(), INTERVAL 102350 MINUTE));
SET @c48_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u1, @c48_0, 3, 0, '谢谢，信息很及时。', DATE_SUB(NOW(), INTERVAL 101968 MINUTE), '郑晓琳');

-- 学校快递点在哪
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '其他', '学校快递点在哪', '新生求问，菜鸟驿站在哪，晚上几点关门？', 3, 50, 422, DATE_SUB(NOW(), INTERVAL 101007 MINUTE), 594.0, '', '快递');
SET @p49 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p49, 29, 0, '菜鸟驿站在东门学生服务中心一楼，营业到21:30；顺丰和京东在西门快递柜；大件行李到南门货运点。取件带取件码就行。', DATE_SUB(NOW(), INTERVAL 100877 MINUTE));
SET @c49_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p49, 5, 0, '感谢分享', DATE_SUB(NOW(), INTERVAL 100989 MINUTE));
SET @c49_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p49, 6, 0, '大部分快递在东门菜鸟驿站，凭取件码自助取。顺丰、京东在西门，晚上9点前都能取。', DATE_SUB(NOW(), INTERVAL 100982 MINUTE));
SET @c49_2 := LAST_INSERT_ID();

-- 二手电动车去哪买
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u11, '其他', '二手电动车去哪买', '想买辆二手电动车代步，学校附近哪里靠谱？', 4, 34, 564, DATE_SUB(NOW(), INTERVAL 99773 MINUTE), 700.0, '', '二手');
SET @p50 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p50, 24, 0, '东门二手市场和闲鱼同城都能买，重点看电池健康、刹车和轮胎。校内电动车要上牌，买之前问清楚有没有牌。', DATE_SUB(NOW(), INTERVAL 99676 MINUTE));
SET @c50_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p50, 3, 0, '同问', DATE_SUB(NOW(), INTERVAL 99758 MINUTE));
SET @c50_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p50, 4, 0, '二手电动车建议找认识的人或毕业学长，试骑一圈看续航。电池换新要几百，价格太低要警惕。', DATE_SUB(NOW(), INTERVAL 99557 MINUTE));
SET @c50_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u11, @c50_0, 3, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 99289 MINUTE), '徐嘉');

-- 四六级怎么报名
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u17, '院务', '四六级怎么报名', '学校四六级在哪里报名，照片有什么要求？', 3, 65, 519, DATE_SUB(NOW(), INTERVAL 98319 MINUTE), 739.0, '', '四六级');
SET @p51 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p51, 12, 0, '教务系统报名，照片要求蓝底证件照。四级425才能报六级，口语自愿。缺考会影响下次报名资格。', DATE_SUB(NOW(), INTERVAL 98079 MINUTE));
SET @c51_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p51, 3, 0, '之前也遇到过，关注一下', DATE_SUB(NOW(), INTERVAL 98228 MINUTE));
SET @c51_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p51, 18, 0, '报名在教务系统，缴费后才算成功。照片自己传的要蓝底，不符合会被退回，注意截止时间。', DATE_SUB(NOW(), INTERVAL 98100 MINUTE));
SET @c51_2 := LAST_INSERT_ID();

-- 计组期末考试难吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '课程专区', '计组期末考试难吗', '想问计组期末难度，往年题重复率高不高？', 4, 37, 751, DATE_SUB(NOW(), INTERVAL 96647 MINUTE), 903.0, '', '张伟');
SET @p52 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p52, 36, 0, '组成原理复习先过一遍课件，再刷近三年卷子，Cache和流水线是必考大题。实验别缺，报告分好拿。', DATE_SUB(NOW(), INTERVAL 96416 MINUTE));
SET @c52_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p52, 7, 0, '这个信息很有用', DATE_SUB(NOW(), INTERVAL 96644 MINUTE));
SET @c52_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p52, 7, 0, '重点是数据表示、指令系统、流水线和Cache，往年题重复率高。实验报告占30%，考试前把课后题和三年真题刷一遍。', DATE_SUB(NOW(), INTERVAL 96533 MINUTE));
SET @c52_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u6, @c52_0, 1, 0, '好的，我这就去试试。', DATE_SUB(NOW(), INTERVAL 96326 MINUTE), '李航');

-- 二食堂有什么推荐的
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u13, '日常吐槽', '二食堂有什么推荐的', '二食堂哪些菜比较好吃？性价比高一点的。', 3, 47, 685, DATE_SUB(NOW(), INTERVAL 95629 MINUTE), 854.0, '', '食堂');
SET @p53 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p53, 24, 0, '推荐二食堂二楼的砂锅，一食堂一楼的快餐性价比高。想人少就11:20前或12:30后去。', DATE_SUB(NOW(), INTERVAL 95542 MINUTE));
SET @c53_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p53, 4, 0, '笑死，我也是', DATE_SUB(NOW(), INTERVAL 95476 MINUTE));
SET @c53_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u13, @c53_0, 2, 0, '谢谢，信息很及时。', DATE_SUB(NOW(), INTERVAL 95160 MINUTE), '孙悦');

-- 操作系统实验报告要求
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u12, '课程专区', '操作系统实验报告要求', '实验报告需要写哪些内容，代码要贴吗？', 3, 149, 864, DATE_SUB(NOW(), INTERVAL 93955 MINUTE), 1343.0, '', '王强');
SET @p54 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p54, 34, 0, '实验用xv6，第一次先把环境跑通，lab1加系统调用。遇到编译错误先看Makefile，报告截图一定要有。', DATE_SUB(NOW(), INTERVAL 93934 MINUTE));
SET @c54_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p54, 8, 0, '已收藏', DATE_SUB(NOW(), INTERVAL 93858 MINUTE));
SET @c54_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p54, 11, 0, '实验基于xv6，环境用课程给的Docker镜像。先做lab1系统调用，报告要有代码、运行截图和分析，抄袭查得很严。', DATE_SUB(NOW(), INTERVAL 93900 MINUTE));
SET @c54_2 := LAST_INSERT_ID();

-- 大学生医保报销流程
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u0, '院务', '大学生医保报销流程', '校医院说校外就医能报，需要准备哪些材料？', 4, 2, 13, DATE_SUB(NOW(), INTERVAL 92392 MINUTE), 46.0, '', '医保');
SET @p55 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p55, 34, 0, '校外门诊报销需要发票原件、病历本和费用明细，交到校医院一楼医保窗口，每月中旬集中办理，到账大概一个月。', DATE_SUB(NOW(), INTERVAL 92369 MINUTE));
SET @c55_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p55, 6, 0, '收藏了，谢谢', DATE_SUB(NOW(), INTERVAL 92366 MINUTE));
SET @c55_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p55, 11, 0, '急诊或转诊的校外费用可以报，前提是有转诊单。材料：发票、清单、病历、银行卡复印件，交医保办。普通药店买药不报。', DATE_SUB(NOW(), INTERVAL 92222 MINUTE));
SET @c55_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u0, @c55_0, 1, 0, '谢谢，信息很及时。', DATE_SUB(NOW(), INTERVAL 92092 MINUTE), '王一诺');

-- 主楼A座在哪
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u5, '其他', '主楼A座在哪', '课表写A101，主楼A座怎么走？', 3, 11, 101, DATE_SUB(NOW(), INTERVAL 91293 MINUTE), 162.0, '', '校园');
SET @p56 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p56, 19, 0, '主楼A座在图书馆北侧，二教从南门进直走200米。课表里的 A101 就是 A座1层101，B开头是B座。', DATE_SUB(NOW(), INTERVAL 91168 MINUTE));
SET @c56_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p56, 3, 0, '学到了', DATE_SUB(NOW(), INTERVAL 91146 MINUTE));
SET @c56_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u5, @c56_0, 4, 0, '谢谢，信息很及时。', DATE_SUB(NOW(), INTERVAL 90902 MINUTE), '高远');

-- 买二手电动车注意什么
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u12, '其他', '买二手电动车注意什么', '买二手电动车主要看什么，电池怎么判断？', 4, 6, 55, DATE_SUB(NOW(), INTERVAL 89617 MINUTE), 112.0, '', '二手');
SET @p57 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p57, 21, 0, '二手电动车建议找认识的人或毕业学长，试骑一圈看续航。电池换新要几百，价格太低要警惕。', DATE_SUB(NOW(), INTERVAL 89526 MINUTE));
SET @c57_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p57, 4, 0, '感谢分享', DATE_SUB(NOW(), INTERVAL 89550 MINUTE));
SET @c57_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p57, 20, 0, '闲鱼同城约在校门口试车，检查刹车、灯和电池。学校现在查无牌车，尽量买带牌的。', DATE_SUB(NOW(), INTERVAL 89516 MINUTE));
SET @c57_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u12, @c57_0, 1, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 89368 MINUTE), '刘思远');

-- 能不能force push
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u0, '学习交流', '能不能force push', '本地和远程不一致，直接 force push 可以吗？', 2, 4, 27, DATE_SUB(NOW(), INTERVAL 88451 MINUTE), 53.0, '', 'Git');
SET @p58 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p58, 26, 0, '合并冲突不可怕，改完记得编译测试再提交。团队分支上 force push 会覆盖别人代码，禁止。', DATE_SUB(NOW(), INTERVAL 88249 MINUTE));
SET @c58_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p58, 1, 0, '学到了，感谢分享', DATE_SUB(NOW(), INTERVAL 88409 MINUTE));
SET @c58_1 := LAST_INSERT_ID();

-- MySQL远程连不上
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u10, '学习交流', 'MySQL远程连不上', '服务器上的 MySQL 本地能连，远程连不上。', 4, 1, 23, DATE_SUB(NOW(), INTERVAL 86944 MINUTE), 62.0, '', 'MySQL');
SET @p59 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p59, 11, 0, '报 Access denied 是账号密码或权限问题；Connection refused 是服务没起或端口不对。远程连接还要开防火墙。', DATE_SUB(NOW(), INTERVAL 86843 MINUTE));
SET @c59_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p59, 2, 0, '同问，最近也在纠结这个', DATE_SUB(NOW(), INTERVAL 86740 MINUTE));
SET @c59_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p59, 8, 0, '本地连不上先看服务状态（Windows: net start mysql）；远程连接检查 bind-address=0.0.0.0 和用户的 host 权限。', DATE_SUB(NOW(), INTERVAL 86758 MINUTE));
SET @c59_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u10, @c59_0, 2, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 86466 MINUTE), '王雨桐');

-- 校外看病怎么报销
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u17, '院务', '校外看病怎么报销', '在校外医院看了门诊，发票都在，回学校怎么报销？', 3, 24, 498, DATE_SUB(NOW(), INTERVAL 85651 MINUTE), 603.0, '', '医保');
SET @p60 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p60, 12, 0, '在校医院看病直接刷医保卡结算。校外就医要先自费，保留发票、费用清单和病历，每月10-20日到校医院医保办手工报销。', DATE_SUB(NOW(), INTERVAL 85580 MINUTE));
SET @c60_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p60, 1, 0, '同问，我也想知道', DATE_SUB(NOW(), INTERVAL 85535 MINUTE));
SET @c60_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p60, 16, 0, '校外门诊报销需要发票原件、病历本和费用明细，交到校医院一楼医保窗口，每月中旬集中办理，到账大概一个月。', DATE_SUB(NOW(), INTERVAL 85477 MINUTE));
SET @c60_2 := LAST_INSERT_ID();

-- 宿舍网老是掉线
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u11, '其他', '宿舍网老是掉线', '宿舍校园网隔一会儿就掉，需要重新认证，怎么解决？', 2, 46, 474, DATE_SUB(NOW(), INTERVAL 84628 MINUTE), 638.0, '', '校园网');
SET @p61 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p61, 33, 0, '宿舍网和教学区网是同一套账号。连不上先忘记网络重连，还不行就在自助服务页重置密码或报修。', DATE_SUB(NOW(), INTERVAL 84574 MINUTE));
SET @c61_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p61, 4, 0, '感谢分享', DATE_SUB(NOW(), INTERVAL 84502 MINUTE));
SET @c61_1 := LAST_INSERT_ID();

-- MySQL连接失败
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u9, '学习交流', 'MySQL连接失败', '本地项目连 MySQL 报 Access denied，密码没错。', 4, 2, 50, DATE_SUB(NOW(), INTERVAL 83201 MINUTE), 86.0, '', 'MySQL');
SET @p62 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p62, 30, 0, '先确认 MySQL 服务在跑、端口3306通不通，再检查账号密码和库名。远程连不上要改 bind-address 并给用户授权。', DATE_SUB(NOW(), INTERVAL 83175 MINUTE));
SET @c62_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p62, 7, 0, '这个回答很全，收藏了', DATE_SUB(NOW(), INTERVAL 83108 MINUTE));
SET @c62_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p62, 13, 0, '报 Access denied 是账号密码或权限问题；Connection refused 是服务没起或端口不对。远程连接还要开防火墙。', DATE_SUB(NOW(), INTERVAL 83146 MINUTE));
SET @c62_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u9, @c62_0, 4, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 82789 MINUTE), '王雨桐');

-- 教材版本怎么确认
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u16, '其他', '教材版本怎么确认', '买二手书怕版本不对，怎么确认是老师指定的那版？', 4, 6, 58, DATE_SUB(NOW(), INTERVAL 81719 MINUTE), 118.0, '', '教材');
SET @p63 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p63, 8, 0, '学长学姐群、闲鱼、教材科门口都行，价格一般三到五折。买之前对一下ISBN和版次。', DATE_SUB(NOW(), INTERVAL 81645 MINUTE));
SET @c63_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p63, 8, 0, '帮顶', DATE_SUB(NOW(), INTERVAL 81570 MINUTE));
SET @c63_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p63, 17, 0, '专业课教材建议买二手，公共课用电子版也行。开学前在群里问，毕业季最便宜。', DATE_SUB(NOW(), INTERVAL 81566 MINUTE));
SET @c63_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u16, @c63_0, 2, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 81241 MINUTE), '徐嘉');

-- 抢课有什么技巧
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u2, '课程专区', '抢课有什么技巧', '选课总抢不过别人，有没有什么技巧？', 3, 32, 221, DATE_SUB(NOW(), INTERVAL 80405 MINUTE), 337.0, '', '选课');
SET @p64 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p64, 20, 0, '抢课前把课程代码准备好，开放瞬间提交。选不上先加候选，开学第一周盯紧退课名额。', DATE_SUB(NOW(), INTERVAL 80179 MINUTE));
SET @c64_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p64, 6, 0, '这个信息很有用', DATE_SUB(NOW(), INTERVAL 80175 MINUTE));
SET @c64_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p64, 8, 0, '教务系统整点开放，提前10分钟登录，用校园网更稳。热门课拼手速，没抢到就等第一周退补选，会有人退。', DATE_SUB(NOW(), INTERVAL 80275 MINUTE));
SET @c64_2 := LAST_INSERT_ID();

-- 面试项目怎么讲
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u9, '求职招募', '面试项目怎么讲', '面试官问项目难点，应该怎么回答？', 3, 49, 316, DATE_SUB(NOW(), INTERVAL 79024 MINUTE), 489.0, '', '面试');
SET @p65 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p65, 35, 0, '一面八股+算法，二面项目深挖。把简历上的每个技术点都准备到能追问三层，算法刷剑指Offer和Hot100。', DATE_SUB(NOW(), INTERVAL 78830 MINUTE));
SET @c65_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p65, 4, 0, '先码住', DATE_SUB(NOW(), INTERVAL 78992 MINUTE));
SET @c65_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p65, 18, 0, '面试前把项目复盘一遍：为什么这么设计、遇到什么问题、怎么解决。八股别死背，结合项目讲。', DATE_SUB(NOW(), INTERVAL 78900 MINUTE));
SET @c65_2 := LAST_INSERT_ID();

-- 补考怎么申请
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u3, '院务', '补考怎么申请', '上学期高数挂了，补考在哪里申请？截止到什么时候？', 4, 51, 374, DATE_SUB(NOW(), INTERVAL 77704 MINUTE), 561.0, '', '考试');
SET @p66 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p66, 31, 0, '成绩出来后一周内登录教务系统，在「考试报名→补考申请」里提交，逾期只能找学院教务办。补考一般安排在开学第2-3周，时间和考场在教务系统查。', DATE_SUB(NOW(), INTERVAL 77578 MINUTE));
SET @c66_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p66, 3, 0, '帮顶，蹲一个准确答案', DATE_SUB(NOW(), INTERVAL 77587 MINUTE));
SET @c66_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p66, 15, 0, '教务系统→考试报名→补考申请，成绩公布后7天内操作。注意补考没有平时分，卷面必须及格。具体安排开学第二周会在学院群通知。', DATE_SUB(NOW(), INTERVAL 77578 MINUTE));
SET @c66_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u3, @c66_0, 0, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 77266 MINUTE), '王雨桐');

-- 彩印和装订求推荐
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u0, '其他', '彩印和装订求推荐', '需要彩印几份材料并装订，东门那家可以吗？', 2, 66, 780, DATE_SUB(NOW(), INTERVAL 75973 MINUTE), 1007.0, '', '打印');
SET @p67 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p67, 29, 0, '自助打印机在图书馆一楼和教学楼A座大厅都有，刷校园卡或微信。论文装订提前一天送过去更稳。', DATE_SUB(NOW(), INTERVAL 75886 MINUTE));
SET @c67_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p67, 8, 0, '学到了', DATE_SUB(NOW(), INTERVAL 75808 MINUTE));
SET @c67_1 := LAST_INSERT_ID();

-- 校园网连不上怎么办
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u9, '其他', '校园网连不上怎么办', '连上校园网但认证失败，一直提示密码错误。', 4, 3, 32, DATE_SUB(NOW(), INTERVAL 74490 MINUTE), 75.0, '', '校园网');
SET @p68 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p68, 28, 0, '连 SDU-EDU，账号是学号，初始密码是身份证后6位。认证失败先在自助服务页解绑设备再重连，最多同时在线3台。', DATE_SUB(NOW(), INTERVAL 74253 MINUTE));
SET @c68_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p68, 3, 0, '感谢分享', DATE_SUB(NOW(), INTERVAL 74364 MINUTE));
SET @c68_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p68, 19, 0, '校园网账号就是学号，密码初始为身份证后六位，改过的用新密码。掉线一般是设备数超了，去自助服务解绑。', DATE_SUB(NOW(), INTERVAL 74321 MINUTE));
SET @c68_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u9, @c68_0, 4, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 74091 MINUTE), '唐可可');

-- 校医院几点开门
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u1, '院务', '校医院几点开门', '晚上发烧能去校医院吗？门诊时间是几点？', 3, 41, 281, DATE_SUB(NOW(), INTERVAL 73276 MINUTE), 428.0, '', '校医院');
SET @p69 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p69, 10, 0, '门诊8:00-17:30，晚上有急诊到21:00。感冒发烧先挂内科，学生医保直接结算。需要转诊找医生开转诊单。', DATE_SUB(NOW(), INTERVAL 73186 MINUTE));
SET @c69_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p69, 4, 0, '帮顶，蹲一个准确答案', DATE_SUB(NOW(), INTERVAL 73109 MINUTE));
SET @c69_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u1, @c69_0, 4, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 72797 MINUTE), '孙悦');

-- 保修期电脑去哪修
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u9, '其他', '保修期电脑去哪修', '还在保修期，去学校维修点还是官方售后？', 2, 28, 339, DATE_SUB(NOW(), INTERVAL 71833 MINUTE), 441.0, '', '电脑');
SET @p70 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p70, 22, 0, '电脑进水、黑屏先断电，送信息楼B座检测。清灰、换硬盘这些学生价不贵，数据记得先备份。', DATE_SUB(NOW(), INTERVAL 71639 MINUTE));
SET @c70_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p70, 7, 0, '帮顶', DATE_SUB(NOW(), INTERVAL 71636 MINUTE));
SET @c70_1 := LAST_INSERT_ID();

-- 挂科了补考流程
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u5, '院务', '挂科了补考流程', '成绩刚出，有一门没过，想问补考申请流程和时间安排。', 3, 2, 15, DATE_SUB(NOW(), INTERVAL 70990 MINUTE), 58.0, '', '考试');
SET @p71 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p71, 18, 0, '教务系统→考试报名→补考申请，成绩公布后7天内操作。注意补考没有平时分，卷面必须及格。具体安排开学第二周会在学院群通知。', DATE_SUB(NOW(), INTERVAL 70888 MINUTE));
SET @c71_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p71, 1, 0, '收藏了，谢谢', DATE_SUB(NOW(), INTERVAL 70832 MINUTE));
SET @c71_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u5, @c71_0, 0, 0, '明白了，谢谢学长！', DATE_SUB(NOW(), INTERVAL 70622 MINUTE), '李航');

-- 晚上哪里可以自习
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u17, '学习交流', '晚上哪里可以自习', '教学楼晚上开放到几点，有没有空教室自习？', 3, 6, 54, DATE_SUB(NOW(), INTERVAL 69184 MINUTE), 103.0, '', '考研');
SET @p72 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p72, 15, 0, '图书馆考研区要抢，建议9月初就关注通知。晚上二教三四楼的空教室人少，22:30关门。', DATE_SUB(NOW(), INTERVAL 69000 MINUTE));
SET @c72_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p72, 6, 0, '这个回答很全，收藏了', DATE_SUB(NOW(), INTERVAL 69101 MINUTE));
SET @c72_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p72, 11, 0, '图书馆4楼有考研专区，9月开放长期座位申请，需要预约。教学楼空教室晚上开到23点，考试周会延长。', DATE_SUB(NOW(), INTERVAL 68978 MINUTE));
SET @c72_2 := LAST_INSERT_ID();

-- 实习什么时候投简历
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '求职招募', '实习什么时候投简历', '暑期实习一般几月份开始投，渠道有哪些？', 3, 49, 387, DATE_SUB(NOW(), INTERVAL 68048 MINUTE), 570.0, '', '实习');
SET @p73 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p73, 22, 0, '日常实习全年都有，暑期实习要提前半年准备。投递渠道：官网、牛客、学长内推，简历投出去后同步刷题。', DATE_SUB(NOW(), INTERVAL 67930 MINUTE));
SET @c73_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p73, 1, 0, '有帮助，谢谢', DATE_SUB(NOW(), INTERVAL 67885 MINUTE));
SET @c73_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p73, 11, 0, '找实习先定方向，Java后端就准备JVM、并发、MySQL、Redis和项目。3月开始投，别等暑假。', DATE_SUB(NOW(), INTERVAL 68000 MINUTE));
SET @c73_2 := LAST_INSERT_ID();

-- 计算机组成原理怎么复习
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u3, '课程专区', '计算机组成原理怎么复习', '期末快到了，计组重点是什么，怎么复习效率高？', 2, 16, 139, DATE_SUB(NOW(), INTERVAL 66435 MINUTE), 215.0, '', '张伟');
SET @p74 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p74, 33, 0, '重点是数据表示、指令系统、流水线和Cache，往年题重复率高。实验报告占30%，考试前把课后题和三年真题刷一遍。', DATE_SUB(NOW(), INTERVAL 66290 MINUTE));
SET @c74_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p74, 4, 0, '这个信息很有用', DATE_SUB(NOW(), INTERVAL 66286 MINUTE));
SET @c74_1 := LAST_INSERT_ID();

-- 图书馆四楼又有人占座
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u13, '日常吐槽', '图书馆四楼又有人占座', '放一本书占一整天，人一直不来，真的很影响别人。', 3, 48, 555, DATE_SUB(NOW(), INTERVAL 65623 MINUTE), 723.0, '', '图书馆');
SET @p75 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p75, 6, 0, '可以跟管理员反映', DATE_SUB(NOW(), INTERVAL 65562 MINUTE));
SET @c75_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p75, 8, 0, '预约系统签到超过30分钟会释放座位', DATE_SUB(NOW(), INTERVAL 65530 MINUTE));
SET @c75_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u13, @c75_0, 2, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 65208 MINUTE), '孙悦');

-- 数据库怎么复习
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u9, '课程专区', '数据库怎么复习', '数据库知识点太杂，求一个复习重点。', 3, 3, 33, DATE_SUB(NOW(), INTERVAL 64292 MINUTE), 66.0, '', '李静');
SET @p76 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p76, 17, 0, '数据库重点：SQL、关系代数、范式、事务和索引。往年题一定要刷，课设占比高，提前两周开始。', DATE_SUB(NOW(), INTERVAL 64221 MINUTE));
SET @c76_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p76, 8, 0, '感谢学长', DATE_SUB(NOW(), INTERVAL 64187 MINUTE));
SET @c76_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p76, 11, 0, 'SQL大题必考，还有范式分解和事务隔离级别。复习以PPT和近三年卷子为主，实验课设占40%，别拖到最后。', DATE_SUB(NOW(), INTERVAL 64176 MINUTE));
SET @c76_2 := LAST_INSERT_ID();

-- 顺丰快递去哪取
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u7, '其他', '顺丰快递去哪取', '顺丰和京东的快递是送到哪个点？', 4, 2, 26, DATE_SUB(NOW(), INTERVAL 62955 MINUTE), 68.0, '', '快递');
SET @p77 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p77, 26, 0, '大部分快递在东门菜鸟驿站，凭取件码自助取。顺丰、京东在西门，晚上9点前都能取。', DATE_SUB(NOW(), INTERVAL 62796 MINUTE));
SET @c77_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p77, 2, 0, '感谢分享', DATE_SUB(NOW(), INTERVAL 62806 MINUTE));
SET @c77_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p77, 13, 0, '东门学生服务中心一楼是菜鸟驿站，21:30关门。大件会送到南门，收到短信再过去。', DATE_SUB(NOW(), INTERVAL 62935 MINUTE));
SET @c77_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u7, @c77_0, 2, 0, '明白了，谢谢学长！', DATE_SUB(NOW(), INTERVAL 62699 MINUTE), '徐嘉');

-- 转诊单怎么开
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u3, '院务', '转诊单怎么开', '想去大医院看病，校医院给开转诊单吗？', 2, 52, 483, DATE_SUB(NOW(), INTERVAL 61635 MINUTE), 669.0, '', '校医院');
SET @p78 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p78, 21, 0, '看病带校园卡，挂号1元。小病校医院就行，需要去大医院让医生开转诊单，后面才能报销。', DATE_SUB(NOW(), INTERVAL 61529 MINUTE));
SET @c78_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p78, 4, 0, '帮顶，蹲一个准确答案', DATE_SUB(NOW(), INTERVAL 61535 MINUTE));
SET @c78_1 := LAST_INSERT_ID();

-- 前端后端怎么部署
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u3, '学习交流', '前端后端怎么部署', 'Vue 前端和 Spring Boot 后端部署到同一台服务器怎么配？', 3, 0, 17, DATE_SUB(NOW(), INTERVAL 59887 MINUTE), 55.0, '', '部署');
SET @p79 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p79, 35, 0, '典型部署：nginx 托管前端静态文件并反代 /api 到 127.0.0.1:8080，Spring Boot 用 systemd 起 jar，配置走环境变量。', DATE_SUB(NOW(), INTERVAL 59857 MINUTE));
SET @c79_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p79, 8, 0, '这个回答很全，收藏了', DATE_SUB(NOW(), INTERVAL 59877 MINUTE));
SET @c79_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p79, 15, 0, '服务器上先装 JDK 和 nginx，前端构建上传，后端 java -jar 或 systemd。数据库密码用环境变量，别写进 jar。', DATE_SUB(NOW(), INTERVAL 59873 MINUTE));
SET @c79_2 := LAST_INSERT_ID();

-- 借教室需要什么手续
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u11, '院务', '借教室需要什么手续', '班级想借教室开班会，需要找谁审批？', 4, 1, 18, DATE_SUB(NOW(), INTERVAL 58603 MINUTE), 60.0, '', '教室');
SET @p80 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p80, 31, 0, '在办事大厅提交教室借用申请，审批流是指导老师→学院→教务处。提前3天以上，周一到周五办理。社团活动要附活动方案。', DATE_SUB(NOW(), INTERVAL 58505 MINUTE));
SET @c80_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p80, 3, 0, '同问，我也想知道', DATE_SUB(NOW(), INTERVAL 58425 MINUTE));
SET @c80_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p80, 16, 0, '借教室走企业微信办事大厅，选空闲教室，填写用途，找指导老师批。临时借用基本批不下来，至少提前3天。', DATE_SUB(NOW(), INTERVAL 58501 MINUTE));
SET @c80_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u11, @c80_0, 3, 0, '好的，我这就去试试。', DATE_SUB(NOW(), INTERVAL 58200 MINUTE), '李航');

-- 高等数学
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u10, '打分', '高等数学', '作业不少，认真刷题能过。', 2, 30, 518, DATE_SUB(NOW(), INTERVAL 57487 MINUTE), 637.0, '', '张伟');
SET @p81 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p81, 9, 0, '这门课我也上过，整体体验和楼主差不多。', DATE_SUB(NOW(), INTERVAL 57348 MINUTE));
SET @c81_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p81, 2, 0, '同感', DATE_SUB(NOW(), INTERVAL 57295 MINUTE));
SET @c81_1 := LAST_INSERT_ID();
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u11, 'post', @p81, 3, DATE_SUB(NOW(), INTERVAL 56887 MINUTE), DATE_SUB(NOW(), INTERVAL 56887 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u17, 'post', @p81, 3, DATE_SUB(NOW(), INTERVAL 56887 MINUTE), DATE_SUB(NOW(), INTERVAL 56887 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u9, 'post', @p81, 4, DATE_SUB(NOW(), INTERVAL 56887 MINUTE), DATE_SUB(NOW(), INTERVAL 56887 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u2, 'post', @p81, 2, DATE_SUB(NOW(), INTERVAL 56887 MINUTE), DATE_SUB(NOW(), INTERVAL 56887 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u3, 'post', @p81, 3, DATE_SUB(NOW(), INTERVAL 56887 MINUTE), DATE_SUB(NOW(), INTERVAL 56887 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u0, 'post', @p81, 4, DATE_SUB(NOW(), INTERVAL 56887 MINUTE), DATE_SUB(NOW(), INTERVAL 56887 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u8, 'post', @p81, 3, DATE_SUB(NOW(), INTERVAL 56887 MINUTE), DATE_SUB(NOW(), INTERVAL 56887 MINUTE));

-- 二教怎么走
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '其他', '二教怎么走', '从南门进学校，去二教怎么走最近？', 4, 36, 407, DATE_SUB(NOW(), INTERVAL 56276 MINUTE), 545.0, '', '校园');
SET @p82 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p82, 22, 0, 'A座和B座挨着，都在图书馆北边；二教在南门主干道尽头。楼里都有楼层指引，找不到问保安最快。', DATE_SUB(NOW(), INTERVAL 56049 MINUTE));
SET @c82_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p82, 1, 0, '学到了', DATE_SUB(NOW(), INTERVAL 56234 MINUTE));
SET @c82_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p82, 18, 0, '上课地点看楼栋字母：A/B座在图书馆北侧，二教南门方向，三教在东门。提前10分钟出门基本够。', DATE_SUB(NOW(), INTERVAL 56128 MINUTE));
SET @c82_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u6, @c82_0, 4, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 56038 MINUTE), '唐可可');

-- 面试算法要刷多少题
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u10, '求职招募', '面试算法要刷多少题', '校招面试算法大概要刷多少题，刷哪些？', 2, 1, 30, DATE_SUB(NOW(), INTERVAL 54522 MINUTE), 50.0, '', '面试');
SET @p83 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p83, 17, 0, '面试前把项目复盘一遍：为什么这么设计、遇到什么问题、怎么解决。八股别死背，结合项目讲。', DATE_SUB(NOW(), INTERVAL 54497 MINUTE));
SET @c83_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p83, 6, 0, '感谢内推信息', DATE_SUB(NOW(), INTERVAL 54323 MINUTE));
SET @c83_1 := LAST_INSERT_ID();

-- 数据库期末重点
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u7, '课程专区', '数据库期末重点', '数据库原理期末考什么，SQL题多吗？', 4, 57, 646, DATE_SUB(NOW(), INTERVAL 53578 MINUTE), 846.0, '', '李静');
SET @p84 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p84, 24, 0, 'SQL大题必考，还有范式分解和事务隔离级别。复习以PPT和近三年卷子为主，实验课设占40%，别拖到最后。', DATE_SUB(NOW(), INTERVAL 53460 MINUTE));
SET @c84_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p84, 5, 0, '已收藏', DATE_SUB(NOW(), INTERVAL 53404 MINUTE));
SET @c84_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p84, 12, 0, '李老师考试偏基础，SQL写查询、ER图转关系模式、范式判断是重点。把实验报告整理好，课设认真做能拉分。', DATE_SUB(NOW(), INTERVAL 53408 MINUTE));
SET @c84_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u7, @c84_0, 4, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 53223 MINUTE), '唐可可');

-- 计组实验报告占多少分
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u5, '课程专区', '计组实验报告占多少分', '张老师的计组实验报告占比大吗，要认真写吗？', 4, 4, 28, DATE_SUB(NOW(), INTERVAL 52253 MINUTE), 66.0, '', '张伟');
SET @p85 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p85, 22, 0, '张老师的课重点在流水线和存储层次，计算题必考。建议把PPT例题做熟，实验报告认真写，占分不少。', DATE_SUB(NOW(), INTERVAL 52073 MINUTE));
SET @c85_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p85, 1, 0, '已收藏', DATE_SUB(NOW(), INTERVAL 52034 MINUTE));
SET @c85_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p85, 13, 0, '组成原理复习先过一遍课件，再刷近三年卷子，Cache和流水线是必考大题。实验别缺，报告分好拿。', DATE_SUB(NOW(), INTERVAL 52144 MINUTE));
SET @c85_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u5, @c85_0, 1, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 51927 MINUTE), '李航');

-- 数据库原理
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '打分', '数据库原理', '课程内容扎实，实验和课设都很锻炼人。', 2, 18, 481, DATE_SUB(NOW(), INTERVAL 50767 MINUTE), 547.0, '', '李静');
SET @p86 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p86, 12, 0, '这门课我也上过，整体体验和楼主差不多。', DATE_SUB(NOW(), INTERVAL 50758 MINUTE));
SET @c86_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u6, @p86, 1, 0, '同感', DATE_SUB(NOW(), INTERVAL 50554 MINUTE));
SET @c86_1 := LAST_INSERT_ID();
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u9, 'post', @p86, 5, DATE_SUB(NOW(), INTERVAL 50167 MINUTE), DATE_SUB(NOW(), INTERVAL 50167 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u17, 'post', @p86, 5, DATE_SUB(NOW(), INTERVAL 50167 MINUTE), DATE_SUB(NOW(), INTERVAL 50167 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u11, 'post', @p86, 4, DATE_SUB(NOW(), INTERVAL 50167 MINUTE), DATE_SUB(NOW(), INTERVAL 50167 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u6, 'post', @p86, 5, DATE_SUB(NOW(), INTERVAL 50167 MINUTE), DATE_SUB(NOW(), INTERVAL 50167 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u11, 'post', @p86, 4, DATE_SUB(NOW(), INTERVAL 50167 MINUTE), DATE_SUB(NOW(), INTERVAL 50167 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u0, 'post', @p86, 5, DATE_SUB(NOW(), INTERVAL 50167 MINUTE), DATE_SUB(NOW(), INTERVAL 50167 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u2, 'post', @p86, 4, DATE_SUB(NOW(), INTERVAL 50167 MINUTE), DATE_SUB(NOW(), INTERVAL 50167 MINUTE));

-- 重修会覆盖成绩吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u16, '学习交流', '重修会覆盖成绩吗', '高数重修后成绩单怎么显示，影响保研吗？', 3, 31, 402, DATE_SUB(NOW(), INTERVAL 49380 MINUTE), 533.0, '', '高数');
SET @p87 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p87, 11, 0, '先申请补考，然后按期末重点复习，往年题重复不少。补考不过就重修，重修成绩单会有标记。', DATE_SUB(NOW(), INTERVAL 49325 MINUTE));
SET @c87_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p87, 7, 0, '蹲一个后续', DATE_SUB(NOW(), INTERVAL 49182 MINUTE));
SET @c87_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p87, 20, 0, '补考难度一般低于期末，把往年补考卷刷熟，重点看老师划的范围。补考通过成绩记60，重修会覆盖但标注。', DATE_SUB(NOW(), INTERVAL 49164 MINUTE));
SET @c87_2 := LAST_INSERT_ID();

-- 考研专区座位申请
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u5, '学习交流', '考研专区座位申请', '图书馆四楼考研专区的长期座位怎么申请？', 2, 1, 13, DATE_SUB(NOW(), INTERVAL 48462 MINUTE), 41.0, '', '图书馆');
SET @p88 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p88, 18, 0, '图书馆座位在微信小程序预约，刷脸或扫码签到。违约3次会停用预约权限一周，注意别爽约。', DATE_SUB(NOW(), INTERVAL 48328 MINUTE));
SET @c88_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p88, 1, 0, '学到了，感谢分享', DATE_SUB(NOW(), INTERVAL 48276 MINUTE));
SET @c88_1 := LAST_INSERT_ID();

-- 电脑坏了去哪修
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u7, '其他', '电脑坏了去哪修', '笔记本风扇声音很大，学校哪里能修？', 4, 106, 648, DATE_SUB(NOW(), INTERVAL 46718 MINUTE), 1009.0, '', '电脑');
SET @p89 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u17, @p89, 20, 0, '信息楼B座一楼有维修点，学生价清灰换硅脂50，重装系统30。先让师傅检测再决定，别急着换硬件。', DATE_SUB(NOW(), INTERVAL 46620 MINUTE));
SET @c89_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p89, 7, 0, '学到了', DATE_SUB(NOW(), INTERVAL 46634 MINUTE));
SET @c89_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p89, 20, 0, '小问题先去信息楼B座维修点，价格透明。保修期内的品牌机建议走官方售后，别自己拆。', DATE_SUB(NOW(), INTERVAL 46508 MINUTE));
SET @c89_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u7, @c89_0, 1, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 46453 MINUTE), '高远');

-- 怎么提高编程能力
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '学习交流', '怎么提高编程能力', '大一，感觉只会做课后题，不知道怎么提升。', 3, 86, 651, DATE_SUB(NOW(), INTERVAL 45845 MINUTE), 928.0, '', '编程');
SET @p90 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u1, @p90, 20, 0, '多做项目，先把一个完整的 Web 应用做出来', DATE_SUB(NOW(), INTERVAL 45715 MINUTE));
SET @c90_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p90, 10, 0, '刷题加看源码，坚持写博客', DATE_SUB(NOW(), INTERVAL 45642 MINUTE));
SET @c90_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u6, @c90_0, 0, 0, '太详细了，感谢！', DATE_SUB(NOW(), INTERVAL 45404 MINUTE), '陈子昂');

-- 图书馆占座会被清吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u3, '学习交流', '图书馆占座会被清吗', '离开一会儿座位会被释放吗？怎么算违规？', 4, 61, 480, DATE_SUB(NOW(), INTERVAL 44697 MINUTE), 689.0, '', '图书馆');
SET @p91 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p91, 32, 0, '座位提前一天晚上8点在预约小程序抢，热门位置很快没。签到后临时离开记得点「暂离」，否则30分钟自动释放。', DATE_SUB(NOW(), INTERVAL 44471 MINUTE));
SET @c91_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p91, 5, 0, '学到了，感谢分享', DATE_SUB(NOW(), INTERVAL 44521 MINUTE));
SET @c91_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p91, 4, 0, '图书馆座位在微信小程序预约，刷脸或扫码签到。违约3次会停用预约权限一周，注意别爽约。', DATE_SUB(NOW(), INTERVAL 44599 MINUTE));
SET @c91_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u3, @c91_0, 2, 0, '好的，我这就去试试。', DATE_SUB(NOW(), INTERVAL 44225 MINUTE), '何洛');

-- 转专业什么时候申请
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u9, '院务', '转专业什么时候申请', '大几可以转专业，一般什么时候发通知？', 3, 1, 12, DATE_SUB(NOW(), INTERVAL 43349 MINUTE), 52.0, '', '转专业');
SET @p92 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p92, 34, 0, '转专业一般在大一末，绩点前30%才有资格报名，具体名额看当年通知。想转计算机建议提前刷编程题，面试会问。', DATE_SUB(NOW(), INTERVAL 43301 MINUTE));
SET @c92_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p92, 2, 0, '帮顶，蹲一个准确答案', DATE_SUB(NOW(), INTERVAL 43198 MINUTE));
SET @c92_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p92, 19, 0, '5月份教务系统报名，提交成绩单和个人陈述，然后参加学院考核。热门专业基本要专业前10%才稳。', DATE_SUB(NOW(), INTERVAL 43335 MINUTE));
SET @c92_2 := LAST_INSERT_ID();

-- 保研名额怎么算
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u12, '学习交流', '保研名额怎么算', '保研名额是按专业排名吗，一般前百分之多少？', 3, 9, 168, DATE_SUB(NOW(), INTERVAL 41916 MINUTE), 215.0, '', '保研');
SET @p93 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p93, 11, 0, '保研核心是绩点排名，其次六级和科研。大三下开始准备夏令营，材料包括成绩单、推荐信、个人陈述。', DATE_SUB(NOW(), INTERVAL 41859 MINUTE));
SET @c93_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p93, 7, 0, '蹲一个后续', DATE_SUB(NOW(), INTERVAL 41805 MINUTE));
SET @c93_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p93, 11, 0, '想保研先把绩点稳住，六级尽早过。4-6月投夏令营，9月如果没拿到优营再参加预推免。', DATE_SUB(NOW(), INTERVAL 41907 MINUTE));
SET @c93_2 := LAST_INSERT_ID();

-- 校园里那只橘猫
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '其他', '校园里那只橘猫', '每天在图书馆门口晒太阳，谁在喂它？', 2, 31, 737, DATE_SUB(NOW(), INTERVAL 40746 MINUTE), 846.0, '', '校园');
SET @p94 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p94, 4, 0, '好像是宿管阿姨在喂', DATE_SUB(NOW(), INTERVAL 40686 MINUTE));
SET @c94_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p94, 2, 0, '别喂太多，之前贴了告示', DATE_SUB(NOW(), INTERVAL 40508 MINUTE));
SET @c94_1 := LAST_INSERT_ID();

-- 医保报销时间
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u1, '院务', '医保报销时间', '医保报销是每个月几号办理？多久到账？', 4, 1, 40, DATE_SUB(NOW(), INTERVAL 39243 MINUTE), 67.0, '', '医保');
SET @p95 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p95, 26, 0, '急诊或转诊的校外费用可以报，前提是有转诊单。材料：发票、清单、病历、银行卡复印件，交医保办。普通药店买药不报。', DATE_SUB(NOW(), INTERVAL 39041 MINUTE));
SET @c95_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p95, 6, 0, '收藏了，谢谢', DATE_SUB(NOW(), INTERVAL 39062 MINUTE));
SET @c95_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p95, 8, 0, '在校医院看病直接刷医保卡结算。校外就医要先自费，保留发票、费用清单和病历，每月10-20日到校医院医保办手工报销。', DATE_SUB(NOW(), INTERVAL 39216 MINUTE));
SET @c95_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u1, @c95_0, 4, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 38972 MINUTE), '孙悦');

-- 校医院看病流程
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u2, '院务', '校医院看病流程', '第一次去校医院，挂号流程是什么，能用医保吗？', 2, 2, 59, DATE_SUB(NOW(), INTERVAL 38252 MINUTE), 95.0, '', '校医院');
SET @p96 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p96, 19, 0, '校医院一楼挂号，普通门诊全天，急诊晚上到9点。发烧先去分诊台测体温，再挂内科。', DATE_SUB(NOW(), INTERVAL 38091 MINUTE));
SET @c96_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p96, 8, 0, '之前也遇到过，关注一下', DATE_SUB(NOW(), INTERVAL 38059 MINUTE));
SET @c96_1 := LAST_INSERT_ID();

-- 数据库课设占比
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '课程专区', '数据库课设占比', '李老师数据库课设占多少分，需要做哪些内容？', 2, 3, 60, DATE_SUB(NOW(), INTERVAL 36853 MINUTE), 94.0, '', '李静');
SET @p97 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p97, 36, 0, '李老师考试偏基础，SQL写查询、ER图转关系模式、范式判断是重点。把实验报告整理好，课设认真做能拉分。', DATE_SUB(NOW(), INTERVAL 36653 MINUTE));
SET @c97_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p97, 1, 0, '已收藏', DATE_SUB(NOW(), INTERVAL 36641 MINUTE));
SET @c97_1 := LAST_INSERT_ID();

-- 体育
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '打分', '体育', '选课要注意项目，考核看平时。', 2, 23, 136, DATE_SUB(NOW(), INTERVAL 35481 MINUTE), 224.0, '', '王强');
SET @p98 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p98, 10, 0, '这门课我也上过，整体体验和楼主差不多。', DATE_SUB(NOW(), INTERVAL 35278 MINUTE));
SET @c98_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u8, @p98, 2, 0, '给分确实不错', DATE_SUB(NOW(), INTERVAL 35285 MINUTE));
SET @c98_1 := LAST_INSERT_ID();
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u12, 'post', @p98, 4, DATE_SUB(NOW(), INTERVAL 34881 MINUTE), DATE_SUB(NOW(), INTERVAL 34881 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u0, 'post', @p98, 5, DATE_SUB(NOW(), INTERVAL 34881 MINUTE), DATE_SUB(NOW(), INTERVAL 34881 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u1, 'post', @p98, 4, DATE_SUB(NOW(), INTERVAL 34881 MINUTE), DATE_SUB(NOW(), INTERVAL 34881 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u0, 'post', @p98, 3, DATE_SUB(NOW(), INTERVAL 34881 MINUTE), DATE_SUB(NOW(), INTERVAL 34881 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u11, 'post', @p98, 4, DATE_SUB(NOW(), INTERVAL 34881 MINUTE), DATE_SUB(NOW(), INTERVAL 34881 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u6, 'post', @p98, 5, DATE_SUB(NOW(), INTERVAL 34881 MINUTE), DATE_SUB(NOW(), INTERVAL 34881 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u2, 'post', @p98, 4, DATE_SUB(NOW(), INTERVAL 34881 MINUTE), DATE_SUB(NOW(), INTERVAL 34881 MINUTE));

-- 空调坏了找谁修
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u16, '院务', '空调坏了找谁修', '宿舍空调不制冷，是找后勤还是找厂家？', 3, 16, 189, DATE_SUB(NOW(), INTERVAL 34312 MINUTE), 274.0, '', '报修');
SET @p99 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p99, 21, 0, '关注「智慧后勤」提交报修单，可以看进度。空调和热水器属于厂家保修，报修时选对应类型。', DATE_SUB(NOW(), INTERVAL 34156 MINUTE));
SET @c99_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p99, 6, 0, '同问，我也想知道', DATE_SUB(NOW(), INTERVAL 34155 MINUTE));
SET @c99_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u16, @c99_0, 1, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 33833 MINUTE), '李静');

-- 找个考研搭子
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u13, '学习交流', '找个考研搭子', '目标计算机专硕，想找一起自习互相监督的。', 2, 6, 50, DATE_SUB(NOW(), INTERVAL 33141 MINUTE), 86.0, '', '考研');
SET @p100 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p100, 5, 0, '我也有这个打算，可以一起', DATE_SUB(NOW(), INTERVAL 33068 MINUTE));
SET @c100_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p100, 4, 0, '建议固定自习室，效率高', DATE_SUB(NOW(), INTERVAL 33070 MINUTE));
SET @c100_1 := LAST_INSERT_ID();

-- 操作系统
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u12, '打分', '操作系统', '实验有难度，但能学到东西，老师答疑耐心。', 2, 0, 8, DATE_SUB(NOW(), INTERVAL 31820 MINUTE), 37.0, '', '王强');
SET @p101 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p101, 3, 0, '这门课我也上过，整体体验和楼主差不多。', DATE_SUB(NOW(), INTERVAL 31768 MINUTE));
SET @c101_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u0, @p101, 1, 0, '中肯', DATE_SUB(NOW(), INTERVAL 31810 MINUTE));
SET @c101_1 := LAST_INSERT_ID();
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u7, 'post', @p101, 4, DATE_SUB(NOW(), INTERVAL 31220 MINUTE), DATE_SUB(NOW(), INTERVAL 31220 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u16, 'post', @p101, 4, DATE_SUB(NOW(), INTERVAL 31220 MINUTE), DATE_SUB(NOW(), INTERVAL 31220 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u3, 'post', @p101, 5, DATE_SUB(NOW(), INTERVAL 31220 MINUTE), DATE_SUB(NOW(), INTERVAL 31220 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u16, 'post', @p101, 4, DATE_SUB(NOW(), INTERVAL 31220 MINUTE), DATE_SUB(NOW(), INTERVAL 31220 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u15, 'post', @p101, 3, DATE_SUB(NOW(), INTERVAL 31220 MINUTE), DATE_SUB(NOW(), INTERVAL 31220 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u2, 'post', @p101, 4, DATE_SUB(NOW(), INTERVAL 31220 MINUTE), DATE_SUB(NOW(), INTERVAL 31220 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u7, 'post', @p101, 5, DATE_SUB(NOW(), INTERVAL 31220 MINUTE), DATE_SUB(NOW(), INTERVAL 31220 MINUTE));

-- 社团什么时候招新
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u3, '日常吐槽', '社团什么时候招新', '开学想加社团，百团大战一般什么时候？', 2, 138, 771, DATE_SUB(NOW(), INTERVAL 30957 MINUTE), 1197.0, '', '社团');
SET @p102 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u13, @p102, 17, 0, '9月百团大战在中心广场，选1-2个真正感兴趣的就够。技术类社团项目资源多，但也要看活动频率。', DATE_SUB(NOW(), INTERVAL 30726 MINUTE));
SET @c102_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u8, @p102, 7, 0, '哈哈太真实了', DATE_SUB(NOW(), INTERVAL 30762 MINUTE));
SET @c102_1 := LAST_INSERT_ID();

-- 软件工程导论怎么样
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u12, '课程专区', '软件工程导论怎么样', '这门课给分好吗，平时作业多不多？', 2, 1, 20, DATE_SUB(NOW(), INTERVAL 29697 MINUTE), 52.0, '', '李静');
SET @p103 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p103, 10, 0, '给分不错，小组项目认真做，90+不难', DATE_SUB(NOW(), INTERVAL 29592 MINUTE));
SET @c103_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p103, 4, 0, '作业不算多，但期末要背概念', DATE_SUB(NOW(), INTERVAL 29463 MINUTE));
SET @c103_1 := LAST_INSERT_ID();

-- 论文装订去哪里
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u17, '其他', '论文装订去哪里', '毕业论文需要装订，学校附近哪里可以弄？', 3, 1, 10, DATE_SUB(NOW(), INTERVAL 28184 MINUTE), 43.0, '', '打印');
SET @p104 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p104, 15, 0, '打印便宜的是图书馆一楼，一毛一页。需要装订、彩印去东门文印中心，能开发票。', DATE_SUB(NOW(), INTERVAL 28014 MINUTE));
SET @c104_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p104, 8, 0, '感谢分享', DATE_SUB(NOW(), INTERVAL 28087 MINUTE));
SET @c104_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u17, @c104_0, 4, 0, '好的，我这就去试试。', DATE_SUB(NOW(), INTERVAL 27774 MINUTE), '徐嘉');

-- Java后端面试准备
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '求职招募', 'Java后端面试准备', '面试 Java 后端要准备哪些内容，八股怎么背？', 2, 3, 44, DATE_SUB(NOW(), INTERVAL 27354 MINUTE), 65.0, '', '面试');
SET @p105 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p105, 12, 0, 'Java后端面试重点：集合、JVM、并发、MySQL索引和事务、Redis缓存、Spring。项目要能画架构图，讲清楚难点和取舍。', DATE_SUB(NOW(), INTERVAL 27210 MINUTE));
SET @c105_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p105, 2, 0, '有帮助，谢谢', DATE_SUB(NOW(), INTERVAL 27286 MINUTE));
SET @c105_1 := LAST_INSERT_ID();

-- xv6实验环境怎么配
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u11, '课程专区', 'xv6实验环境怎么配', '操作系统实验的Docker环境怎么搭，有教程吗？', 3, 4, 101, DATE_SUB(NOW(), INTERVAL 26126 MINUTE), 131.0, '', '王强');
SET @p106 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p106, 10, 0, '操作系统实验是xv6的lab系列，环境配好Docker就不折腾。每个lab先看懂框架再改，报告按模板写清楚。', DATE_SUB(NOW(), INTERVAL 25918 MINUTE));
SET @c106_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p106, 1, 0, '感谢学长', DATE_SUB(NOW(), INTERVAL 25947 MINUTE));
SET @c106_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p106, 15, 0, '实验用xv6，第一次先把环境跑通，lab1加系统调用。遇到编译错误先看Makefile，报告截图一定要有。', DATE_SUB(NOW(), INTERVAL 26021 MINUTE));
SET @c106_2 := LAST_INSERT_ID();

-- 四级没过能考六级吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u0, '院务', '四级没过能考六级吗', '四级还没过，可以同时报六级吗？', 3, 86, 625, DATE_SUB(NOW(), INTERVAL 24556 MINUTE), 909.0, '', '四六级');
SET @p107 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p107, 36, 0, '报名在教务系统，缴费后才算成功。照片自己传的要蓝底，不符合会被退回，注意截止时间。', DATE_SUB(NOW(), INTERVAL 24433 MINUTE));
SET @c107_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p107, 2, 0, '同问，我也想知道', DATE_SUB(NOW(), INTERVAL 24402 MINUTE));
SET @c107_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p107, 19, 0, '四六级每年6月和12月，学校统一在教务系统报名。四级过了才能考六级，缺考下次可能限制报名。', DATE_SUB(NOW(), INTERVAL 24400 MINUTE));
SET @c107_2 := LAST_INSERT_ID();

-- 饭卡掉了求补办流程
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u2, '院务', '饭卡掉了求补办流程', '饭卡掉了两天了，补办需要带什么材料，要多少钱，多久能拿到？', 3, 4, 57, DATE_SUB(NOW(), INTERVAL 23435 MINUTE), 97.0, '', '校园卡');
SET @p108 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p108, 19, 0, '先打电话或在小程序挂失（一卡通中心电话在官网能查到），再带身份证去行政楼103补卡，20元。旧卡余额会转到新卡，一般当天到账。', DATE_SUB(NOW(), INTERVAL 23222 MINUTE));
SET @c108_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p108, 5, 0, '之前也遇到过，关注一下', DATE_SUB(NOW(), INTERVAL 23363 MINUTE));
SET @c108_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p108, 17, 0, '第一步先在「校园卡服务」小程序里挂失，避免被别人捡去刷。然后带身份证或学生证到行政楼103一卡通中心补办，工本费20元，现场制卡当天就能拿。', DATE_SUB(NOW(), INTERVAL 23294 MINUTE));
SET @c108_2 := LAST_INSERT_ID();

-- 家教兼职怎么找
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u2, '求职招募', '家教兼职怎么找', '想做家教，学校有推荐的平台吗？', 4, 32, 290, DATE_SUB(NOW(), INTERVAL 22736 MINUTE), 416.0, '', '兼职');
SET @p109 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p109, 29, 0, '优先校内勤工助学，安全又不耽误课。校外兼职别信高薪日结，押金和培训费一律不交。', DATE_SUB(NOW(), INTERVAL 22704 MINUTE));
SET @c109_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p109, 4, 0, '同问，正在找工作', DATE_SUB(NOW(), INTERVAL 22623 MINUTE));
SET @c109_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p109, 7, 0, '校内勤工助学在学工系统申请，图书馆、实验室助理都有。校外兼职注意签协议，任何要交押金的都别去。', DATE_SUB(NOW(), INTERVAL 22591 MINUTE));
SET @c109_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u2, @c109_0, 3, 0, '明白了，谢谢学长！', DATE_SUB(NOW(), INTERVAL 22437 MINUTE), '李航');

-- 求推荐好用的笔记软件
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u7, '其他', '求推荐好用的笔记软件', '做课程笔记用，最好能同步和搜索。', 2, 36, 675, DATE_SUB(NOW(), INTERVAL 21409 MINUTE), 797.0, '', '笔记软件');
SET @p110 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p110, 12, 0, 'Notion 和 Obsidian 都不错，Obsidian 本地更快', DATE_SUB(NOW(), INTERVAL 21332 MINUTE));
SET @c110_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p110, 4, 0, '课程笔记用 OneNote 也可以', DATE_SUB(NOW(), INTERVAL 21366 MINUTE));
SET @c110_1 := LAST_INSERT_ID();

-- 一卡通丢失如何挂失
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u1, '院务', '一卡通丢失如何挂失', '校园卡不知道掉哪了，怕被盗刷，挂失和补办分别要去哪？', 4, 20, 565, DATE_SUB(NOW(), INTERVAL 20231 MINUTE), 666.0, '', '校园卡');
SET @p111 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p111, 36, 0, '卡丢了先线上挂失：微信搜索「校园卡服务」→挂失。补办去行政楼103，带身份证，20元工本费，周一到周五9:00-17:00办理，当天可取。', DATE_SUB(NOW(), INTERVAL 20172 MINUTE));
SET @c111_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p111, 7, 0, '之前也遇到过，关注一下', DATE_SUB(NOW(), INTERVAL 20106 MINUTE));
SET @c111_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p111, 19, 0, '先打电话或在小程序挂失（一卡通中心电话在官网能查到），再带身份证去行政楼103补卡，20元。旧卡余额会转到新卡，一般当天到账。', DATE_SUB(NOW(), INTERVAL 20186 MINUTE));
SET @c111_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u1, @c111_0, 4, 0, '好的，我这就去试试。', DATE_SUB(NOW(), INTERVAL 20019 MINUTE), '李静');

-- Boot项目Bean冲突
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '学习交流', 'Boot项目Bean冲突', '启动报 Bean 冲突，是怎么回事？', 3, 70, 629, DATE_SUB(NOW(), INTERVAL 18824 MINUTE), 857.0, '', 'SpringBoot');
SET @p112 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p112, 22, 0, '看完整堆栈找 Caused by，数据库连接问题检查 url、账号密码和防火墙；Bean 冲突看是不是重复扫描。', DATE_SUB(NOW(), INTERVAL 18812 MINUTE));
SET @c112_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p112, 5, 0, '同问，最近也在纠结这个', DATE_SUB(NOW(), INTERVAL 18657 MINUTE));
SET @c112_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p112, 15, 0, '先看日志里的 Caused by。常见是端口被占用、数据库连不上、Redis/Kafka 没启动。按第一个异常往下查，别只看最后一行。', DATE_SUB(NOW(), INTERVAL 18667 MINUTE));
SET @c112_2 := LAST_INSERT_ID();

-- 日常实习值得去吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u7, '求职招募', '日常实习值得去吗', '日常实习和暑期实习有什么区别，哪个更适合？', 2, 30, 774, DATE_SUB(NOW(), INTERVAL 18065 MINUTE), 876.0, '', '实习');
SET @p113 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p113, 33, 0, '找实习先定方向，Java后端就准备JVM、并发、MySQL、Redis和项目。3月开始投，别等暑假。', DATE_SUB(NOW(), INTERVAL 17913 MINUTE));
SET @c113_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p113, 2, 0, '有帮助，谢谢', DATE_SUB(NOW(), INTERVAL 18006 MINUTE));
SET @c113_1 := LAST_INSERT_ID();

-- 合并分支冲突了
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u17, '学习交流', '合并分支冲突了', 'merge 之后一堆冲突文件，应该怎么改？', 3, 101, 703, DATE_SUB(NOW(), INTERVAL 16879 MINUTE), 1032.0, '', 'Git');
SET @p114 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p114, 28, 0, '冲突就打开文件找 <<<<<<< 标记，保留需要的内容，删干净标记，git add 后继续。推之前先 pull。', DATE_SUB(NOW(), INTERVAL 16844 MINUTE));
SET @c114_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p114, 8, 0, '学到了，感谢分享', DATE_SUB(NOW(), INTERVAL 16646 MINUTE));
SET @c114_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p114, 5, 0, '合并冲突不可怕，改完记得编译测试再提交。团队分支上 force push 会覆盖别人代码，禁止。', DATE_SUB(NOW(), INTERVAL 16642 MINUTE));
SET @c114_2 := LAST_INSERT_ID();

-- 计算机网络
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u9, '打分', '计算机网络', '知识点多，但老师讲得清楚。', 2, 10, 182, DATE_SUB(NOW(), INTERVAL 15835 MINUTE), 243.0, '', '王强');
SET @p115 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p115, 4, 0, '这门课我也上过，整体体验和楼主差不多。', DATE_SUB(NOW(), INTERVAL 15721 MINUTE));
SET @c115_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u8, @p115, 2, 0, '参考一下', DATE_SUB(NOW(), INTERVAL 15822 MINUTE));
SET @c115_1 := LAST_INSERT_ID();
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u2, 'post', @p115, 4, DATE_SUB(NOW(), INTERVAL 15235 MINUTE), DATE_SUB(NOW(), INTERVAL 15235 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u0, 'post', @p115, 4, DATE_SUB(NOW(), INTERVAL 15235 MINUTE), DATE_SUB(NOW(), INTERVAL 15235 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u16, 'post', @p115, 3, DATE_SUB(NOW(), INTERVAL 15235 MINUTE), DATE_SUB(NOW(), INTERVAL 15235 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u13, 'post', @p115, 4, DATE_SUB(NOW(), INTERVAL 15235 MINUTE), DATE_SUB(NOW(), INTERVAL 15235 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u13, 'post', @p115, 5, DATE_SUB(NOW(), INTERVAL 15235 MINUTE), DATE_SUB(NOW(), INTERVAL 15235 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u1, 'post', @p115, 4, DATE_SUB(NOW(), INTERVAL 15235 MINUTE), DATE_SUB(NOW(), INTERVAL 15235 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u13, 'post', @p115, 4, DATE_SUB(NOW(), INTERVAL 15235 MINUTE), DATE_SUB(NOW(), INTERVAL 15235 MINUTE));

-- 教室借用申请
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u12, '院务', '教室借用申请', '办事大厅里没找到借教室入口，有谁知道具体路径？', 3, 2, 46, DATE_SUB(NOW(), INTERVAL 14496 MINUTE), 70.0, '', '教室');
SET @p116 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p116, 16, 0, '借教室走企业微信办事大厅，选空闲教室，填写用途，找指导老师批。临时借用基本批不下来，至少提前3天。', DATE_SUB(NOW(), INTERVAL 14404 MINUTE));
SET @c116_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p116, 6, 0, '收藏了，谢谢', DATE_SUB(NOW(), INTERVAL 14483 MINUTE));
SET @c116_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p116, 12, 0, '企业微信→办事大厅→教室借用，填时间、人数和用途，需要指导老师审批，至少提前3个工作日。晚上18点后的活动还要报备保卫处。', DATE_SUB(NOW(), INTERVAL 14381 MINUTE));
SET @c116_2 := LAST_INSERT_ID();

-- 有没有一起做开源项目的
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u1, '求职招募', '有没有一起做开源项目的', '想组队做个校园项目练手，前后端都要，最好能坚持。', 2, 107, 666, DATE_SUB(NOW(), INTERVAL 13670 MINUTE), 1010.0, '', '开源');
SET @p117 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p117, 6, 0, '算我一个，会 Java 后端', DATE_SUB(NOW(), INTERVAL 13487 MINUTE));
SET @c117_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p117, 4, 0, '前端可以帮忙', DATE_SUB(NOW(), INTERVAL 13478 MINUTE));
SET @c117_1 := LAST_INSERT_ID();

-- 二手教材哪里买
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '其他', '二手教材哪里买', '专业课教材太贵，二手教材去哪里收比较全？', 3, 0, 11, DATE_SUB(NOW(), INTERVAL 12688 MINUTE), 37.0, '', '教材');
SET @p118 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p118, 12, 0, '开学第一周教材科门口和二手群最集中，闲鱼也能淘。注意版本，问清楚是不是老师指定的那版。', DATE_SUB(NOW(), INTERVAL 12580 MINUTE));
SET @c118_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p118, 1, 0, '同问', DATE_SUB(NOW(), INTERVAL 12664 MINUTE));
SET @c118_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p118, 14, 0, '学长学姐群、闲鱼、教材科门口都行，价格一般三到五折。买之前对一下ISBN和版次。', DATE_SUB(NOW(), INTERVAL 12631 MINUTE));
SET @c118_2 := LAST_INSERT_ID();

-- 技术社团值得加吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u6, '日常吐槽', '技术社团值得加吗', '想学编程，技术类社团能学到东西吗？', 2, 26, 594, DATE_SUB(NOW(), INTERVAL 11373 MINUTE), 695.0, '', '社团');
SET @p119 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p119, 11, 0, '想学技术就找有项目的社团，想放松就选兴趣类。报名后问清楚会费和活动安排。', DATE_SUB(NOW(), INTERVAL 11318 MINUTE));
SET @c119_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u8, @p119, 3, 0, '同感', DATE_SUB(NOW(), INTERVAL 11223 MINUTE));
SET @c119_1 := LAST_INSERT_ID();

-- 四六级缺考影响下次吗
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u1, '院务', '四六级缺考影响下次吗', '上次四六级缺考了，下次还能报名吗？', 3, 9, 51, DATE_SUB(NOW(), INTERVAL 10094 MINUTE), 101.0, '', '四六级');
SET @p120 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p120, 15, 0, '四六级每年6月和12月，学校统一在教务系统报名。四级过了才能考六级，缺考下次可能限制报名。', DATE_SUB(NOW(), INTERVAL 10033 MINUTE));
SET @c120_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p120, 3, 0, '收藏了，谢谢', DATE_SUB(NOW(), INTERVAL 9990 MINUTE));
SET @c120_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u15, @p120, 16, 0, '教务系统报名，照片要求蓝底证件照。四级425才能报六级，口语自愿。缺考会影响下次报名资格。', DATE_SUB(NOW(), INTERVAL 10071 MINUTE));
SET @c120_2 := LAST_INSERT_ID();

-- 下雨天从宿舍走到教学楼
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u12, '日常吐槽', '下雨天从宿舍走到教学楼', '一路全是水坑，鞋全湿了，求推荐防水鞋。', 2, 69, 459, DATE_SUB(NOW(), INTERVAL 9413 MINUTE), 691.0, '', '雨天');
SET @p121 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u8, @p121, 5, 0, '买双防泼水的运动鞋就行', DATE_SUB(NOW(), INTERVAL 9298 MINUTE));
SET @c121_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p121, 2, 0, '记得带伞，最近雨多', DATE_SUB(NOW(), INTERVAL 9316 MINUTE));
SET @c121_1 := LAST_INSERT_ID();

-- 二食堂排队排到门外
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '日常吐槽', '二食堂排队排到门外', '中午12点去二食堂，队伍直接排到门口了，等了20分钟才打上饭。', 3, 9, 154, DATE_SUB(NOW(), INTERVAL 8126 MINUTE), 217.0, '', '食堂');
SET @p122 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u12, @p122, 7, 0, '11点半去会好很多', DATE_SUB(NOW(), INTERVAL 8046 MINUTE));
SET @c122_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u6, @p122, 3, 0, '高峰期都这样，错峰吧', DATE_SUB(NOW(), INTERVAL 7932 MINUTE));
SET @c122_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u8, @c122_0, 2, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 7887 MINUTE), '徐嘉');

-- 计算机网络期末重点
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u11, '课程专区', '计算机网络期末重点', '网络期末重点是什么，计算题多吗？', 3, 1, 12, DATE_SUB(NOW(), INTERVAL 7463 MINUTE), 43.0, '', '王强');
SET @p123 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p123, 13, 0, 'TCP拥塞控制、子网划分必考，计算题占一半', DATE_SUB(NOW(), INTERVAL 7436 MINUTE));
SET @c123_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p123, 7, 0, '往年题重复率挺高，一定刷', DATE_SUB(NOW(), INTERVAL 7345 MINUTE));
SET @c123_1 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u11, @c123_0, 2, 0, '明白了，谢谢学长！', DATE_SUB(NOW(), INTERVAL 7077 MINUTE), '吴昊');

-- 转专业需要什么条件
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u8, '院务', '转专业需要什么条件', '想从材料转到计算机，转专业看绩点还是考试？', 3, 14, 207, DATE_SUB(NOW(), INTERVAL 6223 MINUTE), 286.0, '', '转专业');
SET @p124 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p124, 21, 0, '大一结束申请，要求绩点排名专业前30%，5月教务处发布通知。流程是网申+笔试+面试，计算机这类热门专业竞争很激烈。', DATE_SUB(NOW(), INTERVAL 6110 MINUTE));
SET @c124_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p124, 2, 0, '之前也遇到过，关注一下', DATE_SUB(NOW(), INTERVAL 6074 MINUTE));
SET @c124_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u14, @p124, 18, 0, '转专业一般在大一末，绩点前30%才有资格报名，具体名额看当年通知。想转计算机建议提前刷编程题，面试会问。', DATE_SUB(NOW(), INTERVAL 6028 MINUTE));
SET @c124_2 := LAST_INSERT_ID();

-- 食堂哪个窗口好吃
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u12, '日常吐槽', '食堂哪个窗口好吃', '求推荐食堂值得吃的窗口，刚来学校不知道吃啥。', 4, 116, 891, DATE_SUB(NOW(), INTERVAL 5470 MINUTE), 1268.0, '', '食堂');
SET @p125 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u13, @p125, 25, 0, '一食堂二楼麻辣香锅和二食堂一楼牛肉面比较稳，三食堂早餐的豆浆油条不错。11:40-12:10人最多，建议错峰。', DATE_SUB(NOW(), INTERVAL 5451 MINUTE));
SET @c125_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u7, @p125, 5, 0, '抱抱楼主', DATE_SUB(NOW(), INTERVAL 5238 MINUTE));
SET @c125_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u8, @p125, 7, 0, '推荐二食堂二楼的砂锅，一食堂一楼的快餐性价比高。想人少就11:20前或12:30后去。', DATE_SUB(NOW(), INTERVAL 5344 MINUTE));
SET @c125_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u12, @c125_0, 2, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 5010 MINUTE), '马腾');

-- 校外兼职要注意什么
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u1, '求职招募', '校外兼职要注意什么', '找校外兼职怎么避免被骗，有没有靠谱渠道？', 4, 3, 18, DATE_SUB(NOW(), INTERVAL 4097 MINUTE), 52.0, '', '兼职');
SET @p126 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u9, @p126, 32, 0, '校内岗位关注学工处通知，按月发补贴。校外家教、门店兼职要确认结算方式，保护个人信息。', DATE_SUB(NOW(), INTERVAL 4026 MINUTE));
SET @c126_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p126, 8, 0, '感谢内推信息', DATE_SUB(NOW(), INTERVAL 3968 MINUTE));
SET @c126_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p126, 5, 0, '优先校内勤工助学，安全又不耽误课。校外兼职别信高薪日结，押金和培训费一律不交。', DATE_SUB(NOW(), INTERVAL 3904 MINUTE));
SET @c126_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u1, @c126_0, 4, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 3772 MINUTE), '吴昊');

-- 夏令营什么时候报名
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u13, '学习交流', '夏令营什么时候报名', '想保研，夏令营一般什么时候开始投？', 2, 54, 576, DATE_SUB(NOW(), INTERVAL 3703 MINUTE), 751.0, '', '保研');
SET @p127 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p127, 36, 0, '想保研先把绩点稳住，六级尽早过。4-6月投夏令营，9月如果没拿到优营再参加预推免。', DATE_SUB(NOW(), INTERVAL 3663 MINUTE));
SET @c127_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p127, 3, 0, '这个回答很全，收藏了', DATE_SUB(NOW(), INTERVAL 3566 MINUTE));
SET @c127_1 := LAST_INSERT_ID();

-- Redis连接不上
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u12, '学习交流', 'Redis连接不上', 'Spring Boot 启动报 Redis 连接失败，Redis 明明装了。', 4, 28, 755, DATE_SUB(NOW(), INTERVAL 2431 MINUTE), 880.0, '', 'Redis');
SET @p128 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p128, 8, 0, '先 redis-cli ping 看有没有 PONG。没起就启动服务；连不上检查 bind 和 protected-mode，WSL 里启动后 Windows 用 localhost 访问。', DATE_SUB(NOW(), INTERVAL 2219 MINUTE));
SET @c128_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p128, 6, 0, '蹲一个后续', DATE_SUB(NOW(), INTERVAL 2379 MINUTE));
SET @c128_1 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u2, @p128, 6, 0, 'Redis 报连接拒绝一般是没启动或端口不对。改配置后记得重启，密码要和应用配置一致。', DATE_SUB(NOW(), INTERVAL 2281 MINUTE));
SET @c128_2 := LAST_INSERT_ID();
INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) VALUES (@u12, @c128_0, 4, 0, '感谢，按你说的已经办好了！', DATE_SUB(NOW(), INTERVAL 1971 MINUTE), '何洛');

-- 大学英语
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u15, '打分', '大学英语', '课堂氛围轻松，考试不难。', 2, 3, 30, DATE_SUB(NOW(), INTERVAL 1456 MINUTE), 55.0, '', '李静');
SET @p129 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p129, 12, 0, '这门课我也上过，整体体验和楼主差不多。', DATE_SUB(NOW(), INTERVAL 1295 MINUTE));
SET @c129_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u11, @p129, 2, 0, '中肯', DATE_SUB(NOW(), INTERVAL 1425 MINUTE));
SET @c129_1 := LAST_INSERT_ID();
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u8, 'post', @p129, 5, DATE_SUB(NOW(), INTERVAL 856 MINUTE), DATE_SUB(NOW(), INTERVAL 856 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u1, 'post', @p129, 4, DATE_SUB(NOW(), INTERVAL 856 MINUTE), DATE_SUB(NOW(), INTERVAL 856 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u10, 'post', @p129, 4, DATE_SUB(NOW(), INTERVAL 856 MINUTE), DATE_SUB(NOW(), INTERVAL 856 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u6, 'post', @p129, 5, DATE_SUB(NOW(), INTERVAL 856 MINUTE), DATE_SUB(NOW(), INTERVAL 856 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u5, 'post', @p129, 5, DATE_SUB(NOW(), INTERVAL 856 MINUTE), DATE_SUB(NOW(), INTERVAL 856 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u10, 'post', @p129, 4, DATE_SUB(NOW(), INTERVAL 856 MINUTE), DATE_SUB(NOW(), INTERVAL 856 MINUTE));
INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) VALUES (@u5, 'post', @p129, 4, DATE_SUB(NOW(), INTERVAL 856 MINUTE), DATE_SUB(NOW(), INTERVAL 856 MINUTE));

-- 出个27寸显示器
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u10, '其他', '出个27寸显示器', '自用显示器，2K分辨率，成色九成新，有意私聊。', 2, 2, 18, DATE_SUB(NOW(), INTERVAL 1184 MINUTE), 55.0, '', '二手');
SET @p130 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u5, @p130, 3, 0, '多少钱出？', DATE_SUB(NOW(), INTERVAL 1151 MINUTE));
SET @c130_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u10, @p130, 2, 0, '600，可以当面验货', DATE_SUB(NOW(), INTERVAL 1113 MINUTE));
SET @c130_1 := LAST_INSERT_ID();

-- 服务器部署注意事项
INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) VALUES (@u5, '学习交流', '服务器部署注意事项', '项目要上线了，部署时有哪些容易踩的坑？', 2, 11, 329, DATE_SUB(NOW(), INTERVAL 430 MINUTE), 383.0, '', '部署');
SET @p131 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u16, @p131, 24, 0, '服务器上先装 JDK 和 nginx，前端构建上传，后端 java -jar 或 systemd。数据库密码用环境变量，别写进 jar。', DATE_SUB(NOW(), INTERVAL 427 MINUTE));
SET @c131_0 := LAST_INSERT_ID();
INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) VALUES (@u3, @p131, 2, 0, '同问，最近也在纠结这个', DATE_SUB(NOW(), INTERVAL 278 MINUTE));
SET @c131_1 := LAST_INSERT_ID();

COMMIT;

-- ============ 导入结果校验 ============
SELECT '种子用户' AS item, COUNT(*) AS cnt FROM user WHERE email LIKE 'ragseed_%'
UNION ALL SELECT '种子帖子', COUNT(*) FROM post WHERE user_id IN (SELECT user_id FROM user WHERE email LIKE 'ragseed_%')
UNION ALL SELECT '种子一级评论', COUNT(*) FROM pcomment WHERE user_id IN (SELECT user_id FROM user WHERE email LIKE 'ragseed_%')
UNION ALL SELECT '种子二级评论', COUNT(*) FROM ccomment WHERE user_id IN (SELECT user_id FROM user WHERE email LIKE 'ragseed_%')
UNION ALL SELECT '种子评分', COUNT(*) FROM rating WHERE item_type = 'post' AND item_id IN (SELECT post_id FROM post WHERE user_id IN (SELECT user_id FROM user WHERE email LIKE 'ragseed_%'));
