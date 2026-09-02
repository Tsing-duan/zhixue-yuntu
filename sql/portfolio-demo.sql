-- 智学云途私有招聘作品 Demo 数据
-- 本文件由已脱敏工作树派生，仅完成静态闭包检查。
-- DB_RUNTIME_UNVERIFIED：尚未执行 MySQL 实际导入。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ape_account
-- ----------------------------
DROP TABLE IF EXISTS `ape_account`;
CREATE TABLE `ape_account`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `type` int(2) NULL DEFAULT NULL COMMENT '类型',
  `state` int(2) NULL DEFAULT NULL COMMENT '状态（1关闭）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_ai_history
-- ----------------------------
DROP TABLE IF EXISTS `ape_ai_history`;
CREATE TABLE `ape_ai_history` (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `agent_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Agent 类型：chat/study-plan/wrong-agent/lesson-prepare/course-summary',
  `user_input` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '用户输入',
  `ai_reply` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'AI 回复',
  `context_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '生成时的平台上下文快照',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ai_history_user_agent_time` (`user_id`, `agent_type`, `create_time`) USING BTREE,
  KEY `idx_ai_history_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI 生成历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_article
-- ----------------------------
DROP TABLE IF EXISTS `ape_article`;
CREATE TABLE `ape_article`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程id',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '头像',
  `article_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `state` int(2) NULL DEFAULT 2 COMMENT '状态 0：通过 1：未通过  2：审核中 ',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程名称',
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '笔记' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_article_comment
-- ----------------------------
DROP TABLE IF EXISTS `ape_article_comment`;
CREATE TABLE `ape_article_comment`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程主键',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '笔记评论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_article_favor
-- ----------------------------
DROP TABLE IF EXISTS `ape_article_favor`;
CREATE TABLE `ape_article_favor`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `article_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '笔记id',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '笔记收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_chapter
-- ----------------------------
DROP TABLE IF EXISTS `ape_chapter`;
CREATE TABLE `ape_chapter`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程id',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程名称',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节名称',
  `video_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '视频名称',
  `video` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '视频',
  `courseware_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课件名称',
  `courseware` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '课件',
  `homework` int(2) NULL DEFAULT 0 COMMENT '课程作业是否存在  0:否',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '章节' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_chapter
INSERT INTO `ape_chapter` VALUES ('1730032800929447937', '1729751427215790082', 'Portfolio Demo Course', 'Portfolio Demo Chapter', '', '', '', '', 0, 'Synthetic portfolio fixture', 'portfolio-demo', '2026-03-30 09:15:37', 'portfolio-demo', '2026-03-30 09:15:37');

-- ----------------------------
-- Table structure for ape_chapter_video
-- ----------------------------
DROP TABLE IF EXISTS `ape_chapter_video`;
CREATE TABLE `ape_chapter_video`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `chapter_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节id',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '章节视频是否观看' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_classification
-- ----------------------------
DROP TABLE IF EXISTS `ape_classification`;
CREATE TABLE `ape_classification`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分类' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_classification
INSERT INTO `ape_classification` VALUES ('1725410802936901634', 'Demo Classification');

-- ----------------------------
-- Table structure for ape_dept
-- ----------------------------
DROP TABLE IF EXISTS `ape_dept`;
CREATE TABLE `ape_dept`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `id_arrary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主键数组',
  `parent_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上级id',
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门名称',
  `order_num` int(10) NULL DEFAULT NULL COMMENT '显示顺序',
  `leader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` int(2) NULL DEFAULT NULL COMMENT '部门状态（0正常 1停用）',
  `del_flag` int(2) NULL DEFAULT 0 COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_dept
INSERT INTO `ape_dept` VALUES ('100', '0', '0', 'study-platform', 1, 'Portfolio Demo Admin 01', 'demo-phone-001', 'redacted-001@example.invalid', 0, 0, 'portfolio_admin_01', '2023-03-06 10:01:25', 'Portfolio Demo Admin 01', '2026-03-29 09:50:41');

-- ----------------------------
-- Table structure for ape_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `ape_dict_data`;
CREATE TABLE `ape_dict_data`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典编码',
  `dict_sort` int(4) NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `status` int(2) NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_dict_data
INSERT INTO `ape_dict_data` VALUES ('1711306026204889089', 'sex', 1, '男', '0', '', '', 0, 'Portfolio Demo Admin 01', '2023-10-09 17:02:06', 'Portfolio Demo Admin 01', '2023-10-09 17:02:06', '');
INSERT INTO `ape_dict_data` VALUES ('1711306054076039169', 'sex', 1, '女', '1', '', '', 0, 'Portfolio Demo Admin 01', '2023-10-09 17:02:12', 'Portfolio Demo Admin 01', '2023-10-09 17:02:12', '');

-- ----------------------------
-- Table structure for ape_forum
-- ----------------------------
DROP TABLE IF EXISTS `ape_forum`;
CREATE TABLE `ape_forum`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '讨论名字',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '讨论内容',
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `state` int(2) NULL DEFAULT 2 COMMENT '状态 0：通过 1：未通过 2：审核中',
  `audit_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `audit_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '论坛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_forum_item
-- ----------------------------
DROP TABLE IF EXISTS `ape_forum_item`;
CREATE TABLE `ape_forum_item`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `forum_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '论坛id',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名称',
  `user_avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '用户头像',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `state` int(2) NULL DEFAULT 2 COMMENT '状态 0：通过 1：未通过 2：审核中',
  `audit_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime(0) NULL DEFAULT NULL COMMENT '审核时间',
  `audit_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '论坛讨论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_gen_table
-- ----------------------------
DROP TABLE IF EXISTS `ape_gen_table`;
CREATE TABLE `ape_gen_table`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表描述',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作 sub主子表操作）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` int(2) NULL DEFAULT 0 COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `ape_gen_table_column`;
CREATE TABLE `ape_gen_table_column`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编号',
  `table_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` int(2) NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_required` int(2) NULL DEFAULT 0 COMMENT '是否必填（1是）',
  `is_insert` int(2) NULL DEFAULT 1 COMMENT '是否为插入字段（1是）',
  `is_edit` int(2) NULL DEFAULT 1 COMMENT '是否编辑字段（1是）',
  `is_list` int(2) NULL DEFAULT 1 COMMENT '是否列表字段（1是）',
  `is_query` int(2) NULL DEFAULT 1 COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '文本框' COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '字典类型',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_homework
-- ----------------------------
DROP TABLE IF EXISTS `ape_homework`;
CREATE TABLE `ape_homework`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `chapter_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节id',
  `chapter_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节名称',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目',
  `sort` int(5) NULL DEFAULT NULL COMMENT '序号',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '答案',
  `type` int(2) NULL DEFAULT NULL COMMENT '类型 0：单选 1：多选 2：填空 3：判断',
  `score` int(5) NULL DEFAULT NULL COMMENT '分数',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目内容',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '作业' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_homework
INSERT INTO `ape_homework` VALUES ('1730040079170961409', '1730032800929447937', 'Portfolio Demo Chapter', 'Which option is the correct demo answer?', '1', 'A', '0', '1', '[{"value":"A","option":"Correct demo option"},{"value":"B","option":"Incorrect demo option"}]', 'Synthetic portfolio fixture', 'portfolio-demo', '2026-03-18 09:44:32', 'portfolio-demo', '2026-03-18 09:44:32');

-- ----------------------------
-- Table structure for ape_homework_student
-- ----------------------------
DROP TABLE IF EXISTS `ape_homework_student`;
CREATE TABLE `ape_homework_student`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `chapter_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节id',
  `chapter_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节名称',
  `work_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作业题目id',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目',
  `sort` int(5) NULL DEFAULT NULL COMMENT '序号',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '答案',
  `type` int(2) NULL DEFAULT NULL COMMENT '类型 0：单选 1：多选 2：填空 3：判断',
  `score` int(5) NULL DEFAULT NULL COMMENT '分数',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目内容',
  `point` int(5) NULL DEFAULT 0 COMMENT '得分',
  `solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '答案',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生作业' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_homework_student
INSERT INTO `ape_homework_student` VALUES ('1730598904719826945', '1730032800929447937', 'Portfolio Demo Chapter', '1730040079170961409', 'Which option is the correct demo answer?', '1', 'A', '0', '1', '[{"value":"A","option":"Correct demo option"},{"value":"B","option":"Incorrect demo option"}]', '0', 'B', '769dc998d617bff4e30c4bde94f37e90', 'Synthetic portfolio fixture', 'portfolio-demo', '2026-12-01 22:45:07', 'portfolio-demo', '2026-12-01 22:45:07');

-- ----------------------------
-- Table structure for ape_login_log
-- ----------------------------
DROP TABLE IF EXISTS `ape_login_log`;
CREATE TABLE `ape_login_log`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录账号',
  `login_ip` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录IP',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作系统',
  `status` int(2) NULL DEFAULT NULL COMMENT '登录状态 1成功0失败',
  `login_time` datetime(0) NULL DEFAULT NULL COMMENT '登录时间',
  `msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '登录信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '登陆日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_major
-- ----------------------------
DROP TABLE IF EXISTS `ape_major`;
CREATE TABLE `ape_major`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '专业表' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_major
INSERT INTO `ape_major` VALUES ('1725131293429145602', 'Demo Major');

-- ----------------------------
-- Table structure for ape_menu
-- ----------------------------
DROP TABLE IF EXISTS `ape_menu`;
CREATE TABLE `ape_menu`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `id_arrary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主键数组',
  `menu_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `parent_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '父菜单',
  `order_num` int(5) NULL DEFAULT NULL COMMENT '排序',
  `target` int(2) NULL DEFAULT NULL COMMENT '是否外链（0：是）',
  `route_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由地址',
  `component_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件地址',
  `param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由参数',
  `menu_type` int(2) NULL DEFAULT NULL COMMENT '菜单类型（0：目录 1：菜单 2：按钮）',
  `visible` int(2) NULL DEFAULT NULL COMMENT '菜单状态（0显示 1隐藏）',
  `status` int(2) NULL DEFAULT NULL COMMENT '状态（1：停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_menu
INSERT INTO `ape_menu` VALUES ('1696785252320374786', '0', '系统设置', '0', 11, 1, 'system', '', '', 0, 0, 0, '', 'el-icon-setting', '', NULL, 'Portfolio Demo Admin 01', '2026-03-20 16:41:23', '');
INSERT INTO `ape_menu` VALUES ('1699602906470940674', '0', '首页', '0', 1, 1, '/index', '/system/index/index', '', 1, 0, 0, 'system:index', 'el-icon-menu', '', NULL, 'Portfolio Demo Admin 01', '2026-10-10 08:49:36', '');
INSERT INTO `ape_menu` VALUES ('1699604954557972482', '1696785252320374786', '用户管理', '1696785252320374786', 1, 1, '/user', '/system/user/user', '', 1, 0, 0, 'system:user:list', 'el-icon-user', '', NULL, '', NULL, '');
INSERT INTO `ape_menu` VALUES ('1699605448084946945', '1696785252320374786', '角色管理', '1696785252320374786', 2, 1, '/role', '/system/role/role', '', 1, 0, 0, 'system:role:list', 'el-icon-mouse', '', NULL, '', NULL, '');
INSERT INTO `ape_menu` VALUES ('1699605729581465601', '1696785252320374786', '菜单管理', '1696785252320374786', 3, 1, '/menu', '/system/menu/menu', '', 1, 0, 0, 'system:menu:list', 'el-icon-set-up', '', NULL, '', NULL, '');
INSERT INTO `ape_menu` VALUES ('1699606384496869377', '1696785252320374786', '字典管理', '1696785252320374786', 6, 1, '/dict', '/system/dict/dict', '', 1, 0, 0, 'system:dict:list', 'el-icon-notebook-1', '', NULL, '', NULL, '');
INSERT INTO `ape_menu` VALUES ('1699606609743577090', '1696785252320374786', '参数设置', '1696785252320374786', 7, 1, '/param', '/system/param/param', '', 1, 0, 0, 'system:param:list', 'el-icon-edit-outline', '', NULL, '', NULL, '');
INSERT INTO `ape_menu` VALUES ('1699606808062853121', '', '通知公告', '0', 9, 1, '/notice', '/system/notice/notice', '', 1, 0, 0, 'system:notice:list', 'el-icon-bell', '', NULL, 'Portfolio Demo Admin 01', '2026-03-20 16:43:33', '');
INSERT INTO `ape_menu` VALUES ('1711206155472863233', '0', '系统工具', '0', 12, 0, '#', '', '', 0, 0, 0, '', 'el-icon-attract', '', NULL, 'Portfolio Demo Admin 01', '2026-03-20 16:41:49', '');
INSERT INTO `ape_menu` VALUES ('1711206909218652162', '1711206155472863233', '代码生成', '1711206155472863233', 1, 1, '/generate', '/system/generate/generate', '', 1, 0, 0, 'system:generate:list', 'el-icon-magic-stick', '', NULL, '', NULL, '');
INSERT INTO `ape_menu` VALUES ('1714569475810668545', '0', '个人中心', '0', 10, 1, '/center', '/system/center/center', '', 1, 0, 0, 'system:center', 'el-icon-thumb', 'Portfolio Demo Admin 01', '2026-10-18 17:09:53', 'Portfolio Demo Admin 01', '2026-03-20 16:43:38', '');
INSERT INTO `ape_menu` VALUES ('1725124695185018881', '0', '学校管理', '0', 2, 1, '/school', '/system/school/school', '', 1, 0, 0, '/system:school:list', 'el-icon-school', 'Portfolio Demo Admin 01', '2026-03-16 20:12:33', 'Portfolio Demo Admin 01', '2026-03-20 16:37:17', '');
INSERT INTO `ape_menu` VALUES ('1725130993364426754', '0', '专业管理', '0', 3, 1, '/major', '/system/major/major', '', 1, 0, 0, 'system:major:list', 'el-icon-s-platform', 'Portfolio Demo Admin 01', '2026-03-16 20:37:35', 'Portfolio Demo Admin 01', '2026-03-20 16:37:26', '');
INSERT INTO `ape_menu` VALUES ('1725135461107703809', '0', '留言管理', '0', 4, 1, '/message', '/system/message/message', '', 1, 0, 0, 'system:message:list', 'el-icon-question', 'Portfolio Demo Admin 01', '2026-03-16 20:55:20', 'Portfolio Demo Admin 01', '2026-03-20 16:38:00', '');
INSERT INTO `ape_menu` VALUES ('1725138166026936321', '0', '课程管理', '0', 5, 1, 'system', '', '', 0, 0, 0, '', 'el-icon-s-claim', 'Portfolio Demo Admin 01', '2026-03-16 21:06:05', 'Portfolio Demo Admin 01', '2026-03-20 16:38:05', '');
INSERT INTO `ape_menu` VALUES ('1725141019642634242', '0', '用户管理', '0', 8, 0, 'system', '', '', 0, 0, 0, '', 'el-icon-info', 'Portfolio Demo Admin 01', '2026-03-16 21:17:25', 'Portfolio Demo Admin 01', '2026-03-20 16:38:47', '');
INSERT INTO `ape_menu` VALUES ('1725141263528828930', '1725141019642634242', '教师管理', '1725141019642634242', 1, 1, '/teacher', '/system/teacher/teacher', '', 1, 0, 0, 'system:teacher:list', 'el-icon-s-open', 'Portfolio Demo Admin 01', '2026-03-16 21:18:23', 'Portfolio Demo Admin 01', '2026-03-16 21:20:20', '');
INSERT INTO `ape_menu` VALUES ('1725141546556268546', '1725141019642634242', '学生管理', '1725141019642634242', 2, 1, '/student', '/system/student/student', '', 1, 0, 0, 'system:student:list', 'el-icon-coordinate', 'Portfolio Demo Admin 01', '2026-03-16 21:19:31', 'Portfolio Demo Admin 01', '2026-03-16 21:20:24', '');
INSERT INTO `ape_menu` VALUES ('1725356529557561346', '1725138166026936321', '课程管理', '1725138166026936321', 1, 1, '/task', '/system/task/task', '', 1, 0, 0, 'system:task:list', 'el-icon-platform-eleme', 'Portfolio Demo Admin 01', '2026-03-17 11:33:47', 'Portfolio Demo Admin 01', '2026-03-17 11:33:47', '');
INSERT INTO `ape_menu` VALUES ('1725398880971776002', '1725138166026936321', '课程分类', '1725138166026936321', 2, 1, '/classification', '/system/classification/classification', '', 1, 0, 0, 'system:classification:list', 'el-icon-help', 'Portfolio Demo Admin 01', '2026-03-17 14:22:04', 'Portfolio Demo Admin 01', '2026-03-17 14:24:22', '');
INSERT INTO `ape_menu` VALUES ('1725473590578524161', '1725138166026936321', '章节管理', '1725138166026936321', 3, 1, '/chapter', '/system/chapter/chapter', '', 1, 0, 0, 'system:chapter:list', 'el-icon-circle-plus-outline', 'Portfolio Demo Admin 01', '2026-03-17 19:18:56', 'Portfolio Demo Admin 01', '2026-03-17 19:19:05', '');
INSERT INTO `ape_menu` VALUES ('1725683296906338305', '1725138166026936321', '章节作业', '1725138166026936321', 4, 1, '/homework', '/system/homework/homework', '', 1, 0, 0, 'system:homework:list', 'el-icon-s-operation', 'Portfolio Demo Admin 01', '2026-03-18 09:12:14', 'Portfolio Demo Admin 01', '2026-03-18 09:12:14', '');
INSERT INTO `ape_menu` VALUES ('1726410215436136450', '0', '笔记管理', '0', 6, 1, '/article', '/system/article/article', '', 1, 0, 0, 'system:article:list', 'el-icon-edit-outline', 'Portfolio Demo Admin 01', '2026-03-20 09:20:45', 'Portfolio Demo Admin 01', '2026-03-20 16:38:38', '');
INSERT INTO `ape_menu` VALUES ('1726443745172045826', '1726494839550095361', '考试管理', '1726494839550095361', 1, 1, '/test', '/system/testP/test', '', 1, 0, 0, 'system:test:list', 'el-icon-s-platform', 'Portfolio Demo Admin 01', '2026-03-20 11:33:59', 'Portfolio Demo Admin 01', '2026-03-20 14:57:22', '');
INSERT INTO `ape_menu` VALUES ('1726494839550095361', '0', '考试中心', '0', 7, 1, 'system', '', '', 0, 0, 0, '', 'el-icon-s-promotion', 'Portfolio Demo Admin 01', '2026-03-20 14:57:01', 'Portfolio Demo Admin 01', '2026-03-20 16:38:55', '');
INSERT INTO `ape_menu` VALUES ('1726495249274875905', '1726494839550095361', '考试题目', '1726494839550095361', 1, 1, '/testItem', '/system/testP/testItem', '', 1, 1, 0, 'system:testItem:list', 'el-icon-s-grid', 'Portfolio Demo Admin 01', '2026-03-20 14:58:39', 'Portfolio Demo Admin 01', '2026-03-20 15:03:45', '');
INSERT INTO `ape_menu` VALUES ('1726864359255003137', '1725138166026936321', '学生列表', '1725138166026936321', 5, 1, '/taskStudent', '/system/task/taskStudent', '', 1, 1, 0, 'system:taskStudent:list', 'el-icon-platform-eleme', 'Portfolio Demo Admin 01', '2026-03-21 15:25:21', 'Portfolio Demo Admin 01', '2026-03-21 15:25:21', '');
INSERT INTO `ape_menu` VALUES ('1726937405442859010', '1725138166026936321', '课程评论', '1725138166026936321', 6, 1, '/taskComment', '/system/task/taskComment', '', 1, 1, 0, 'system:taskComment:list', 'el-icon-question', 'Portfolio Demo Admin 01', '2026-03-21 20:15:37', 'Portfolio Demo Admin 01', '2026-03-21 20:15:37', '');
INSERT INTO `ape_menu` VALUES ('1726966944873123841', '0', '笔记评论', '0', 13, 1, '/articleComment', '/system/article/articleComment', '', 1, 1, 0, 'system:articleComment:list', 'el-icon-circle-plus', 'Portfolio Demo Admin 01', '2026-03-21 22:13:00', 'Portfolio Demo Admin 01', '2026-03-21 22:13:00', '');
INSERT INTO `ape_menu` VALUES ('1727331380376899585', '1725138166026936321', '学习情况', '0', 7, 1, '/chapterStudy', '/system/chapter/chapterStudy', '', 1, 1, 0, 'system:chapterStudy:list', 'el-icon-phone-outline', 'Portfolio Demo Admin 01', '2026-03-22 22:21:08', 'Portfolio Demo Admin 01', '2026-03-22 22:22:02', '');
INSERT INTO `ape_menu` VALUES ('1729132365646671874', '1726494839550095361', '阅卷', '1726494839550095361', 3, 1, '/testStudent', '/system/testP/testStudent', '', 1, 1, 0, 'system:testStudent:list', 'el-icon-circle-check', 'Portfolio Demo Admin 01', '2026-03-27 21:37:36', 'Portfolio Demo Admin 01', '2026-03-27 21:37:36', '');
INSERT INTO `ape_menu` VALUES ('1729408476388335618', '1725138166026936321', '学习情况', '1725138166026936321', 6, 1, '/studyInfo', '/system/task/studyInfo', '', 1, 1, 0, 'system:studyInfo:list', 'el-icon-s-comment', 'Portfolio Demo Admin 01', '2026-03-28 15:54:46', 'Portfolio Demo Admin 01', '2026-03-28 15:54:46', '');
INSERT INTO `ape_menu` VALUES ('1729483865346584577', '1725138166026936321', '教师课程管理', '1725138166026936321', 1, 1, '/teacherTask', '/system/task/teacherTask', '', 1, 0, 0, 'system:teacherTask:list', 'el-icon-help', 'Portfolio Demo Admin 01', '2026-03-28 20:54:20', 'Portfolio Demo Admin 01', '2026-03-28 20:54:20', '');
INSERT INTO `ape_menu` VALUES ('1729484287973044226', '1725138166026936321', '教师章节管理', '1725138166026936321', 3, 1, '/teacherChapter', '/system/chapter/teacherChapter', '', 1, 0, 0, 'system:teacherChapter:list', 'el-icon-s-operation', 'Portfolio Demo Admin 01', '2026-03-28 20:56:01', 'Portfolio Demo Admin 01', '2026-03-28 20:56:01', '');
INSERT INTO `ape_menu` VALUES ('1729485248544157697', '0', '学生笔记管理', '0', 6, 1, '/teacherArticle', '/system/article/teacherArticle', '', 1, 0, 0, 'system:teacherArticle:list', 'el-icon-document-remove', 'Portfolio Demo Admin 01', '2026-03-28 20:59:50', 'Portfolio Demo Admin 01', '2026-03-28 20:59:50', '');
INSERT INTO `ape_menu` VALUES ('1729485667131502594', '1726494839550095361', '课程考试管理', '1726494839550095361', 1, 1, '/teacherTest', '/system/testP/teacherTest', '', 1, 0, 0, 'system:teacherTest:list', 'el-icon-s-claim', 'Portfolio Demo Admin 01', '2026-03-28 21:01:30', 'Portfolio Demo Admin 01', '2026-03-28 21:01:30', '');
INSERT INTO `ape_menu` VALUES ('1729486234088157186', '0', '教师首页', '0', 1, 1, '/index', '/system/index/teacherIndex', '', 1, 0, 0, 'system:teacherIndex:list', 'el-icon-s-management', 'Portfolio Demo Admin 01', '2026-03-28 21:03:45', 'Portfolio Demo Admin 01', '2026-03-28 21:03:45', '');
INSERT INTO `ape_menu` VALUES ('1747787875986825218', '0', '讨论管理', '0', 7, 1, '/forum', '/system/forum/forum', '', 1, 0, 0, 'system:list:forum', 'el-icon-reading', 'Portfolio Demo Admin 01', '2024-01-18 09:07:57', 'Portfolio Demo Admin 01', '2024-01-18 09:51:29', '');
INSERT INTO `ape_menu` VALUES ('1747795666055020545', '0', '讨论内容', '0', 7, 1, '/forumItem', '/system/forum/forumItem', '', 1, 1, 0, 'system:forumItem:list', 'el-icon-more-outline', 'Portfolio Demo Admin 01', '2024-01-18 09:38:54', 'Portfolio Demo Admin 01', '2024-01-18 09:38:54', '');
INSERT INTO `ape_menu` VALUES ('1747795895785439234', '0', '讨论管理', '0', 7, 1, '/forumAdmin', '/system/forum/forumAdmin', '', 1, 0, 0, 'system:forumAdmin:list', 'el-icon-s-flag', 'Portfolio Demo Admin 01', '2024-01-18 09:39:49', 'Portfolio Demo Admin 01', '2024-01-18 09:51:43', '');
INSERT INTO `ape_menu` VALUES ('1747822226493517826', '0', '课程答疑', '0', 7, 1, '/question', '/system/question/question', '', 1, 0, 0, 'system:question:list', 'el-icon-finished', 'Portfolio Demo Admin 01', '2024-01-18 11:24:26', 'Portfolio Demo Admin 01', '2024-01-18 11:24:26', '');

-- ----------------------------
-- Table structure for ape_message
-- ----------------------------
DROP TABLE IF EXISTS `ape_message`;
CREATE TABLE `ape_message`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '提问内容',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '回复',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '留言表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_operate_log
-- ----------------------------
DROP TABLE IF EXISTS `ape_operate_log`;
CREATE TABLE `ape_operate_log`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模块名称',
  `type` int(2) NULL DEFAULT NULL COMMENT '业务类型0其他1新增2修改3删除',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '方法名称',
  `request_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求类型',
  `oper_type` int(2) NULL DEFAULT NULL COMMENT '操作类型0其他1后台2移动端',
  `oper_user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作用户id',
  `oper_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作用户名称',
  `oper_user_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作用户账号',
  `oper_time` datetime(0) NULL DEFAULT NULL COMMENT '操作时间',
  `oper_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求url',
  `oper_ip` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作ip',
  `oper_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作地点',
  `oper_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `result_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '返回参数',
  `state` int(2) NULL DEFAULT NULL COMMENT '操作状态0正常1异常',
  `error_msg` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误消息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_param
-- ----------------------------
DROP TABLE IF EXISTS `ape_param`;
CREATE TABLE `ape_param`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `param_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数名称',
  `param_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数键',
  `param_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '参数值',
  `within` int(2) NULL DEFAULT NULL COMMENT '系统内置',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_post
-- ----------------------------
DROP TABLE IF EXISTS `ape_post`;
CREATE TABLE `ape_post`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int(4) NOT NULL COMMENT '显示顺序',
  `status` int(2) NOT NULL COMMENT '状态（0正常 1停用）',
  `del_flag` int(2) NULL DEFAULT 0 COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_post
INSERT INTO `ape_post` VALUES ('1697050775075495937', 'Java', '后端', 1, 0, 0, '', NULL, '', NULL, '后端开发');

-- ----------------------------
-- Table structure for ape_question
-- ----------------------------
DROP TABLE IF EXISTS `ape_question`;
CREATE TABLE `ape_question`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '提问内容',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '回复',
  `teacher_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教师id',
  `task_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程id',
  `task_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '答疑' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_role
-- ----------------------------
DROP TABLE IF EXISTS `ape_role`;
CREATE TABLE `ape_role`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `role_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(4) NOT NULL COMMENT '显示顺序',
  `status` int(2) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` int(2) NULL DEFAULT 0 COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_role
INSERT INTO `ape_role` VALUES ('3e20ea32c15af62cdf843d9dd0b1a5a9', '管理员', 'portfolio_admin_01', 1, 0, 0, '', NULL, 'Portfolio Demo Admin 01', '2024-01-18 09:40:09', '');
INSERT INTO `ape_role` VALUES ('16f367f7a82fab5bd664c504e0433351', '教师', 'teacher', 3, 0, 0, 'Portfolio Demo Admin 01', '2026-03-17 10:54:23', 'Portfolio Demo Admin 01', '2024-01-18 11:24:37', '');
INSERT INTO `ape_role` VALUES ('1cb0cc19dfecf8ee8293f5af773d62a9', '学生', 'student', 2, '0', 1, '', NULL, '', NULL, 'Synthetic portfolio fixture');

-- ----------------------------
-- Table structure for ape_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `ape_role_menu`;
CREATE TABLE `ape_role_menu`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `role_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色id',
  `menu_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色菜单关系' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_role_menu
INSERT INTO `ape_role_menu` VALUES ('1747795982200684545', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699602906470940674');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684546', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725124695185018881');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684547', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725130993364426754');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684548', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725135461107703809');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684549', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725138166026936321');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684550', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725356529557561346');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684551', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725398880971776002');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684552', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725473590578524161');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684553', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725683296906338305');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684554', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1726864359255003137');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684555', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1726937405442859010');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684556', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1729408476388335618');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684557', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1726410215436136450');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684558', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1726494839550095361');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684559', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1726443745172045826');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684560', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1726495249274875905');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684561', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1729132365646671874');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684562', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1727331380376899585');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684563', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1747795666055020545');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684564', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1747795895785439234');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684565', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725141019642634242');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684566', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725141263528828930');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684567', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1725141546556268546');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684568', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699606808062853121');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684569', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1714569475810668545');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684570', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1696785252320374786');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684571', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699604954557972482');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684572', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699605448084946945');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684573', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699605729581465601');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684574', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1696786476860010497');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684575', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699606164799225857');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684576', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699606384496869377');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684577', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699606609743577090');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684578', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699606971506491394');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684579', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699607170954035201');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684580', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1699607352240242689');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684581', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1711206155472863233');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684582', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1711206909218652162');
INSERT INTO `ape_role_menu` VALUES ('1747795982200684583', '3e20ea32c15af62cdf843d9dd0b1a5a9', '1726966944873123841');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965633', '16f367f7a82fab5bd664c504e0433351', '1729486234088157186');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965634', '16f367f7a82fab5bd664c504e0433351', '1725138166026936321');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965635', '16f367f7a82fab5bd664c504e0433351', '1729483865346584577');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965636', '16f367f7a82fab5bd664c504e0433351', '1729484287973044226');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965637', '16f367f7a82fab5bd664c504e0433351', '1725683296906338305');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965638', '16f367f7a82fab5bd664c504e0433351', '1726864359255003137');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965639', '16f367f7a82fab5bd664c504e0433351', '1726937405442859010');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965640', '16f367f7a82fab5bd664c504e0433351', '1729408476388335618');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965641', '16f367f7a82fab5bd664c504e0433351', '1729485248544157697');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965642', '16f367f7a82fab5bd664c504e0433351', '1726494839550095361');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965643', '16f367f7a82fab5bd664c504e0433351', '1726495249274875905');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965644', '16f367f7a82fab5bd664c504e0433351', '1729485667131502594');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965645', '16f367f7a82fab5bd664c504e0433351', '1729132365646671874');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965646', '16f367f7a82fab5bd664c504e0433351', '1727331380376899585');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965647', '16f367f7a82fab5bd664c504e0433351', '1747787875986825218');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965648', '16f367f7a82fab5bd664c504e0433351', '1747795666055020545');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965649', '16f367f7a82fab5bd664c504e0433351', '1747822226493517826');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965650', '16f367f7a82fab5bd664c504e0433351', '1714569475810668545');
INSERT INTO `ape_role_menu` VALUES ('1747822272848965651', '16f367f7a82fab5bd664c504e0433351', '1726966944873123841');

-- ----------------------------
-- Table structure for ape_school
-- ----------------------------
DROP TABLE IF EXISTS `ape_school`;
CREATE TABLE `ape_school`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学校表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_task
-- ----------------------------
DROP TABLE IF EXISTS `ape_task`;
CREATE TABLE `ape_task`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程名称',
  `task_describe` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '课程描述',
  `teacher_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教师id',
  `teacher_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教师名称',
  `image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '封面图片',
  `state` int(2) NULL DEFAULT 2 COMMENT '状态 0：上架 1:下架  2：审核中',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属专业',
  `classification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '所属分类',
  `num` int(10) NULL DEFAULT 0 COMMENT '学生数量',
  `proportion` int(5) NULL DEFAULT NULL COMMENT '视频作业分数占比',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_task
INSERT INTO `ape_task` VALUES ('1729751427215790082', 'Portfolio Demo Course', 'Synthetic course fixture for portfolio demonstration.', 'c864ceb6e258c75d183ba54a2033bd77', 'Portfolio Demo Teacher 01', '', 0, 'Demo Major', 'Demo Classification', 3, 70, '', 'portfolio-demo', '2026-03-29 14:37:32', 'portfolio-demo', '2026-01-09 18:31:43');

-- ----------------------------
-- Table structure for ape_task_comment
-- ----------------------------
DROP TABLE IF EXISTS `ape_task_comment`;
CREATE TABLE `ape_task_comment`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程主键',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程评论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_task_favor
-- ----------------------------
DROP TABLE IF EXISTS `ape_task_favor`;
CREATE TABLE `ape_task_favor`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程id',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `task_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程名称',
  `task_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '课程封面',
  `task_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '课程简介',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ape_task_student
-- ----------------------------
DROP TABLE IF EXISTS `ape_task_student`;
CREATE TABLE `ape_task_student`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学生id',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学生姓名',
  `teacher_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教师id',
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教师姓名',
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程id',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程名称',
  `state` int(11) NULL DEFAULT 1 COMMENT '报名状态 0：通过  1：未通过',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程报名' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_task_student
INSERT INTO `ape_task_student` VALUES ('1730038492058910721', '769dc998d617bff4e30c4bde94f37e90', 'Portfolio Demo Student 01', 'c864ceb6e258c75d183ba54a2033bd77', 'Portfolio Demo Teacher 01', '1729751427215790082', 'Portfolio Demo Course', 0);

-- ----------------------------
-- Table structure for ape_test
-- ----------------------------
DROP TABLE IF EXISTS `ape_test`;
CREATE TABLE `ape_test`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '考试名称',
  `task_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程id',
  `task_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程名称',
  `total_score` int(5) NULL DEFAULT NULL COMMENT '总分',
  `total_time` int(5) NULL DEFAULT NULL COMMENT '考试时长',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '截止时间',
  `state` int(2) NULL DEFAULT 2 COMMENT '审核：0：通过 1：未通过 2：审核中',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '考试' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_test
INSERT INTO `ape_test` VALUES ('1730068371957653506', 'Portfolio Demo Exam', '1729751427215790082', 'Portfolio Demo Course', '1', 120, '2026-03-28 08:00:00', '2026-04-30 08:00:00', '0', 'Synthetic portfolio fixture', 'portfolio-demo', '2026-03-30 11:36:58', 'portfolio-demo', '2026-04-30 23:10:49');

-- ----------------------------
-- Table structure for ape_test_item
-- ----------------------------
DROP TABLE IF EXISTS `ape_test_item`;
CREATE TABLE `ape_test_item`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `item_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '题目id',
  `test_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '考试id',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目',
  `sort` int(5) NULL DEFAULT NULL COMMENT '序号',
  `type` int(2) NULL DEFAULT NULL COMMENT '类型 0：单选 1：多选 2：填空 3：判断 4：问答题 5：计算题',
  `score` int(5) NULL DEFAULT NULL COMMENT '分数',
  `keyword` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '得分点',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '答案',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目内容',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '考试题目' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_test_item
INSERT INTO `ape_test_item` VALUES ('1730103081903628289', NULL, '1730068371957653506', 'Which option is the correct demo exam answer?', '1', '0', '1', '', 'A', '[{"value":"A","option":"Correct demo option"},{"value":"B","option":"Incorrect demo option"}]', 'Synthetic portfolio fixture', 'portfolio-demo', '2026-03-30 13:54:53', 'portfolio-demo', '2026-03-30 13:54:53');

-- ----------------------------
-- Table structure for ape_test_student
-- ----------------------------
DROP TABLE IF EXISTS `ape_test_student`;
CREATE TABLE `ape_test_student`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `item_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '题目id',
  `test_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '考试id',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目',
  `sort` int(5) NULL DEFAULT NULL COMMENT '序号',
  `type` int(2) NULL DEFAULT NULL COMMENT '类型 0：单选 1：多选 2：填空 3：判断 4：问答题 5：计算题',
  `score` int(5) NULL DEFAULT NULL COMMENT '分数',
  `keyword` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '得分点',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '答案',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目内容',
  `point` int(5) NULL DEFAULT 0 COMMENT '得分',
  `solution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '答案',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户考试题目' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_test_student
INSERT INTO `ape_test_student` VALUES ('1730124180074893313', '1730103081903628289', '1730068371957653506', 'Which option is the correct demo exam answer?', '1', '0', '1', '', 'A', '[{"value":"A","option":"Correct demo option"},{"value":"B","option":"Incorrect demo option"}]', '0', 'B', '769dc998d617bff4e30c4bde94f37e90', 'Synthetic portfolio fixture', 'portfolio-demo', '2026-03-30 15:18:43', 'portfolio-demo', NULL);

-- ----------------------------
-- Table structure for ape_user
-- ----------------------------
DROP TABLE IF EXISTS `ape_user`;
CREATE TABLE `ape_user`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `post_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '岗位id',
  `id_arrary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门数组',
  `dept_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部门id',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `login_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登陆账号',
  `user_type` int(2) NULL DEFAULT NULL COMMENT '用户类型（0：系统 1：教师 2：学生）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户邮箱',
  `tel` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `sex` int(2) NULL DEFAULT 0 COMMENT '性别（0：男 1：女）',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '头像',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码(BCrypt)',
  `salt` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '盐值',
  `status` int(2) NULL DEFAULT 0 COMMENT '帐号状态（0正常 1停用）',
  `login_ip` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `login_date` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime(0) NULL DEFAULT NULL COMMENT '密码最后更新时间',
  `del_flag` int(2) NULL DEFAULT 0 COMMENT '删除标志（0代表存在 2代表删除）',
  `school` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学校',
  `country` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '国家',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '专业',
  `agree` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职称',
  `age` int(10) NULL DEFAULT NULL COMMENT '年龄',
  `birth` datetime(0) NULL DEFAULT NULL COMMENT '生日',
  `flair` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '教学资质',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `work_date` int(11) NULL DEFAULT NULL COMMENT '教龄',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_user
INSERT INTO `ape_user` VALUES ('1697436716646531073', '1697050775075495937', '100', '100', 'Portfolio Demo Admin 01', 'portfolio_admin_01', 0, 'portfolio.admin01@example.invalid', 'demo-phone-001', 0, '/img/avatars/zhixue-admin-default.png','$2a$10$6Ry0Z7tOhfpu6sPlU84/qeGWPUcGcvJruUvsBVRVmlb8aW08HqgoC', '', 0, '127.0.0.1', '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0, 'Portfolio Demo School', 'Demo Region', 'Demo Faculty', NULL, NULL, NULL, NULL, 'Synthetic demo address', NULL, 'Synthetic portfolio fixture', 'portfolio-demo', NULL, 'portfolio-demo', '2026-10-23 20:46:11');
INSERT INTO `ape_user` VALUES ('c864ceb6e258c75d183ba54a2033bd77', NULL, NULL, NULL, 'Portfolio Demo Teacher 01', 'portfolio_teacher_01', 1, 'portfolio.teacher01@example.invalid', 'demo-phone-101', 0, '/img/avatars/zhixue-teacher-default.png','$2a$10$yl35ethhDeRl8NbAQJ7wJulvyXkfI71tbz04q2rIbIkQSzo6yk.Ae', '', 0, '127.0.0.1', '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0, 'Portfolio Demo School', 'Demo Region', 'Demo Faculty', 'Demo Instructor', 25, '2000-01-01 00:00:00', 'Synthetic teaching profile', 'Synthetic demo address', 3, 'Synthetic portfolio fixture', 'portfolio-demo', '2026-03-29 10:53:43', 'portfolio-demo', '2026-01-18 09:35:44');
INSERT INTO `ape_user` VALUES ('769dc998d617bff4e30c4bde94f37e90', NULL, NULL, NULL, 'Portfolio Demo Student 01', 'portfolio_student_01', 2, 'portfolio.student01@example.invalid', 'demo-phone-201', 0, '/img/avatars/zhixue-student-default.png','$2a$10$ISvwQnoLm6zerWb1OaxrO.NVRr8bnk.vAsehHli7.w1RXGSMCpgue', '', 0, '127.0.0.1', '2026-01-01 00:00:00', '2026-01-01 00:00:00', 0, 'Portfolio Demo School', 'Demo Region', 'Demo Major', NULL, 20, '2000-01-01 00:00:00', NULL, 'Synthetic demo address', NULL, 'Synthetic portfolio fixture', 'portfolio-demo', '2026-03-29 11:18:27', 'portfolio-demo', '2026-03-29 11:18:50');

-- ----------------------------
-- Table structure for ape_user_role
-- ----------------------------
DROP TABLE IF EXISTS `ape_user_role`;
CREATE TABLE `ape_user_role`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主键',
  `user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `role_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关系' ROW_FORMAT = Dynamic;

-- Synthetic demo records for ape_user_role
INSERT INTO `ape_user_role` VALUES ('portfolio-demo-user-role-admin', '1697436716646531073', '3e20ea32c15af62cdf843d9dd0b1a5a9');
INSERT INTO `ape_user_role` VALUES ('portfolio-demo-user-role-teacher', 'c864ceb6e258c75d183ba54a2033bd77', '16f367f7a82fab5bd664c504e0433351');
INSERT INTO `ape_user_role` VALUES ('portfolio-demo-user-role-student', '769dc998d617bff4e30c4bde94f37e90', '1cb0cc19dfecf8ee8293f5af773d62a9');

SET FOREIGN_KEY_CHECKS = 1;
