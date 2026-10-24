#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 RAG 演示种子数据（MySQL SQL）。

设计目标：同一类问题有多个措辞不同的历史帖，且帖子回复里包含一致的
“标准答案”，方便后续做「检索历史帖子 + 依据历史回复预答」的 RAG。

用法：
    python docs/seed/generate_rag_seed.py
输出：
    docs/seed/rag_seed.sql
"""

from __future__ import annotations

import random
from pathlib import Path

SEED = 20261024
rng = random.Random(SEED)

# ---------------------------------------------------------------- 用户
# (姓名, 邮箱, 简介, 身份, 积分)
USERS = [
    ("林晓", "ragseed_linxiao@example.com", "软件工程大三，喜欢折腾后端", "student", 128),
    ("陈子昂", "ragseed_chenziang@example.com", "计算机大二，Java 学习中", "student", 96),
    ("王雨桐", "ragseed_wangyutong@example.com", "软件工程研一，方向是数据挖掘", "student", 210),
    ("李航", "ragseed_lihang@example.com", "计算机大四，已保研", "student", 356),
    ("张伟", "ragseed_zhangwei@example.com", "计算机学院教师，讲授组成原理", "teacher", 500),
    ("刘思远", "ragseed_liusiyuan@example.com", "网络工程大三", "student", 74),
    ("赵一鸣", "ragseed_zhaoyiming@example.com", "计科大一，新生一枚", "student", 22),
    ("孙悦", "ragseed_sunyue@example.com", "软件工程大二，爱逛食堂", "student", 65),
    ("周可欣", "ragseed_zhoukexin@example.com", "数字媒体大二", "student", 48),
    ("吴昊", "ragseed_wuhao@example.com", "计算机大三，会一点运维", "student", 152),
    ("郑晓琳", "ragseed_zhengxiaolin@example.com", "软件工程大四，在实习", "student", 268),
    ("何洛", "ragseed_heluo@example.com", "计科大三，后端方向", "student", 134),
    ("徐嘉", "ragseed_xujia@example.com", "软件工程大二", "student", 58),
    ("马腾", "ragseed_mateng@example.com", "人工智能大一", "student", 18),
    ("李静", "ragseed_lijing@example.com", "教务处老师，负责学籍与考试", "teacher", 460),
    ("王一诺", "ragseed_wangyinuo@example.com", "软件工程大三，考研准备中", "student", 88),
    ("唐可可", "ragseed_tangkeke@example.com", "计科大四，准备找工作", "student", 176),
    ("高远", "ragseed_gaoyuan@example.com", "网络空间安全大三", "student", 122),
]

# bcrypt hash of "password"（种子账号仅用于展示作者，不用于登录）
SEED_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

STUDENTS = [0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 17]
ANSWER_POOLS = {
    "院务": [14, 2, 7, 15, 3],
    "学习交流": [3, 2, 9, 11, 16],
    "课程专区": [3, 9, 11, 16, 2],
    "求职招募": [10, 3, 11, 16, 9],
    "日常吐槽": [8, 7, 12, 13, 5],
    "其他": [5, 12, 16, 17, 9],
    "打分": [3, 2, 9, 11],
}

CHATTER = {
    "院务": ["同问，我也想知道", "帮顶，蹲一个准确答案", "之前也遇到过，关注一下", "收藏了，谢谢"],
    "学习交流": ["同问，最近也在纠结这个", "学到了，感谢分享", "这个回答很全，收藏了", "蹲一个后续"],
    "课程专区": ["同问，选课前想了解一下", "感谢学长", "这个信息很有用", "已收藏"],
    "求职招募": ["同问，正在找工作", "感谢内推信息", "有帮助，谢谢", "先码住"],
    "日常吐槽": ["哈哈太真实了", "同感", "笑死，我也是", "抱抱楼主"],
    "其他": ["帮顶", "感谢分享", "同问", "学到了"],
    "打分": ["同感", "给分确实不错", "中肯", "参考一下"],
}

FOLLOW_UPS = [
    "感谢，按你说的已经办好了！",
    "明白了，谢谢学长！",
    "谢谢，信息很及时。",
    "好的，我这就去试试。",
    "太详细了，感谢！",
]

# ---------------------------------------------------------------- 主题簇
# 每个主题 3 个措辞不同的提问帖 + 3 个事实一致的标准答案
TOPICS = [
    {
        "category": "院务",
        "tag": "校园卡",
        "variants": [
            ("校园卡丢了怎么补办", "昨天在二食堂吃完饭发现校园卡不见了，里面有刚充的200块，急！"),
            ("一卡通丢失如何挂失", "校园卡不知道掉哪了，怕被盗刷，挂失和补办分别要去哪？"),
            ("饭卡掉了求补办流程", "饭卡掉了两天了，补办需要带什么材料，要多少钱，多久能拿到？"),
        ],
        "answers": [
            "第一步先在「校园卡服务」小程序里挂失，避免被别人捡去刷。然后带身份证或学生证到行政楼103一卡通中心补办，工本费20元，现场制卡当天就能拿。",
            "卡丢了先线上挂失：微信搜索「校园卡服务」→挂失。补办去行政楼103，带身份证，20元工本费，周一到周五9:00-17:00办理，当天可取。",
            "先打电话或在小程序挂失（一卡通中心电话在官网能查到），再带身份证去行政楼103补卡，20元。旧卡余额会转到新卡，一般当天到账。",
        ],
    },
    {
        "category": "院务",
        "tag": "考试",
        "variants": [
            ("补考怎么申请", "上学期高数挂了，补考在哪里申请？截止到什么时候？"),
            ("挂科了补考流程", "成绩刚出，有一门没过，想问补考申请流程和时间安排。"),
            ("补考申请截止时间", "请问补考最晚什么时候申请，错过了还能补办吗？"),
        ],
        "answers": [
            "成绩出来后一周内登录教务系统，在「考试报名→补考申请」里提交，逾期只能找学院教务办。补考一般安排在开学第2-3周，时间和考场在教务系统查。",
            "教务系统→考试报名→补考申请，成绩公布后7天内操作。注意补考没有平时分，卷面必须及格。具体安排开学第二周会在学院群通知。",
            "先确认成绩是否已录入，然后在教务系统提交补考申请，截止时间是成绩公布后一周。错过时间联系辅导员走线下流程。",
        ],
    },
    {
        "category": "院务",
        "tag": "成绩单",
        "variants": [
            ("成绩单在哪打印", "要申请出国，需要中英文成绩单，去哪里打印比较快？"),
            ("英文成绩单怎么开", "学校能开英文成绩单吗？需要盖章的话走什么流程？"),
            ("成绩单盖章流程", "找工作要交成绩单，自助机打的能直接用吗？"),
        ],
        "answers": [
            "行政楼2楼有自助打印机，刷校园卡就能打中英文成绩单，每份2元。出国申请需要盖章版的话去教务处窗口，提前3个工作日。",
            "自助打印机在行政楼二楼大厅，中英文都能打。要官方盖章的到教务处106，带学生证，一般当天能取；高峰期建议提前3天。",
            "普通成绩单用自助机，刷校园卡即打；英文版和盖章版去教务处办。已经毕业的校友走线上申请邮寄。",
        ],
    },
    {
        "category": "院务",
        "tag": "教室",
        "variants": [
            ("怎么申请借教室", "社团想周末借间教室搞活动，流程是什么，要提前几天？"),
            ("借教室需要什么手续", "班级想借教室开班会，需要找谁审批？"),
            ("教室借用申请", "办事大厅里没找到借教室入口，有谁知道具体路径？"),
        ],
        "answers": [
            "企业微信→办事大厅→教室借用，填时间、人数和用途，需要指导老师审批，至少提前3个工作日。晚上18点后的活动还要报备保卫处。",
            "在办事大厅提交教室借用申请，审批流是指导老师→学院→教务处。提前3天以上，周一到周五办理。社团活动要附活动方案。",
            "借教室走企业微信办事大厅，选空闲教室，填写用途，找指导老师批。临时借用基本批不下来，至少提前3天。",
        ],
    },
    {
        "category": "院务",
        "tag": "报修",
        "variants": [
            ("宿舍水管坏了怎么报修", "卫生间水管漏水，报修应该找谁？大概多久能来？"),
            ("宿舍报修流程", "宿舍灯坏了，后勤报修在哪提交？"),
            ("空调坏了找谁修", "宿舍空调不制冷，是找后勤还是找厂家？"),
        ],
        "answers": [
            "企业微信搜「智慧后勤」→在线报修，选楼栋、房间和故障类型。水电急修直接打楼下值班室电话，一般24小时内上门，不要自己拆。",
            "后勤小程序上报修最快，拍照描述清楚。正常维修1-2天，漏水断电这类急修打值班室电话，师傅当天来。",
            "关注「智慧后勤」提交报修单，可以看进度。空调和热水器属于厂家保修，报修时选对应类型。",
        ],
    },
    {
        "category": "院务",
        "tag": "医保",
        "variants": [
            ("校外看病怎么报销", "在校外医院看了门诊，发票都在，回学校怎么报销？"),
            ("大学生医保报销流程", "校医院说校外就医能报，需要准备哪些材料？"),
            ("医保报销时间", "医保报销是每个月几号办理？多久到账？"),
        ],
        "answers": [
            "在校医院看病直接刷医保卡结算。校外就医要先自费，保留发票、费用清单和病历，每月10-20日到校医院医保办手工报销。",
            "校外门诊报销需要发票原件、病历本和费用明细，交到校医院一楼医保窗口，每月中旬集中办理，到账大概一个月。",
            "急诊或转诊的校外费用可以报，前提是有转诊单。材料：发票、清单、病历、银行卡复印件，交医保办。普通药店买药不报。",
        ],
    },
    {
        "category": "学习交流",
        "tag": "图书馆",
        "variants": [
            ("图书馆座位怎么预约", "考研想去图书馆自习，座位是当天约还是提前约？"),
            ("图书馆占座会被清吗", "离开一会儿座位会被释放吗？怎么算违规？"),
            ("考研专区座位申请", "图书馆四楼考研专区的长期座位怎么申请？"),
        ],
        "answers": [
            "用「图书馆座位预约」小程序，前一天20:00开放第二天座位。到馆后扫码签到，离开超过30分钟座位会被释放。考研专区需要人脸认证。",
            "座位提前一天晚上8点在预约小程序抢，热门位置很快没。签到后临时离开记得点「暂离」，否则30分钟自动释放。",
            "图书馆座位在微信小程序预约，刷脸或扫码签到。违约3次会停用预约权限一周，注意别爽约。",
        ],
    },
    {
        "category": "其他",
        "tag": "快递",
        "variants": [
            ("学校快递点在哪", "新生求问，菜鸟驿站在哪，晚上几点关门？"),
            ("顺丰快递去哪取", "顺丰和京东的快递是送到哪个点？"),
            ("大件快递怎么取", "买了个显示器，大件快递会送到哪里？"),
        ],
        "answers": [
            "菜鸟驿站在东门学生服务中心一楼，营业到21:30；顺丰和京东在西门快递柜；大件行李到南门货运点。取件带取件码就行。",
            "大部分快递在东门菜鸟驿站，凭取件码自助取。顺丰、京东在西门，晚上9点前都能取。",
            "东门学生服务中心一楼是菜鸟驿站，21:30关门。大件会送到南门，收到短信再过去。",
        ],
    },
    {
        "category": "其他",
        "tag": "校园网",
        "variants": [
            ("校园网连不上怎么办", "连上校园网但认证失败，一直提示密码错误。"),
            ("校园网账号密码是什么", "新生的校园网账号和初始密码是什么？"),
            ("宿舍网老是掉线", "宿舍校园网隔一会儿就掉，需要重新认证，怎么解决？"),
        ],
        "answers": [
            "连 SDU-EDU，账号是学号，初始密码是身份证后6位。认证失败先在自助服务页解绑设备再重连，最多同时在线3台。",
            "校园网账号就是学号，密码初始为身份证后六位，改过的用新密码。掉线一般是设备数超了，去自助服务解绑。",
            "宿舍网和教学区网是同一套账号。连不上先忘记网络重连，还不行就在自助服务页重置密码或报修。",
        ],
    },
    {
        "category": "日常吐槽",
        "tag": "食堂",
        "variants": [
            ("食堂哪个窗口好吃", "求推荐食堂值得吃的窗口，刚来学校不知道吃啥。"),
            ("二食堂有什么推荐的", "二食堂哪些菜比较好吃？性价比高一点的。"),
            ("学校食堂几点人少", "想避开高峰期，一般几点去食堂比较空？"),
        ],
        "answers": [
            "一食堂二楼麻辣香锅和二食堂一楼牛肉面比较稳，三食堂早餐的豆浆油条不错。11:40-12:10人最多，建议错峰。",
            "推荐二食堂二楼的砂锅，一食堂一楼的快餐性价比高。想人少就11:20前或12:30后去。",
            "三食堂新开的麻辣烫可以，一食堂二楼的铁板饭份量足。高峰期排队10-15分钟，能错峰尽量错峰。",
        ],
    },
    {
        "category": "其他",
        "tag": "打印",
        "variants": [
            ("学校打印店在哪", "要打印课程论文，哪里打印便宜，能微信支付吗？"),
            ("论文装订去哪里", "毕业论文需要装订，学校附近哪里可以弄？"),
            ("彩印和装订求推荐", "需要彩印几份材料并装订，东门那家可以吗？"),
        ],
        "answers": [
            "图书馆一楼自助打印0.1元/页，微信扫码；论文装订去东门文印中心，普通装订20元起，半小时可取。",
            "打印便宜的是图书馆一楼，一毛一页。需要装订、彩印去东门文印中心，能开发票。",
            "自助打印机在图书馆一楼和教学楼A座大厅都有，刷校园卡或微信。论文装订提前一天送过去更稳。",
        ],
    },
    {
        "category": "院务",
        "tag": "校医院",
        "variants": [
            ("校医院几点开门", "晚上发烧能去校医院吗？门诊时间是几点？"),
            ("校医院看病流程", "第一次去校医院，挂号流程是什么，能用医保吗？"),
            ("转诊单怎么开", "想去大医院看病，校医院给开转诊单吗？"),
        ],
        "answers": [
            "门诊8:00-17:30，晚上有急诊到21:00。感冒发烧先挂内科，学生医保直接结算。需要转诊找医生开转诊单。",
            "校医院一楼挂号，普通门诊全天，急诊晚上到9点。发烧先去分诊台测体温，再挂内科。",
            "看病带校园卡，挂号1元。小病校医院就行，需要去大医院让医生开转诊单，后面才能报销。",
        ],
    },
    {
        "category": "其他",
        "tag": "校园",
        "variants": [
            ("主楼A座在哪", "课表写A101，主楼A座怎么走？"),
            ("二教怎么走", "从南门进学校，去二教怎么走最近？"),
            ("B座和A座是同一栋吗", "上课地点写B203，和A座在一起吗？"),
        ],
        "answers": [
            "主楼A座在图书馆北侧，二教从南门进直走200米。课表里的 A101 就是 A座1层101，B开头是B座。",
            "A座和B座挨着，都在图书馆北边；二教在南门主干道尽头。楼里都有楼层指引，找不到问保安最快。",
            "上课地点看楼栋字母：A/B座在图书馆北侧，二教南门方向，三教在东门。提前10分钟出门基本够。",
        ],
    },
    {
        "category": "院务",
        "tag": "转专业",
        "variants": [
            ("转专业需要什么条件", "想从材料转到计算机，转专业看绩点还是考试？"),
            ("转专业什么时候申请", "大几可以转专业，一般什么时候发通知？"),
            ("转计算机难不难", "转计算机专业竞争大吗，需要提前准备什么？"),
        ],
        "answers": [
            "大一结束申请，要求绩点排名专业前30%，5月教务处发布通知。流程是网申+笔试+面试，计算机这类热门专业竞争很激烈。",
            "转专业一般在大一末，绩点前30%才有资格报名，具体名额看当年通知。想转计算机建议提前刷编程题，面试会问。",
            "5月份教务系统报名，提交成绩单和个人陈述，然后参加学院考核。热门专业基本要专业前10%才稳。",
        ],
    },
    {
        "category": "学习交流",
        "tag": "保研",
        "variants": [
            ("保研需要哪些条件", "想问保研的基本条件，绩点和六级要求是多少？"),
            ("保研名额怎么算", "保研名额是按专业排名吗，一般前百分之多少？"),
            ("夏令营什么时候报名", "想保研，夏令营一般什么时候开始投？"),
        ],
        "answers": [
            "看前6学期绩点排名和六级，425是硬线，科研竞赛能加分。夏令营4-6月报名，9月推免系统填报，名额一般专业前15%。",
            "保研核心是绩点排名，其次六级和科研。大三下开始准备夏令营，材料包括成绩单、推荐信、个人陈述。",
            "想保研先把绩点稳住，六级尽早过。4-6月投夏令营，9月如果没拿到优营再参加预推免。",
        ],
    },
    {
        "category": "学习交流",
        "tag": "考研",
        "variants": [
            ("考研自习室在哪", "准备考研，学校哪里可以长期自习？"),
            ("图书馆考研座位怎么申请", "听说四楼有考研专区，长期座位怎么申请？"),
            ("晚上哪里可以自习", "教学楼晚上开放到几点，有没有空教室自习？"),
        ],
        "answers": [
            "图书馆4楼有考研专区，9月开放长期座位申请，需要预约。教学楼空教室晚上开到23点，考试周会延长。",
            "长期座位在图书馆4楼，开学初在系统申请，先到先得。平时空教室也能自习，看门口课表挑没课的。",
            "图书馆考研区要抢，建议9月初就关注通知。晚上二教三四楼的空教室人少，22:30关门。",
        ],
    },
    {
        "category": "课程专区",
        "tag": "选课",
        "variants": [
            ("选课系统什么时候开放", "下学期选课什么时候开始？热门课很难抢吗？"),
            ("没抢到课怎么办", "选课没抢到心仪的课，后面还有机会吗？"),
            ("抢课有什么技巧", "选课总抢不过别人，有没有什么技巧？"),
        ],
        "answers": [
            "教务系统整点开放，提前10分钟登录，用校园网更稳。热门课拼手速，没抢到就等第一周退补选，会有人退。",
            "选课在教务系统，开放时间看通知。建议列好备选课，第一周退补选是捡漏的好机会。",
            "抢课前把课程代码准备好，开放瞬间提交。选不上先加候选，开学第一周盯紧退课名额。",
        ],
    },
    {
        "category": "课程专区",
        "tag": "张伟",
        "variants": [
            ("计算机组成原理怎么复习", "期末快到了，计组重点是什么，怎么复习效率高？"),
            ("计组实验报告占多少分", "张老师的计组实验报告占比大吗，要认真写吗？"),
            ("计组期末考试难吗", "想问计组期末难度，往年题重复率高不高？"),
        ],
        "answers": [
            "重点是数据表示、指令系统、流水线和Cache，往年题重复率高。实验报告占30%，考试前把课后题和三年真题刷一遍。",
            "张老师的课重点在流水线和存储层次，计算题必考。建议把PPT例题做熟，实验报告认真写，占分不少。",
            "组成原理复习先过一遍课件，再刷近三年卷子，Cache和流水线是必考大题。实验别缺，报告分好拿。",
        ],
    },
    {
        "category": "课程专区",
        "tag": "李静",
        "variants": [
            ("数据库期末重点", "数据库原理期末考什么，SQL题多吗？"),
            ("数据库课设占比", "李老师数据库课设占多少分，需要做哪些内容？"),
            ("数据库怎么复习", "数据库知识点太杂，求一个复习重点。"),
        ],
        "answers": [
            "SQL大题必考，还有范式分解和事务隔离级别。复习以PPT和近三年卷子为主，实验课设占40%，别拖到最后。",
            "李老师考试偏基础，SQL写查询、ER图转关系模式、范式判断是重点。把实验报告整理好，课设认真做能拉分。",
            "数据库重点：SQL、关系代数、范式、事务和索引。往年题一定要刷，课设占比高，提前两周开始。",
        ],
    },
    {
        "category": "课程专区",
        "tag": "王强",
        "variants": [
            ("操作系统实验难吗", "下学期要上操作系统，实验是基于xv6吗？"),
            ("xv6实验环境怎么配", "操作系统实验的Docker环境怎么搭，有教程吗？"),
            ("操作系统实验报告要求", "实验报告需要写哪些内容，代码要贴吗？"),
        ],
        "answers": [
            "实验基于xv6，环境用课程给的Docker镜像。先做lab1系统调用，报告要有代码、运行截图和分析，抄袭查得很严。",
            "操作系统实验是xv6的lab系列，环境配好Docker就不折腾。每个lab先看懂框架再改，报告按模板写清楚。",
            "实验用xv6，第一次先把环境跑通，lab1加系统调用。遇到编译错误先看Makefile，报告截图一定要有。",
        ],
    },
    {
        "category": "学习交流",
        "tag": "高数",
        "variants": [
            ("高数挂科了怎么办", "高数期末没过，补考和重修有什么区别？"),
            ("高数补考难吗", "高数补考难度怎么样，刷往年题有用吗？"),
            ("重修会覆盖成绩吗", "高数重修后成绩单怎么显示，影响保研吗？"),
        ],
        "answers": [
            "补考难度一般低于期末，把往年补考卷刷熟，重点看老师划的范围。补考通过成绩记60，重修会覆盖但标注。",
            "高数补考以基础题为主，极限、导数、积分三大块拿下就稳。找老师要复习提纲，别只看难题。",
            "先申请补考，然后按期末重点复习，往年题重复不少。补考不过就重修，重修成绩单会有标记。",
        ],
    },
    {
        "category": "院务",
        "tag": "四六级",
        "variants": [
            ("四六级怎么报名", "学校四六级在哪里报名，照片有什么要求？"),
            ("四级没过能考六级吗", "四级还没过，可以同时报六级吗？"),
            ("四六级缺考影响下次吗", "上次四六级缺考了，下次还能报名吗？"),
        ],
        "answers": [
            "教务系统报名，照片要求蓝底证件照。四级425才能报六级，口语自愿。缺考会影响下次报名资格。",
            "报名在教务系统，缴费后才算成功。照片自己传的要蓝底，不符合会被退回，注意截止时间。",
            "四六级每年6月和12月，学校统一在教务系统报名。四级过了才能考六级，缺考下次可能限制报名。",
        ],
    },
    {
        "category": "学习交流",
        "tag": "部署",
        "variants": [
            ("Java项目怎么部署到服务器", "Spring Boot 项目想部署到云服务器，一般怎么搞？"),
            ("前端后端怎么部署", "Vue 前端和 Spring Boot 后端部署到同一台服务器怎么配？"),
            ("服务器部署注意事项", "项目要上线了，部署时有哪些容易踩的坑？"),
        ],
        "answers": [
            "前端 pnpm build 后把 dist 丢给 nginx，后端打 fat jar 用 systemd 托管。注意环境变量、端口和 HTTPS，反向代理别漏了 /api 前缀。",
            "典型部署：nginx 托管前端静态文件并反代 /api 到 127.0.0.1:8080，Spring Boot 用 systemd 起 jar，配置走环境变量。",
            "服务器上先装 JDK 和 nginx，前端构建上传，后端 java -jar 或 systemd。数据库密码用环境变量，别写进 jar。",
        ],
    },
    {
        "category": "学习交流",
        "tag": "SpringBoot",
        "variants": [
            ("Spring Boot启动报错", "项目启动直接失败，日志很长，应该从哪看？"),
            ("Spring Boot连不上数据库", "启动提示数据库连接失败，配置检查过没问题。"),
            ("Boot项目Bean冲突", "启动报 Bean 冲突，是怎么回事？"),
        ],
        "answers": [
            "先看日志里的 Caused by。常见是端口被占用、数据库连不上、Redis/Kafka 没启动。按第一个异常往下查，别只看最后一行。",
            "启动失败九成是依赖没起：MySQL、Redis、Kafka 挨个确认。端口冲突就改 server.port 或杀掉占用进程。",
            "看完整堆栈找 Caused by，数据库连接问题检查 url、账号密码和防火墙；Bean 冲突看是不是重复扫描。",
        ],
    },
    {
        "category": "学习交流",
        "tag": "MySQL",
        "variants": [
            ("MySQL连接失败", "本地项目连 MySQL 报 Access denied，密码没错。"),
            ("MySQL远程连不上", "服务器上的 MySQL 本地能连，远程连不上。"),
            ("MySQL服务启动不了", "MySQL 服务起不来，端口也连不上，怎么排查？"),
        ],
        "answers": [
            "先确认 MySQL 服务在跑、端口3306通不通，再检查账号密码和库名。远程连不上要改 bind-address 并给用户授权。",
            "报 Access denied 是账号密码或权限问题；Connection refused 是服务没起或端口不对。远程连接还要开防火墙。",
            "本地连不上先看服务状态（Windows: net start mysql）；远程连接检查 bind-address=0.0.0.0 和用户的 host 权限。",
        ],
    },
    {
        "category": "学习交流",
        "tag": "Redis",
        "variants": [
            ("Redis连接不上", "Spring Boot 启动报 Redis 连接失败，Redis 明明装了。"),
            ("本地Redis怎么启动", "Windows 上怎么启动 Redis，项目要连 localhost:6379。"),
            ("Redis密码怎么配", "Redis 设置了密码，Spring 配置怎么写？"),
        ],
        "answers": [
            "先 redis-cli ping 看有没有 PONG。没起就启动服务；连不上检查 bind 和 protected-mode，WSL 里启动后 Windows 用 localhost 访问。",
            "Redis 报连接拒绝一般是没启动或端口不对。改配置后记得重启，密码要和应用配置一致。",
            "本地开发可以用 Docker 起 Redis，或者 WSL 里 redis-server。Spring 配置 host/port/password 要和实际一致。",
        ],
    },
    {
        "category": "学习交流",
        "tag": "Git",
        "variants": [
            ("Git冲突怎么解决", "pull 的时候提示冲突，怎么处理才不会丢代码？"),
            ("合并分支冲突了", "merge 之后一堆冲突文件，应该怎么改？"),
            ("能不能force push", "本地和远程不一致，直接 force push 可以吗？"),
        ],
        "answers": [
            "先 git pull --rebase 同步，冲突文件手动解决后 git add，再 git rebase --continue。千万别直接 force push 到 master。",
            "冲突就打开文件找 <<<<<<< 标记，保留需要的内容，删干净标记，git add 后继续。推之前先 pull。",
            "合并冲突不可怕，改完记得编译测试再提交。团队分支上 force push 会覆盖别人代码，禁止。",
        ],
    },
    {
        "category": "求职招募",
        "tag": "简历",
        "variants": [
            ("简历怎么写项目经历", "校招简历里的项目经历怎么写才有亮点？"),
            ("简历一页够吗", "应届生简历写一页还是两页，技术栈放哪里？"),
            ("简历没有实习怎么办", "没有实习经历，简历怎么突出项目？"),
        ],
        "answers": [
            "一页纸，项目用 STAR 法则写：背景、任务、行动、结果，尽量量化。技术栈放显眼位置，不要编造经历。",
            "简历突出项目和实习，写清楚你负责什么、用了什么技术、解决了什么问题、有什么数据结果。",
            "校招简历一页足够，按「基本信息-技能-项目-实习-教育」排。项目要能讲清楚难点，别堆名词。",
        ],
    },
    {
        "category": "求职招募",
        "tag": "实习",
        "variants": [
            ("暑期实习怎么找", "大二升大三，想找暑期实习，应该从哪开始？"),
            ("实习什么时候投简历", "暑期实习一般几月份开始投，渠道有哪些？"),
            ("日常实习值得去吗", "日常实习和暑期实习有什么区别，哪个更适合？"),
        ],
        "answers": [
            "暑期实习3-4月集中投递，牛客、官网和内推都试试。准备八股、算法和项目，小厂练手、大厂冲刺。",
            "日常实习全年都有，暑期实习要提前半年准备。投递渠道：官网、牛客、学长内推，简历投出去后同步刷题。",
            "找实习先定方向，Java后端就准备JVM、并发、MySQL、Redis和项目。3月开始投，别等暑假。",
        ],
    },
    {
        "category": "求职招募",
        "tag": "面试",
        "variants": [
            ("Java后端面试准备", "面试 Java 后端要准备哪些内容，八股怎么背？"),
            ("面试项目怎么讲", "面试官问项目难点，应该怎么回答？"),
            ("面试算法要刷多少题", "校招面试算法大概要刷多少题，刷哪些？"),
        ],
        "answers": [
            "Java后端面试重点：集合、JVM、并发、MySQL索引和事务、Redis缓存、Spring。项目要能画架构图，讲清楚难点和取舍。",
            "一面八股+算法，二面项目深挖。把简历上的每个技术点都准备到能追问三层，算法刷剑指Offer和Hot100。",
            "面试前把项目复盘一遍：为什么这么设计、遇到什么问题、怎么解决。八股别死背，结合项目讲。",
        ],
    },
    {
        "category": "其他",
        "tag": "二手",
        "variants": [
            ("二手电动车去哪买", "想买辆二手电动车代步，学校附近哪里靠谱？"),
            ("买二手电动车注意什么", "买二手电动车主要看什么，电池怎么判断？"),
            ("校内电动车要上牌吗", "现在学校查不查无牌电动车，必须上牌吗？"),
        ],
        "answers": [
            "东门二手市场和闲鱼同城都能买，重点看电池健康、刹车和轮胎。校内电动车要上牌，买之前问清楚有没有牌。",
            "二手电动车建议找认识的人或毕业学长，试骑一圈看续航。电池换新要几百，价格太低要警惕。",
            "闲鱼同城约在校门口试车，检查刹车、灯和电池。学校现在查无牌车，尽量买带牌的。",
        ],
    },
    {
        "category": "其他",
        "tag": "教材",
        "variants": [
            ("二手教材哪里买", "专业课教材太贵，二手教材去哪里收比较全？"),
            ("教材版本怎么确认", "买二手书怕版本不对，怎么确认是老师指定的那版？"),
            ("开学买书攻略", "开学买教材有什么省钱的办法？"),
        ],
        "answers": [
            "开学第一周教材科门口和二手群最集中，闲鱼也能淘。注意版本，问清楚是不是老师指定的那版。",
            "学长学姐群、闲鱼、教材科门口都行，价格一般三到五折。买之前对一下ISBN和版次。",
            "专业课教材建议买二手，公共课用电子版也行。开学前在群里问，毕业季最便宜。",
        ],
    },
    {
        "category": "求职招募",
        "tag": "兼职",
        "variants": [
            ("校内兼职怎么找", "想找校内勤工助学，在哪里申请？"),
            ("校外兼职要注意什么", "找校外兼职怎么避免被骗，有没有靠谱渠道？"),
            ("家教兼职怎么找", "想做家教，学校有推荐的平台吗？"),
        ],
        "answers": [
            "校内勤工助学在学工系统申请，图书馆、实验室助理都有。校外兼职注意签协议，任何要交押金的都别去。",
            "校内岗位关注学工处通知，按月发补贴。校外家教、门店兼职要确认结算方式，保护个人信息。",
            "优先校内勤工助学，安全又不耽误课。校外兼职别信高薪日结，押金和培训费一律不交。",
        ],
    },
    {
        "category": "日常吐槽",
        "tag": "社团",
        "variants": [
            ("社团什么时候招新", "开学想加社团，百团大战一般什么时候？"),
            ("加几个社团比较合适", "社团加太多会不会忙不过来，推荐加几个？"),
            ("技术社团值得加吗", "想学编程，技术类社团能学到东西吗？"),
        ],
        "answers": [
            "9月百团大战在中心广场，选1-2个真正感兴趣的就够。技术类社团项目资源多，但也要看活动频率。",
            "社团招新集中在开学前两周，可以先加群观察。别一次报太多，时间会不够用。",
            "想学技术就找有项目的社团，想放松就选兴趣类。报名后问清楚会费和活动安排。",
        ],
    },
    {
        "category": "其他",
        "tag": "电脑",
        "variants": [
            ("电脑坏了去哪修", "笔记本风扇声音很大，学校哪里能修？"),
            ("清灰换硅脂多少钱", "电脑想清灰换硅脂，学校维修点大概多少钱？"),
            ("保修期电脑去哪修", "还在保修期，去学校维修点还是官方售后？"),
        ],
        "answers": [
            "信息楼B座一楼有维修点，学生价清灰换硅脂50，重装系统30。先让师傅检测再决定，别急着换硬件。",
            "小问题先去信息楼B座维修点，价格透明。保修期内的品牌机建议走官方售后，别自己拆。",
            "电脑进水、黑屏先断电，送信息楼B座检测。清灰、换硬盘这些学生价不贵，数据记得先备份。",
        ],
    },
]

# ---------------------------------------------------------------- 独立帖子
STANDALONE = [
    {
        "category": "日常吐槽", "tag": "早八", "poster": 6,
        "title": "早八真的起不来",
        "content": "闹钟响了五遍还是不想起，到教室已经迟到了十分钟，早八到底怎么坚持啊。",
        "comments": [(12, "同感，早八是一天中最痛苦的时刻", 8), (7, "建议前一晚早点睡，我最近11点睡好多了", 5)],
    },
    {
        "category": "日常吐槽", "tag": "食堂", "poster": 8,
        "title": "二食堂排队排到门外",
        "content": "中午12点去二食堂，队伍直接排到门口了，等了20分钟才打上饭。",
        "comments": [(12, "11点半去会好很多", 7), (6, "高峰期都这样，错峰吧", 3)],
    },
    {
        "category": "日常吐槽", "tag": "图书馆", "poster": 13,
        "title": "图书馆四楼又有人占座",
        "content": "放一本书占一整天，人一直不来，真的很影响别人。",
        "comments": [(7, "可以跟管理员反映", 6), (15, "预约系统签到超过30分钟会释放座位", 8)],
    },
    {
        "category": "日常吐槽", "tag": "宿舍", "poster": 5,
        "title": "宿舍热水又停了",
        "content": "晚上洗澡洗到一半变冷水，后勤说在检修。",
        "comments": [(17, "我们楼也是，说明天恢复", 4), (10, "先去楼下公共浴室吧", 3)],
    },
    {
        "category": "日常吐槽", "tag": "雨天", "poster": 12,
        "title": "下雨天从宿舍走到教学楼",
        "content": "一路全是水坑，鞋全湿了，求推荐防水鞋。",
        "comments": [(8, "买双防泼水的运动鞋就行", 5), (7, "记得带伞，最近雨多", 2)],
    },
    {
        "category": "学习交流", "tag": "体测", "poster": 13,
        "title": "体测800米怎么练",
        "content": "下个月体测，800米每次都跑不下来，求训练方法。",
        "comments": [(3, "每周跑三次，先慢跑两公里，两周就能适应", 15), (16, "跑前热身，跟着队伍节奏别冲太快", 8)],
    },
    {
        "category": "其他", "tag": "失物招领", "poster": 11,
        "title": "捡到一串钥匙",
        "content": "在图书馆一楼捡到一串钥匙，挂着蓝色卡套，失主联系我。",
        "comments": [(8, "帮顶，希望失主看到", 4), (12, "可以交给图书馆前台", 6)],
    },
    {
        "category": "其他", "tag": "理发", "poster": 6,
        "title": "求推荐学校附近理发店",
        "content": "想剪个干净利落的短发，学校附近哪家好？",
        "comments": [(9, "东门那家不错，25块，跟师傅说清楚要求", 9), (5, "别去步行街第一家，剪得不行", 2)],
    },
    {
        "category": "其他", "tag": "二手", "poster": 10,
        "title": "出个27寸显示器",
        "content": "自用显示器，2K分辨率，成色九成新，有意私聊。",
        "comments": [(5, "多少钱出？", 3), (10, "600，可以当面验货", 2)],
    },
    {
        "category": "其他", "tag": "羽毛球", "poster": 15,
        "title": "找羽毛球搭子",
        "content": "每周三晚上体育馆打球，有没有一起的？水平入门。",
        "comments": [(17, "我我我，正好想找人打球", 5), (12, "加一个，周三几点？", 3)],
    },
    {
        "category": "其他", "tag": "笔记软件", "poster": 7,
        "title": "求推荐好用的笔记软件",
        "content": "做课程笔记用，最好能同步和搜索。",
        "comments": [(9, "Notion 和 Obsidian 都不错，Obsidian 本地更快", 12), (2, "课程笔记用 OneNote 也可以", 4)],
    },
    {
        "category": "求职招募", "tag": "开源", "poster": 1,
        "title": "有没有一起做开源项目的",
        "content": "想组队做个校园项目练手，前后端都要，最好能坚持。",
        "comments": [(9, "算我一个，会 Java 后端", 6), (12, "前端可以帮忙", 4)],
    },
    {
        "category": "求职招募", "tag": "内推", "poster": 10,
        "title": "XX科技暑期实习内推",
        "content": "部门在招后端开发实习生，base 杭州，简历发我邮箱。",
        "comments": [(3, "已发，感谢", 3), (11, "请问还招前端吗", 2)],
    },
    {
        "category": "学习交流", "tag": "考研", "poster": 13,
        "title": "找个考研搭子",
        "content": "目标计算机专硕，想找一起自习互相监督的。",
        "comments": [(16, "我也有这个打算，可以一起", 5), (3, "建议固定自习室，效率高", 4)],
    },
    {
        "category": "课程专区", "tag": "李静", "poster": 12,
        "title": "软件工程导论怎么样",
        "content": "这门课给分好吗，平时作业多不多？",
        "comments": [(3, "给分不错，小组项目认真做，90+不难", 10), (7, "作业不算多，但期末要背概念", 4)],
    },
    {
        "category": "课程专区", "tag": "王强", "poster": 11,
        "title": "计算机网络期末重点",
        "content": "网络期末重点是什么，计算题多吗？",
        "comments": [(9, "TCP拥塞控制、子网划分必考，计算题占一半", 13), (16, "往年题重复率挺高，一定刷", 7)],
    },
    {
        "category": "日常吐槽", "tag": "食堂", "poster": 5,
        "title": "食堂阿姨手抖",
        "content": "打菜的时候肉都抖没了，只剩汤。",
        "comments": [(13, "哈哈哈每个学校都有", 6), (8, "可以去人少的窗口，阿姨手稳一点", 3)],
    },
    {
        "category": "其他", "tag": "校园", "poster": 8,
        "title": "校园里那只橘猫",
        "content": "每天在图书馆门口晒太阳，谁在喂它？",
        "comments": [(15, "好像是宿管阿姨在喂", 4), (7, "别喂太多，之前贴了告示", 2)],
    },
    {
        "category": "学习交流", "tag": "编程", "poster": 6,
        "title": "怎么提高编程能力",
        "content": "大一，感觉只会做课后题，不知道怎么提升。",
        "comments": [(1, "多做项目，先把一个完整的 Web 应用做出来", 20), (9, "刷题加看源码，坚持写博客", 10)],
    },
]

# 评分帖：(课程名, 老师, 正文, 评分列表)
RATING_POSTS = [
    ("数据库原理", "李静", "课程内容扎实，实验和课设都很锻炼人。", [5, 5, 4, 5, 4, 5, 4]),
    ("操作系统", "王强", "实验有难度，但能学到东西，老师答疑耐心。", [4, 4, 5, 4, 3, 4, 5]),
    ("计算机组成原理", "张伟", "内容偏硬核，期末考试要认真准备。", [4, 3, 4, 4, 5, 3, 4]),
    ("软件工程", "李静", "小组项目多，给分比较友好。", [5, 4, 5, 4, 5, 4, 5]),
    ("计算机网络", "王强", "知识点多，但老师讲得清楚。", [4, 4, 3, 4, 5, 4, 4]),
    ("高等数学", "张伟", "作业不少，认真刷题能过。", [3, 3, 4, 2, 3, 4, 3]),
    ("大学英语", "李静", "课堂氛围轻松，考试不难。", [5, 4, 4, 5, 5, 4, 4]),
    ("体育", "王强", "选课要注意项目，考核看平时。", [4, 5, 4, 3, 4, 5, 4]),
]

TEACHER_TAGS = [
    ("张伟", "zhangwei@example.edu.cn"),
    ("李静", "lijing@example.edu.cn"),
    ("王强", "wangqiang@example.edu.cn"),
    ("刘敏", "liumin@example.edu.cn"),
    ("陈杰", "chenjie@example.edu.cn"),
]


def q(value: str) -> str:
    return "'" + value.replace("\\", "\\\\").replace("'", "''") + "'"


def pick_answerer(category: str, poster: int, used: set[int]) -> int:
    pool = ANSWER_POOLS.get(category, STUDENTS)
    candidates = [u for u in pool if u != poster and u not in used]
    if not candidates:
        candidates = [u for u in STUDENTS if u != poster]
    return rng.choice(candidates)


def minutes_expr(minutes: int) -> str:
    return f"DATE_SUB(NOW(), INTERVAL {max(minutes, 1)} MINUTE)"


def build() -> str:
    lines: list[str] = []
    add = lines.append

    add("-- 由 docs/seed/generate_rag_seed.py 自动生成，请勿手工修改。")
    add("-- RAG 演示数据：多主题、同义提问、回复中包含标准答案。")
    add("SET NAMES utf8mb4;")
    add("START TRANSACTION;")
    add("")
    add("-- ============ 清理上一次导入的种子数据 ============")
    add("CREATE TEMPORARY TABLE IF NOT EXISTS _seed_users (user_id INT PRIMARY KEY);")
    add("DELETE FROM _seed_users;")
    add("INSERT INTO _seed_users SELECT user_id FROM user WHERE email LIKE 'ragseed_%';")
    add("CREATE TEMPORARY TABLE IF NOT EXISTS _seed_posts (post_id INT PRIMARY KEY);")
    add("DELETE FROM _seed_posts;")
    add("INSERT INTO _seed_posts SELECT post_id FROM post WHERE user_id IN (SELECT user_id FROM _seed_users);")
    add("CREATE TEMPORARY TABLE IF NOT EXISTS _seed_pcomments (pcomment_id INT PRIMARY KEY);")
    add("DELETE FROM _seed_pcomments;")
    add("INSERT INTO _seed_pcomments SELECT pcomment_id FROM pcomment WHERE ptarget_id IN (SELECT post_id FROM _seed_posts) OR user_id IN (SELECT user_id FROM _seed_users);")
    add("CREATE TEMPORARY TABLE IF NOT EXISTS _seed_ccomments (ccomment_id INT PRIMARY KEY);")
    add("DELETE FROM _seed_ccomments;")
    add("INSERT INTO _seed_ccomments SELECT ccomment_id FROM ccomment WHERE ctarget_id IN (SELECT pcomment_id FROM _seed_pcomments) OR user_id IN (SELECT user_id FROM _seed_users);")
    add("DELETE FROM cclike WHERE cctarget_id IN (SELECT ccomment_id FROM _seed_ccomments);")
    add("DELETE FROM ccdeny WHERE cctarget_id IN (SELECT ccomment_id FROM _seed_ccomments);")
    add("DELETE FROM pclike WHERE pctarget_id IN (SELECT pcomment_id FROM _seed_pcomments);")
    add("DELETE FROM pcdeny WHERE pctarget_id IN (SELECT pcomment_id FROM _seed_pcomments);")
    add("DELETE FROM notice WHERE target IN (SELECT post_id FROM _seed_posts) OR target IN (SELECT pcomment_id FROM _seed_pcomments) OR target IN (SELECT ccomment_id FROM _seed_ccomments);")
    add("DELETE FROM rating WHERE item_type = 'post' AND item_id IN (SELECT post_id FROM _seed_posts);")
    add("DELETE FROM plike WHERE ptarget_id IN (SELECT post_id FROM _seed_posts);")
    add("DELETE FROM psave WHERE ptarget_id IN (SELECT post_id FROM _seed_posts);")
    add("DELETE FROM pbrowse WHERE ptarget_id IN (SELECT post_id FROM _seed_posts);")
    add("DELETE FROM ccomment WHERE ccomment_id IN (SELECT ccomment_id FROM _seed_ccomments);")
    add("DELETE FROM pcomment WHERE pcomment_id IN (SELECT pcomment_id FROM _seed_pcomments);")
    add("DELETE FROM post WHERE post_id IN (SELECT post_id FROM _seed_posts);")
    add("DELETE FROM user WHERE email LIKE 'ragseed_%';")
    add("DROP TEMPORARY TABLE _seed_ccomments;")
    add("DROP TEMPORARY TABLE _seed_pcomments;")
    add("DROP TEMPORARY TABLE _seed_posts;")
    add("DROP TEMPORARY TABLE _seed_users;")
    add("")
    add("-- ============ 种子用户 ============")
    for idx, (name, email, intro, identity, score) in enumerate(USERS):
        add(
            "INSERT INTO user (email, password, name, intro, identity, score, avatar_url, punish_num, deleted, is_email_notification_blocked) "
            f"VALUES ({q(email)}, {q(SEED_PASSWORD)}, {q(name)}, {q(intro)}, {q(identity)}, {score}, '', 0, 0, 0);"
        )
        add(f"SET @u{idx} := LAST_INSERT_ID();")
    add("")

    add("-- ============ 课程标签（课程专区/打分帖使用） ============")
    for num, (name, email) in enumerate(TEACHER_TAGS, start=1):
        add(
            "INSERT IGNORE INTO tag (name, value, type, num) "
            f"VALUES ({q(name)}, {q(email)}, 'course', {num});"
        )
    add("")

    # 收集全部帖子（主题帖 + 独立帖 + 评分帖）
    posts: list[dict] = []

    for t_index, topic in enumerate(TOPICS):
        category = topic["category"]
        for v_index, (title, content) in enumerate(topic["variants"]):
            assert len(title) <= 20, f"标题超长: {title} ({len(title)})"
            poster = STUDENTS[(t_index * 3 + v_index) % len(STUDENTS)]
            answerer = pick_answerer(category, poster, set())
            other = pick_answerer(category, poster, {answerer})
            answer = topic["answers"][v_index % len(topic["answers"])]
            comments = [
                (answerer, answer, rng.randint(8, 36)),
                (other, rng.choice(CHATTER[category]), rng.randint(1, 8)),
            ]
            if rng.random() < 0.55:
                third = pick_answerer(category, poster, {answerer, other})
                comments.append((third, topic["answers"][(v_index + 1) % len(topic["answers"])], rng.randint(4, 20)))
            follow = None
            if rng.random() < 0.45:
                follow = (poster, rng.choice(FOLLOW_UPS), rng.randint(0, 4))
            posts.append({
                "kind": "topic", "category": category, "tag": topic["tag"],
                "title": title, "content": content, "poster": poster,
                "comments": comments, "follow": follow,
            })

    for item in STANDALONE:
        assert len(item["title"]) <= 20, f"标题超长: {item['title']}"
        comments = []
        for author, text, like in item["comments"]:
            comments.append((author, text, like))
        follow = (item["poster"], rng.choice(FOLLOW_UPS), rng.randint(0, 3)) if rng.random() < 0.3 else None
        posts.append({
            "kind": "standalone", "category": item["category"], "tag": item["tag"],
            "title": item["title"], "content": item["content"], "poster": item["poster"],
            "comments": comments, "follow": follow,
        })

    for course, teacher, content, scores in RATING_POSTS:
        assert len(course) <= 20
        poster = rng.choice(STUDENTS)
        comments = [
            (rng.choice(ANSWER_POOLS["打分"]), "这门课我也上过，整体体验和楼主差不多。", rng.randint(3, 12)),
            (rng.choice(STUDENTS), rng.choice(CHATTER["打分"]), rng.randint(1, 6)),
        ]
        posts.append({
            "kind": "rating", "category": "打分", "tag": teacher,
            "title": course, "content": content, "poster": poster,
            "comments": comments, "follow": None, "scores": scores,
        })

    # 打乱后按时间从旧到新分配发布时间
    rng.shuffle(posts)
    total = len(posts)
    max_age = 120 * 24 * 60  # 120 天前
    ages: list[int] = []
    for i in range(total):
        base = int(max_age * (1 - i / max(total - 1, 1)) ** 1.15)
        ages.append(max(base + rng.randint(0, 600), 30))
    ages.sort(reverse=True)

    add("-- ============ 帖子 / 评论 / 二级回复 / 评分 ============")
    p_index = 0
    for post, age in zip(posts, ages):
        poster = post["poster"]
        comments = post["comments"]
        total_comments = len(comments) + (1 if post["follow"] else 0)
        browse = rng.randint(90, 900) if rng.random() < 0.6 else rng.randint(6, 60)
        like = int(browse * rng.uniform(0.03, 0.18))
        heat = round(browse * 1.0 + total_comments * 6 + like * 3 + rng.randint(0, 20), 1)
        add("")
        add(f"-- {post['title']}")
        add(
            "INSERT INTO post (user_id, category, title, ptext, comment_num, like_num, browse_num, post_time, heat, photos, tag) "
            f"VALUES (@u{poster}, {q(post['category'])}, {q(post['title'])}, {q(post['content'])}, "
            f"{total_comments}, {like}, {browse}, {minutes_expr(age)}, {heat}, '', {q(post['tag'])});"
        )
        add(f"SET @p{p_index} := LAST_INSERT_ID();")

        c_index = 0
        for c_author, c_text, c_like in comments:
            comment_age = max(age - rng.randint(3, 240), 1)
            add(
                "INSERT INTO pcomment (user_id, ptarget_id, like_num, deny_num, pctext, time) "
                f"VALUES (@u{c_author}, @p{p_index}, {c_like}, 0, {q(c_text)}, {minutes_expr(comment_age)});"
            )
            add(f"SET @c{p_index}_{c_index} := LAST_INSERT_ID();")
            c_index += 1

        if post["follow"]:
            f_author, f_text, f_like = post["follow"]
            # 回复第一条评论（标准答案）
            first_author = comments[0][0]
            follow_age = max(age - rng.randint(200, 500), 1)
            add(
                "INSERT INTO ccomment (user_id, ctarget_id, like_num, deny_num, cctext, time, usertargetName) "
                f"VALUES (@u{f_author}, @c{p_index}_0, {f_like}, 0, {q(f_text)}, {minutes_expr(follow_age)}, {q(USERS[first_author][0])});"
            )

        if post["kind"] == "rating":
            for score in post["scores"]:
                rater = rng.choice([u for u in STUDENTS if u != poster])
                add(
                    "INSERT IGNORE INTO rating (user_id, item_type, item_id, score, created_time, updated_time) "
                    f"VALUES (@u{rater}, 'post', @p{p_index}, {score}, {minutes_expr(max(age - 600, 1))}, {minutes_expr(max(age - 600, 1))});"
                )
        p_index += 1

    add("")
    add("COMMIT;")
    add("")
    add("-- ============ 导入结果校验 ============")
    add("SELECT '种子用户' AS item, COUNT(*) AS cnt FROM user WHERE email LIKE 'ragseed_%'")
    add("UNION ALL SELECT '种子帖子', COUNT(*) FROM post WHERE user_id IN (SELECT user_id FROM user WHERE email LIKE 'ragseed_%')")
    add("UNION ALL SELECT '种子一级评论', COUNT(*) FROM pcomment WHERE user_id IN (SELECT user_id FROM user WHERE email LIKE 'ragseed_%')")
    add("UNION ALL SELECT '种子二级评论', COUNT(*) FROM ccomment WHERE user_id IN (SELECT user_id FROM user WHERE email LIKE 'ragseed_%')")
    add("UNION ALL SELECT '种子评分', COUNT(*) FROM rating WHERE item_type = 'post' AND item_id IN (SELECT post_id FROM post WHERE user_id IN (SELECT user_id FROM user WHERE email LIKE 'ragseed_%'));")
    add("")

    return "\n".join(lines)


def main() -> None:
    sql = build()
    out = Path(__file__).with_name("rag_seed.sql")
    out.write_text(sql, encoding="utf-8")
    print(f"已生成 {out}，{len(sql.splitlines())} 行")


if __name__ == "__main__":
    main()
