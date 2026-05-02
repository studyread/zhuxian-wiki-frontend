-- MySQL dump 10.13  Distrib 8.0.31, for Win64 (x86_64)
--
-- Host: localhost    Database: zhuxian_wiki
-- ------------------------------------------------------
-- Server version	5.7.24

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `role` varchar(20) DEFAULT 'admin',
  `status` int(11) DEFAULT '1',
  `last_login_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E','系统管理员','admin',1,'2026-04-28 01:41:35','2026-04-28 01:41:35','2026-04-28 01:41:35');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `summary` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `cover_image` longtext COLLATE utf8mb4_unicode_ci,
  `category_id` bigint(20) DEFAULT NULL,
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `author_id` bigint(20) DEFAULT NULL,
  `view_count` int(11) DEFAULT '0',
  `like_count` int(11) DEFAULT '0',
  `comment_count` int(11) DEFAULT '0',
  `status` int(11) DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created` (`created_at`),
  FULLTEXT KEY `ft_search` (`title`,`content`,`tags`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES 
(1,'青云门技能加点攻略详解','青云门是诛仙世界中的远程法术输出职业，以高爆发和群体伤害著称。本文详细介绍技能加点方案。','青云门是诛仙世界中的远程法术输出职业，以高爆发和群体伤害著称。\r\n\r\n## 技能加点推荐\r\n\r\n### 前期加点（1-40级）\r\n- 主加：神剑御雷真诀\r\n- 副加：七劫斩龙诀\r\n- 辅助：天玄冰\r\n\r\n### 中期加点（40-70级）\r\n- 主加：神剑御雷真诀、七劫斩龙诀\r\n- 副加：天诛剑气\r\n- 辅助：天玄冰、真元护体\r\n\r\n### 后期加点（70级以上）\r\n- PVE：神剑御雷真诀满、七劫斩龙诀满、天诛剑气满\r\n- PVP：根据实际情况调整控制技能\r\n\r\n## 装备选择\r\n- 武器：优先选择法术攻击高的法剑\r\n- 防具：选择增加法术暴击和命中的装备\r\n- 饰品：优先法术攻击和暴击率\r\n\r\n## 实战技巧\r\n1. 保持安全距离输出\r\n2. 合理使用控制技能\r\n3. 注意蓝量管理\r\n4. 团队配合优先',NULL,7,'青云门,技能,加点',NULL,1532,1,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(2,'天音寺副本攻略','天音寺副本是诛仙世界中的经典副本之一，适合30-50级玩家挑战。','天音寺副本是诛仙世界中的经典副本之一，适合30-50级玩家挑战。\r\n\r\n## 副本信息\r\n\r\n### 基本信息\r\n- 推荐等级：35级\r\n- 队伍人数：5人\r\n- 副本难度：中等\r\n- 通关时间：30-40分钟\r\n\r\n### BOSS介绍\r\n\r\n#### 第一个BOSS：护法金刚\r\n- 技能：金刚伏魔、佛光普照\r\n- 打法：注意躲避范围技能，优先击杀召唤的小怪\r\n\r\n#### 第二个BOSS：戒律院首座\r\n- 技能：戒律惩戒、佛法无边\r\n- 打法：需要打断读条技能，注意团队血量\r\n\r\n#### 最终BOSS：天音主持\r\n- 技能：天音波、大慈大悲咒、万佛朝宗\r\n- 打法：\r\n  1. 分散站位避免AOE\r\n  2. 及时打断万佛朝宗\r\n  3. 注意躲避天音波\r\n\r\n## 掉落物品\r\n- 天音寺套装部件\r\n- 高级强化石\r\n- 稀有材料\r\n\r\n## 注意事项\r\n1. 准备足够的药品\r\n2. 队伍配置要合理\r\n3. 注意BOSS技能预警',NULL,13,'天音寺,副本,攻略',NULL,984,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(3,'新手快速升级指南','刚进入诛仙世界的新手玩家，如何快速提升等级？本攻略为你详细介绍。','刚进入诛仙世界的新手玩家，如何快速提升等级？本攻略为你详细介绍。\r\n\r\n## 升级路线\r\n\r\n### 1-20级\r\n- 跟随主线任务\r\n- 熟悉游戏基本操作\r\n- 了解职业技能\r\n\r\n### 20-40级\r\n- 继续主线任务\r\n- 完成每日日常任务\r\n- 参与简单副本\r\n\r\n### 40-60级\r\n- 主线+支线任务并行\r\n- 组队刷副本\r\n- 参与活动玩法\r\n\r\n### 60级以上\r\n- 挑战高级副本\r\n- 参与PVP玩法\r\n- 完成成就任务\r\n\r\n## 经验获取途径\r\n\r\n### 任务类\r\n- 主线任务：经验最丰厚\r\n- 支线任务：补充经验\r\n- 日常任务：稳定经验来源\r\n- 周常任务：大量经验\r\n\r\n### 活动类\r\n- 限时活动：双倍经验时间\r\n- 帮派活动：额外经验奖励\r\n- 世界BOSS：高额经验\r\n\r\n## 升级技巧\r\n1. 优先完成主线任务\r\n2. 合理利用双倍经验\r\n3. 组队效率更高\r\n4. 注意活动时间\r\n5. 使用经验加成道具',NULL,16,'新手,升级,攻略',NULL,2157,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(4,'装备强化系统详解','装备强化是提升战力的重要途径，本文详细介绍强化系统的各个方面。','装备强化是提升战力的重要途径，本文详细介绍强化系统的各个方面。\r\n\r\n## 强化基础\r\n\r\n### 强化等级\r\n- +1到+10：初级强化，成功率较高\r\n- +11到+15：中级强化，需要保护符\r\n- +16以上：高级强化，风险与收益并存\r\n\r\n### 强化材料\r\n- 强化石：基础材料\r\n- 高级强化石：高等级强化使用\r\n- 保护符：防止强化失败降级\r\n- 幸运符：提高成功率\r\n\r\n## 强化技巧\r\n\r\n### 成功率提升\r\n1. 使用幸运符\r\n2. 选择吉时（游戏内时间）\r\n3. 垫刀技巧\r\n\r\n### 风险控制\r\n1. 高等级强化使用保护符\r\n2. 准备备用装备\r\n3. 不要贪心，适度强化\r\n\r\n## 各部位强化优先级\r\n1. 武器：优先强化，提升攻击力\r\n2. 衣服：增加生存能力\r\n3. 首饰：根据职业选择\r\n4. 其他：均衡发展\r\n\r\n## 强化注意事项\r\n- 强化有失败概率\r\n- 高等级强化可能降级\r\n- 强化属性与装备品质相关\r\n- 绑定装备强化后仍然绑定',NULL,25,'装备,强化,攻略',NULL,3459,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(5,'伙伴培养全攻略','伙伴系统是诛仙世界的重要玩法，好的伙伴能大幅提升战斗力。','伙伴系统是诛仙世界的重要玩法，好的伙伴能大幅提升战斗力。\r\n\r\n## 伙伴获取\r\n\r\n### 获取途径\r\n- 主线任务：获得基础伙伴\r\n- 招募系统：消耗招募令\r\n- 活动奖励：限时活动\r\n- 商城购买：直接获得\r\n\r\n### 伙伴品质\r\n- 普通（白色）\r\n- 优秀（绿色）\r\n- 精良（蓝色）\r\n- 史诗（紫色）\r\n- 传说（橙色）\r\n\r\n## 伙伴培养\r\n\r\n### 升级\r\n- 使用经验丹\r\n- 参与战斗获得经验\r\n- 品质越高成长越好\r\n\r\n### 升星\r\n- 消耗同名伙伴碎片\r\n- 提升基础属性\r\n- 解锁新技能\r\n\r\n### 技能\r\n- 主动技能：战斗中释放\r\n- 被动技能：持续生效\r\n- 技能升级：消耗技能书\r\n\r\n### 装备\r\n- 伙伴可以穿戴装备\r\n- 装备强化与主角独立\r\n- 专属装备有额外加成\r\n\r\n## 伙伴搭配\r\n\r\n### 职业搭配\r\n- 输出型伙伴：补充伤害\r\n- 坦克型伙伴：吸收伤害\r\n- 辅助型伙伴：治疗增益\r\n\r\n### 阵容推荐\r\n1. 均衡型：1输出+1坦克+1辅助\r\n2. 输出型：2输出+1辅助\r\n3. 防御型：1输出+2坦克\r\n\r\n## 注意事项\r\n- 伙伴有上阵数量限制\r\n- 注意伙伴羁绊效果\r\n- 及时更换高品质伙伴\r\n- 合理分配培养资源',NULL,27,'伙伴,培养,攻略',NULL,2890,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(6,'PVP竞技场技巧','竞技场是展示实力的舞台，掌握这些技巧帮你提升排名。','竞技场是展示实力的舞台，掌握这些技巧帮你提升排名。\r\n\r\n## 竞技场基础\r\n\r\n### 规则说明\r\n- 每日免费挑战次数：10次\r\n- 可以购买额外次数\r\n- 排名越高奖励越丰厚\r\n- 赛季结算有额外奖励\r\n\r\n### 匹配机制\r\n- 根据排名匹配对手\r\n- 可以刷新对手\r\n- 注意对手战力评估\r\n\r\n## 职业对战技巧\r\n\r\n### 青云门\r\n- 优势：远程爆发\r\n- 打法：保持距离，先手控制\r\n- 注意：防止被近身\r\n\r\n### 天音寺\r\n- 优势：治疗续航\r\n- 打法：消耗战，持久战\r\n- 注意：打断治疗技能\r\n\r\n### 鬼王宗\r\n- 优势：近战爆发\r\n- 打法：快速近身，一套带走\r\n- 注意：技能冷却时间\r\n\r\n### 合欢派\r\n- 优势：控制技能多\r\n- 打法：控制链，持续输出\r\n- 注意：解控技能使用\r\n\r\n## 通用技巧\r\n1. 了解各职业特点\r\n2. 合理使用药品\r\n3. 注意技能CD管理\r\n4. 观察对手习惯\r\n5. 利用地形优势\r\n\r\n## 阵容搭配（组队竞技）\r\n- 1输出+1控制+1治疗\r\n- 2输出+1治疗\r\n- 根据对手调整阵容\r\n\r\n## 奖励说明\r\n- 每日排名奖励\r\n- 赛季结算奖励\r\n- 荣誉点兑换物品',NULL,18,'PVP,竞技场,攻略',NULL,1877,1,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(7,'鬼王宗连招教学','鬼王宗是诛仙世界中的近战物理输出职业，以高爆发和强控制著称。','鬼王宗是诛仙世界中的近战物理输出职业，以高爆发和强控制著称。\r\n\r\n## 基础连招\r\n\r\n### 起手连招\r\n1. 鬼影步 → 接近目标\r\n2. 鬼斩 → 主要输出技能\r\n3. 鬼爪 → 控制技能\r\n4. 鬼舞 → 持续输出\r\n\r\n### 爆发连招\r\n1. 鬼影步 → 鬼斩 → 鬼爪\r\n2. 鬼王降临（大招）\r\n3. 鬼舞 → 鬼斩 → 普攻\r\n\r\n## PVE连招\r\n\r\n### 单体输出\r\n鬼影步 → 鬼斩 → 鬼爪 → 鬼舞 → 普攻循环\r\n\r\n### 群体输出\r\n鬼影步 → 鬼舞 → 鬼斩 → 鬼爪 → 鬼舞\r\n\r\n## PVP连招\r\n\r\n### 对战远程\r\n鬼影步 → 鬼爪（控制）→ 鬼斩 → 鬼舞 → 追击\r\n\r\n### 对战近战\r\n鬼爪（先手控制）→ 鬼斩 → 鬼舞 → 鬼影步（追击或撤退）\r\n\r\n## 技能要点\r\n- 鬼影步：位移+伤害，核心技能\r\n- 鬼斩：主要输出技能\r\n- 鬼爪：控制技能，打断对手\r\n- 鬼舞：AOE技能，持续伤害\r\n- 鬼王降临：大招，爆发技能\r\n\r\n## 注意事项\r\n1. 注意技能CD衔接\r\n2. 合理使用鬼影步位移\r\n3. 鬼爪要准确命中\r\n4. 大招时机要把握好',NULL,9,'鬼王宗,连招,攻略',NULL,2134,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(8,'日常任务完成指南','日常任务是稳定获取经验和资源的重要途径，合理安排时间高效完成。','日常任务是稳定获取经验和资源的重要途径，合理安排时间高效完成。\r\n\r\n## 每日必做任务\r\n\r\n### 经验类\r\n- 师门任务：20环，大量经验\r\n- 帮派任务：10环，经验+帮贡\r\n- 悬赏任务：5次，经验+银两\r\n\r\n### 资源类\r\n- 副本挑战：装备材料\r\n- 竞技场：荣誉点\r\n- 世界BOSS：稀有材料\r\n\r\n### 社交类\r\n- 好友互动：友情点\r\n- 帮派活动：帮贡\r\n- 组队副本：额外奖励\r\n\r\n## 任务优先级\r\n1. 师门任务（经验最多）\r\n2. 限时活动（时间限制）\r\n3. 副本挑战（装备需求）\r\n4. 其他日常\r\n\r\n## 高效完成技巧\r\n1. 组队完成效率更高\r\n2. 合理规划路线\r\n3. 使用自动寻路\r\n4. 准备足够的药品\r\n5. 利用双倍经验时间\r\n\r\n## 任务时间规划\r\n- 早上：师门任务、悬赏任务\r\n- 中午：限时活动\r\n- 晚上：副本、世界BOSS\r\n- 碎片时间：其他日常\r\n\r\n## 注意事项\r\n- 每日0点重置\r\n- 部分任务有等级限制\r\n- 注意活动时间安排\r\n- 不要错过限时活动',NULL,21,'日常,任务,攻略',NULL,1567,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(9,'合欢派控制链打法','合欢派是诛仙世界中的控制型职业，擅长各种控制技能，是PVP中的强力职业。','合欢派是诛仙世界中的控制型职业，擅长各种控制技能，是PVP中的强力职业。\r\n\r\n## 控制技能介绍\r\n\r\n### 魅惑\r\n- 效果：使目标无法攻击，走向施法者\r\n- 持续时间：3秒\r\n- 冷却时间：12秒\r\n\r\n### 沉睡\r\n- 效果：使目标进入睡眠状态\r\n- 持续时间：4秒\r\n- 特点：受到伤害会醒来\r\n\r\n### 定身\r\n- 效果：使目标无法移动\r\n- 持续时间：3秒\r\n- 可以攻击和施法\r\n\r\n### 眩晕\r\n- 效果：使目标无法行动\r\n- 持续时间：2秒\r\n- 最强控制效果\r\n\r\n## 控制链连招\r\n\r\n### 基础控制链\r\n魅惑 → 输出 → 沉睡 → 输出 → 定身 → 输出\r\n\r\n### 进阶控制链\r\n魅惑 → 接近 → 沉睡 → 爆发 → 眩晕 → 继续输出\r\n\r\n### PVP控制链\r\n1. 魅惑起手\r\n2. 接近目标\r\n3. 沉睡控制\r\n4. 释放高伤害技能\r\n5. 定身防止逃跑\r\n6. 眩晕收尾\r\n\r\n## 对战各职业\r\n\r\n### 对战青云门\r\n- 利用魅惑接近\r\n- 沉睡打断施法\r\n- 注意保持距离\r\n\r\n### 对战鬼王宗\r\n- 魅惑控制后拉开距离\r\n- 定身防止近身\r\n- 眩晕打断连招\r\n\r\n### 对战天音寺\r\n- 沉睡打断治疗\r\n- 控制链要连贯\r\n- 爆发输出\r\n\r\n## 注意事项\r\n1. 控制技能CD要记好\r\n2. 不要浪费控制技能\r\n3. 注意对手的解控技能\r\n4. 控制链要连贯\r\n5. 配合队友控制',NULL,10,'合欢派,控制,PVP',NULL,1987,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(10,'装备获取途径汇总','好的装备是提升战力的基础，本文汇总所有装备获取途径。','好的装备是提升战力的基础，本文汇总所有装备获取途径。\r\n\r\n## 副本掉落\r\n\r\n### 普通副本\r\n- 掉落：绿色、蓝色装备\r\n- 适合：新手玩家\r\n- 难度：简单\r\n\r\n### 精英副本\r\n- 掉落：蓝色、紫色装备\r\n- 适合：中期玩家\r\n- 难度：中等\r\n\r\n### 团队副本\r\n- 掉落：紫色、橙色装备\r\n- 适合：高级玩家\r\n- 难度：困难\r\n\r\n## 任务奖励\r\n\r\n### 主线任务\r\n- 奖励：对应等级装备\r\n- 特点：必做，装备跟上等级\r\n\r\n### 支线任务\r\n- 奖励：随机装备\r\n- 特点：补充装备来源\r\n\r\n### 成就奖励\r\n- 奖励：特殊装备\r\n- 特点：属性独特\r\n\r\n## 打造系统\r\n\r\n### 材料收集\r\n- 副本掉落\r\n- 任务奖励\r\n- 商城购买\r\n- 玩家交易\r\n\r\n### 打造流程\r\n1. 收集材料\r\n2. 学习图纸\r\n3. 找NPC打造\r\n4. 强化升级\r\n\r\n## 商城购买\r\n\r\n### 元宝商城\r\n- 高级装备\r\n- 限时装备\r\n- 外观装备\r\n\r\n### 银两商城\r\n- 基础装备\r\n- 强化材料\r\n\r\n### 荣誉商店\r\n- PVP装备\r\n- 需要荣誉点\r\n\r\n## 活动获取\r\n\r\n### 限时活动\r\n- 节日活动装备\r\n- 属性独特\r\n- 外观精美\r\n\r\n### 签到奖励\r\n- 累计签到装备\r\n- 新手福利\r\n\r\n## 交易行\r\n- 玩家间交易\r\n- 注意价格波动\r\n- 可以淘到好装备',NULL,22,'装备,获取,攻略',NULL,2679,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(11,'帮派系统玩法介绍','帮派是诛仙世界中重要的社交系统，加入帮派可以获得很多福利。','帮派是诛仙世界中重要的社交系统，加入帮派可以获得很多福利。\r\n\r\n## 加入帮派\r\n\r\n### 申请加入\r\n- 查找帮派列表\r\n- 提交申请\r\n- 等待审核\r\n\r\n### 创建帮派\r\n- 需要达到一定等级\r\n- 消耗创建资金\r\n- 招募成员\r\n\r\n## 帮派福利\r\n\r\n### 每日福利\r\n- 帮派工资\r\n- 帮派技能\r\n- 帮派商店\r\n\r\n### 帮派技能\r\n- 攻击加成\r\n- 防御加成\r\n- 生命加成\r\n- 需要帮贡升级\r\n\r\n### 帮派商店\r\n- 稀有材料\r\n- 特殊道具\r\n- 需要帮贡购买\r\n\r\n## 帮派活动\r\n\r\n### 帮派任务\r\n- 每日10环\r\n- 经验+帮贡\r\n- 帮派资金\r\n\r\n### 帮派战\r\n- 每周固定时间\r\n- 帮派间对抗\r\n- 丰厚奖励\r\n\r\n### 帮派副本\r\n- 专属副本\r\n- 高级装备\r\n- 需要帮派等级\r\n\r\n### 帮派BOSS\r\n- 每日刷新\r\n- 全帮参与\r\n- 掉落稀有物品\r\n\r\n## 帮贡获取\r\n- 完成帮派任务\r\n- 参与帮派活动\r\n- 捐献资金\r\n- 捐献材料\r\n\r\n## 注意事项\r\n1. 选择活跃的帮派\r\n2. 积极参与帮派活动\r\n3. 按时完成帮派任务\r\n4. 与帮派成员友好相处\r\n5. 帮派等级越高福利越好',NULL,26,'帮派,系统,攻略',NULL,1456,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35'),
(12,'云梦宗治疗手法','云梦宗是诛仙世界中的治疗职业，是团队中不可或缺的角色。','云梦宗是诛仙世界中的治疗职业，是团队中不可或缺的角色。\r\n\r\n## 治疗技能介绍\r\n\r\n### 单体治疗\r\n- 回春术：基础治疗，CD短\r\n- 治愈术：中等治疗，主要技能\r\n- 圣光术：大治疗，CD较长\r\n\r\n### 群体治疗\r\n- 群体治愈：小队回血\r\n- 治疗之雨：范围持续回血\r\n- 圣光普照：大招，全队大量回血\r\n\r\n### 辅助技能\r\n- 护盾：吸收伤害\r\n- 净化：解除负面状态\r\n- 复活：复活死亡队友\r\n\r\n## 治疗手法\r\n\r\n### 单体治疗手法\r\n1. 保持回春术持续\r\n2. 血量下降用治愈术\r\n3. 危急时刻用圣光术\r\n4. 预判伤害提前治疗\r\n\r\n### 群体治疗手法\r\n1. 保持治疗之雨\r\n2. 群体掉血用群体治愈\r\n3. 大招留到关键时刻\r\n4. 注意技能范围\r\n\r\n### PVP治疗手法\r\n- 优先治疗自己\r\n- 注意走位躲避\r\n- 及时净化控制\r\n- 保护核心输出\r\n\r\n## 属性选择\r\n- 优先：治疗效果\r\n- 其次：法力值\r\n- 然后：防御属性\r\n- 最后：速度\r\n\r\n## 装备选择\r\n- 武器：治疗加成\r\n- 衣服：防御+生命\r\n- 首饰：法力+治疗\r\n\r\n## 注意事项\r\n1. 注意蓝量管理\r\n2. 预判队友掉血\r\n3. 优先治疗核心成员\r\n4. 注意自身安全\r\n5. 与队友保持沟通',NULL,12,'云梦宗,治疗,攻略',NULL,1234,0,0,1,'2026-04-28 01:41:35','2026-04-28 01:41:35');
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-30  2:44:00
