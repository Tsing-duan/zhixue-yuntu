package com.ape.apeadmin.controller.ai;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ape.apecommon.annotation.Log;
import com.ape.apecommon.domain.Result;
import com.ape.apecommon.constant.Constants;
import com.ape.apecommon.enums.BusinessType;
import com.ape.apeframework.utils.ShiroUtils;
import com.ape.apesystem.domain.ApeAiHistory;
import com.ape.apesystem.domain.ApeChapter;
import com.ape.apesystem.domain.ApeChapterVideo;
import com.ape.apesystem.domain.ApeHomework;
import com.ape.apesystem.domain.ApeLoginLog;
import com.ape.apesystem.domain.ApeHomeworkStudent;
import com.ape.apesystem.domain.ApeTask;
import com.ape.apesystem.domain.ApeTaskStudent;
import com.ape.apesystem.domain.ApeTest;
import com.ape.apesystem.domain.ApeTestItem;
import com.ape.apesystem.domain.ApeTestStudent;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.AiService;
import com.ape.apesystem.service.ApeAiHistoryService;
import com.ape.apesystem.service.ai.AiGenerationResult;
import com.ape.apesystem.service.ApeArticleService;
import com.ape.apesystem.service.ApeChapterService;
import com.ape.apesystem.service.ApeChapterVideoService;
import com.ape.apesystem.service.ApeForumService;
import com.ape.apesystem.service.ApeHomeworkService;
import com.ape.apesystem.service.ApeHomeworkStudentService;
import com.ape.apesystem.service.ApeLoginLogService;
import com.ape.apesystem.service.ApeTaskService;
import com.ape.apesystem.service.ApeTaskStudentService;
import com.ape.apesystem.service.ApeTestItemService;
import com.ape.apesystem.service.ApeTestService;
import com.ape.apesystem.service.ApeTestStudentService;
import com.ape.apesystem.service.ApeUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI 控制器
 */
@Controller
@ResponseBody
@RequestMapping("/ai")
public class AiController {

    private static final String AGENT_TYPE_CHAT = "chat";
    private static final String AGENT_TYPE_STUDY_PLAN = "study-plan";
    private static final String AGENT_TYPE_WRONG_AGENT = "wrong-agent";
    private static final String AGENT_TYPE_LESSON_PREPARE = "lesson-prepare";
    private static final String AGENT_TYPE_COURSE_SUMMARY = "course-summary";
    private static final String AGENT_TYPE_PLATFORM_REPORT = "platform-report";
    private static final String AGENT_TYPE_COURSE_HEALTH = "course-health";
    private static final String AGENT_TYPE_EXAM_ANALYSIS = "exam-analysis";
    private static final int STUDENT_DAILY_AI_TOKEN_LIMIT = 20000;
    private static final int STUDENT_DAILY_AI_CALL_LIMIT = 30;
    private static final int STUDENT_AI_COOLDOWN_SECONDS = 15;
    private static final int STUDENT_AI_INPUT_MAX_CHARS = 1000;
    private static final int STUDENT_AI_RESERVED_TOKENS = 1200;
    private static final int TEACHER_DAILY_AI_TOKEN_LIMIT = 60000;
    private static final int TEACHER_DAILY_AI_CALL_LIMIT = 60;
    private static final int TEACHER_AI_COOLDOWN_SECONDS = 8;
    private static final int TEACHER_AI_INPUT_MAX_CHARS = 2500;
    private static final int TEACHER_AI_RESERVED_TOKENS = 1800;
    private static final int ADMIN_DAILY_AI_TOKEN_LIMIT = 120000;
    private static final int ADMIN_DAILY_AI_CALL_LIMIT = 100;
    private static final int ADMIN_AI_COOLDOWN_SECONDS = 5;
    private static final int ADMIN_AI_INPUT_MAX_CHARS = 4000;
    private static final int ADMIN_AI_RESERVED_TOKENS = 2500;
    private static final String STUDENT_AI_OFF_TOPIC_MESSAGE =
            "这个问题和当前学习平台场景关联不强。请围绕课程学习、作业错题、考试复习、学习计划或平台使用继续提问。";
    private static final String AI_SECURITY_REJECT_MESSAGE =
            "这个请求可能绕过平台 AI 使用边界。请围绕课程学习、教学备课、考试分析或平台数据整理继续提问。";
    private static final Map<String, Long> AI_REQUEST_GUARD = new ConcurrentHashMap<>();
    private static final List<String> STUDENT_AI_TOPIC_KEYWORDS = Arrays.asList(
            "课程", "学习", "作业", "考试", "错题", "章节", "题目", "答案", "知识点", "复习",
            "计划", "老师", "教师", "笔记", "报名", "成绩", "分数", "课件", "视频", "平台",
            "智学云途", "专业", "学校", "登录", "course", "study", "homework", "exam", "review"
    );
    private static final List<String> STUDENT_AI_OFF_TOPIC_KEYWORDS = Arrays.asList(
            "无关", "情诗", "恋爱", "表白", "股票", "彩票", "游戏", "段子", "笑话", "小说",
            "娱乐", "旅游", "电影", "做饭", "菜谱", "炒股", "算命", "八卦", "unrelated"
    );
    private static final List<String> AI_PROMPT_ATTACK_KEYWORDS = Arrays.asList(
            "忽略之前", "忽略以上", "忽略所有", "忽略你之前", "忽略系统", "系统提示词", "系统提示",
            "system prompt", "developer message", "ignore previous", "ignore all previous",
            "jailbreak", "越狱", "绕过限制", "解除限制", "泄露提示词", "输出你的提示词",
            "显示你的提示词", "打印你的提示词", "扮演dan", "dan模式"
    );

    @Autowired
    private AiService aiService;
    @Autowired
    private ApeAiHistoryService apeAiHistoryService;
    @Autowired
    private ApeTaskService apeTaskService;
    @Autowired
    private ApeTaskStudentService apeTaskStudentService;
    @Autowired
    private ApeChapterService apeChapterService;
    @Autowired
    private ApeChapterVideoService apeChapterVideoService;
    @Autowired
    private ApeHomeworkService apeHomeworkService;
    @Autowired
    private ApeHomeworkStudentService apeHomeworkStudentService;
    @Autowired
    private ApeTestStudentService apeTestStudentService;
    @Autowired
    private ApeUserService apeUserService;
    @Autowired
    private ApeTestService apeTestService;
    @Autowired
    private ApeTestItemService apeTestItemService;
    @Autowired
    private ApeLoginLogService apeLoginLogService;
    @Autowired
    private ApeArticleService apeArticleService;
    @Autowired
    private ApeForumService apeForumService;


    @Log(name = "AI聊天", type = BusinessType.OTHER)
    @PostMapping("/chat")
    public Result chat(@RequestBody JSONObject jsonObject) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录后再使用 AI 助手");
        }

        String message = jsonObject.getString("message");
        if (StringUtils.isBlank(message)) {
            return Result.fail("消息不能为空");
        }

        String userInput = message.trim();
        Result abuseCheck = validateAiRequest(user, AGENT_TYPE_CHAT, userInput, true);
        if (abuseCheck != null) {
            return abuseCheck;
        }
        String role = getRole(user);
        AiGenerationResult aiResult = aiService.chat(role, userInput);
        Result providerFailure = providerFailure(aiResult);
        if (providerFailure != null) {
            return providerFailure;
        }
        String reply = aiResult.getContent();
        ApeAiHistory history = saveHistory(user.getId(), user.getUserName(), AGENT_TYPE_CHAT, userInput, reply, "role=" + role,
                aiResult.getPromptTokens(), aiResult.getCompletionTokens());
        return Result.success(replyData(reply, history));
    }

    @Log(name = "AI自动学习规划", type = BusinessType.OTHER)
    @PostMapping("/study-plan")
    @RequiresRoles("student")
    public Result studyPlan(@RequestBody JSONObject jsonObject) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录后再使用 AI 学习规划");
        }
        if (!isStudent(user)) {
            return Result.fail("学习规划仅限学生使用");
        }
        String taskId = StringUtils.trimToNull(jsonObject.getString("taskId"));
        if (taskId != null) {
            ApeTask selectedTask = apeTaskService.getById(taskId);
            if (!canAccessTask(user, selectedTask)) {
                return Result.fail("课程不存在或无权访问");
            }
        }

        String context = buildStudyContext(user, taskId);
        String task = "请生成一份自动学习规划，要求包含阶段目标、每日/每周安排、复习节奏、错题巩固策略和可执行清单。"
                + "\n学习目标：" + text(jsonObject, "goal", "根据当前课程进度稳步提升")
                + "\n计划周期：" + text(jsonObject, "days", "14") + " 天"
                + "\n每周可投入时间：" + text(jsonObject, "hours", "6") + " 小时"
                + "\n额外要求：" + text(jsonObject, "extra", "安排要现实可执行，方便学生照着做");
        Result abuseCheck = validateAiRequest(user, AGENT_TYPE_STUDY_PLAN, task, task + "\n" + context, false);
        if (abuseCheck != null) {
            return abuseCheck;
        }
        AiGenerationResult aiResult = aiService.runAgent(getRole(user), "AI 自动学习规划", task, context);
        Result providerFailure = providerFailure(aiResult);
        if (providerFailure != null) {
            return providerFailure;
        }
        String reply = aiResult.getContent();
        ApeAiHistory history = saveHistory(user.getId(), user.getUserName(), AGENT_TYPE_STUDY_PLAN, task, reply, context,
                aiResult.getPromptTokens(), aiResult.getCompletionTokens());
        return Result.success(replyData(reply, history));
    }

    /** 获取错题列表（供前端勾选） */
    @Log(name = "获取错题列表", type = BusinessType.OTHER)
    @GetMapping("/wrong-list")
    @RequiresRoles("student")
    public Result wrongList() {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) return Result.fail("请先登录");
        if (!isStudent(user)) return Result.fail("错题列表仅限学生查看");

        JSONArray list = new JSONArray();

        // 作业错题
        QueryWrapper<ApeHomeworkStudent> hwQw = new QueryWrapper<>();
        hwQw.lambda().eq(ApeHomeworkStudent::getUserId, user.getId())
                .orderByAsc(ApeHomeworkStudent::getChapterId).orderByAsc(ApeHomeworkStudent::getSort);
        for (ApeHomeworkStudent item : apeHomeworkStudentService.list(hwQw)) {
            if (isHomeworkAnswerCorrect(item)) continue;
            fillHomeworkCourseName(item);
            JSONObject obj = new JSONObject();
            obj.put("id", "hw_" + item.getId());
            obj.put("source", "作业");
            obj.put("courseName", safe(item.getTaskName()));
            obj.put("chapterName", safe(item.getChapterName()));
            obj.put("title", item.getTitle());
            obj.put("questionType", Constants.getQuestionTypeName(item.getType()));
            obj.put("correctAnswer", item.getAnswer());
            obj.put("studentAnswer", item.getSolution());
            obj.put("score", (item.getPoint() != null ? item.getPoint() : 0));
            obj.put("maxScore", (item.getScore() != null ? item.getScore() : 0));
            list.add(obj);
        }

        // 考试错题
        QueryWrapper<ApeTestStudent> testQw = new QueryWrapper<>();
        testQw.lambda().eq(ApeTestStudent::getUserId, user.getId())
                .orderByAsc(ApeTestStudent::getTestId).orderByAsc(ApeTestStudent::getSort);
        for (ApeTestStudent item : apeTestStudentService.list(testQw)) {
            if (!isWrongTestAnswer(item)) continue;
            JSONObject obj = new JSONObject();
            obj.put("id", "test_" + item.getId());
            obj.put("source", "考试");
            obj.put("courseName", safe(item.getTestName()));
            obj.put("chapterName", "");
            obj.put("title", item.getTitle());
            obj.put("questionType", Constants.getQuestionTypeName(item.getType()));
            obj.put("correctAnswer", item.getAnswer());
            obj.put("studentAnswer", item.getSolution());
            obj.put("score", (item.getPoint() != null ? item.getPoint() : 0));
            obj.put("maxScore", (item.getScore() != null ? item.getScore() : 0));
            list.add(obj);
        }

        return Result.success(list);
    }

    @Log(name = "AI错题Agent", type = BusinessType.OTHER)
    @PostMapping("/wrong-agent")
    @RequiresRoles("student")
    public Result wrongAgent(@RequestBody JSONObject jsonObject) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录后再使用 AI 错题 Agent");
        }
        if (!isStudent(user)) {
            return Result.fail("错题分析仅限学生使用");
        }

        JSONArray itemIds = jsonObject.getJSONArray("itemIds");
        int maxCount;
        if (itemIds != null && !itemIds.isEmpty()) {
            maxCount = itemIds.size();
        } else {
            Integer limit = jsonObject.getInteger("limit");
            maxCount = limit == null ? 12 : Math.max(3, Math.min(limit, 20));
        }
        WrongContext wrongContext = buildWrongContext(user, maxCount, itemIds);
        String task = "请分析这些错题，输出：薄弱知识点、错误原因、订正思路、同类题练习建议、下一步复习安排。"
                + "\n学生希望：" + text(jsonObject, "target", "先找出最该补的知识点，再给出复习步骤");
        Result abuseCheck = validateAiRequest(user, AGENT_TYPE_WRONG_AGENT, task, task + "\n" + wrongContext.content, false);
        if (abuseCheck != null) {
            return abuseCheck;
        }
        AiGenerationResult aiResult = aiService.runAgent(getRole(user), "AI 错题 Agent", task, wrongContext.content);
        Result providerFailure = providerFailure(aiResult);
        if (providerFailure != null) {
            return providerFailure;
        }
        String reply = aiResult.getContent();
        ApeAiHistory history = saveHistory(user.getId(), user.getUserName(), AGENT_TYPE_WRONG_AGENT, task, reply, wrongContext.content,
                aiResult.getPromptTokens(), aiResult.getCompletionTokens());

        JSONObject data = replyData(reply, history);
        data.put("wrongCount", wrongContext.totalCount);
        return Result.success(data);
    }

    @Log(name = "AI教师备课助手", type = BusinessType.OTHER)
    @PostMapping("/lesson-prepare")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result lessonPrepare(@RequestBody JSONObject jsonObject) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录后再使用 AI 备课助手");
        }
        if (!isAdmin(user) && !isTeacher(user)) {
            return Result.fail("备课助手仅限管理员和教师使用，学生请使用其他 AI 功能");
        }

        String context = buildTeachingContext(user, jsonObject.getString("taskId"));
        if (context == null) {
            return Result.fail("课程不存在或无权访问");
        }
        String task = "请生成一份教师备课方案，要求包含教学目标、重难点、课堂流程、互动问题、板书/课件结构、课后作业建议。"
                + "\n备课主题：" + text(jsonObject, "topic", "结合课程章节进行备课")
                + "\n课时长度：" + text(jsonObject, "minutes", "45") + " 分钟"
                + "\n授课对象：" + text(jsonObject, "students", "平台课程学生")
                + "\n教学目标：" + text(jsonObject, "goal", "帮助学生理解核心知识并能完成基础练习");
        Result abuseCheck = validateAiRequest(user, AGENT_TYPE_LESSON_PREPARE, task, task + "\n" + context, false);
        if (abuseCheck != null) {
            return abuseCheck;
        }
        AiGenerationResult aiResult = aiService.runAgent(getRole(user), "AI 教师备课助手", task, context);
        Result providerFailure = providerFailure(aiResult);
        if (providerFailure != null) {
            return providerFailure;
        }
        String reply = aiResult.getContent();
        ApeAiHistory history = saveHistory(user.getId(), user.getUserName(), AGENT_TYPE_LESSON_PREPARE, task, reply, context,
                aiResult.getPromptTokens(), aiResult.getCompletionTokens());
        return Result.success(replyData(reply, history));
    }

    @Log(name = "AI自动总结课程", type = BusinessType.OTHER)
    @PostMapping("/course-summary")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result courseSummary(@RequestBody JSONObject jsonObject) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录后再使用 AI 课程总结");
        }

        String context = buildCourseSummaryContext(user, jsonObject.getString("taskId"));
        if (context == null) {
            return Result.fail("课程不存在或无权访问");
        }
        String task = "请自动总结课程，输出：课程主线、章节知识图谱、重点难点、学习成果、复习建议和可用于期末/答辩的概括。"
                + "\n总结侧重点：" + text(jsonObject, "focus", "帮助学生快速复盘课程重点");
        Result abuseCheck = validateAiRequest(user, AGENT_TYPE_COURSE_SUMMARY, task, task + "\n" + context, false);
        if (abuseCheck != null) {
            return abuseCheck;
        }
        AiGenerationResult aiResult = aiService.runAgent(getRole(user), "AI 自动总结课程", task, context);
        Result providerFailure = providerFailure(aiResult);
        if (providerFailure != null) {
            return providerFailure;
        }
        String reply = aiResult.getContent();
        ApeAiHistory history = saveHistory(user.getId(), user.getUserName(), AGENT_TYPE_COURSE_SUMMARY, task, reply, context,
                aiResult.getPromptTokens(), aiResult.getCompletionTokens());
        return Result.success(replyData(reply, history));
    }

    /** 数据整理 Agent */
    @Log(name = "AI数据整理", type = BusinessType.OTHER)
    @PostMapping("/data-agent")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result dataAgent(@RequestBody JSONObject jsonObject) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录后再使用 AI 数据整理");
        }
        String agentType = jsonObject.getString("agentType");
        if (StringUtils.isBlank(agentType)) {
            return Result.fail("agentType 不能为空");
        }
        boolean isAdmin = isAdmin(user);
        boolean isTeacher = isTeacher(user);
        if (!isAdmin && !isTeacher) {
            return Result.fail("数据整理功能仅限管理员和教师使用");
        }

        String task;
        String context;
        String historyAgentType;

        switch (agentType) {
            case AGENT_TYPE_PLATFORM_REPORT:
                if (!isAdmin) {
                    return Result.fail("平台运营周报仅限管理员使用");
                }
                task = "请生成一份平台运营周报，要求包含：平台整体数据概览、变化趋势、关注事项和改进建议。";
                context = buildPlatformReportContext();
                historyAgentType = AGENT_TYPE_PLATFORM_REPORT;
                break;

            case AGENT_TYPE_COURSE_HEALTH:
                context = buildCourseHealthContext(jsonObject.getString("taskId"), user);
                if (context == null) {
                    return Result.fail("课程不存在或无权访问");
                }
                task = "请生成一份课程健康度诊断报告，要求包含：课程参与度、学习进度分析、作业考试表现、问题和改进建议。";
                historyAgentType = AGENT_TYPE_COURSE_HEALTH;
                break;

            case AGENT_TYPE_EXAM_ANALYSIS:
                context = buildExamAnalysisContext(jsonObject.getString("testId"), user);
                if (context == null) {
                    return Result.fail("考试不存在或无权访问");
                }
                task = "请生成一份考试质量分析报告，要求包含：成绩分布、题目难度分析、常见错误类型、教学建议。";
                historyAgentType = AGENT_TYPE_EXAM_ANALYSIS;
                break;

            default:
                return Result.fail("不支持的 agentType，可选：platform-report / course-health / exam-analysis");
        }

        Result abuseCheck = validateAiRequest(user, historyAgentType, task, task + "\n" + context, false);
        if (abuseCheck != null) {
            return abuseCheck;
        }
        AiGenerationResult aiResult = aiService.runAgent(
                getRole(user),
                "AI 数据整理 — " + agentType, task, context);
        Result providerFailure = providerFailure(aiResult);
        if (providerFailure != null) {
            return providerFailure;
        }
        String reply = aiResult.getContent();
        ApeAiHistory history = saveHistory(user.getId(), user.getUserName(), historyAgentType, task, reply, context,
                aiResult.getPromptTokens(), aiResult.getCompletionTokens());
        return Result.success(replyData(reply, history));
    }

    /** 平台运营周报上下文 */
    private String buildPlatformReportContext() {
        StringBuilder sb = new StringBuilder();
        int totalUsers = (int) apeUserService.count();
        int teacherCount = (int) apeUserService.count(
                new QueryWrapper<ApeUser>().lambda().eq(ApeUser::getUserType, Constants.USER_TYPE_TEACHER));
        int studentCount = (int) apeUserService.count(
                new QueryWrapper<ApeUser>().lambda().eq(ApeUser::getUserType, Constants.USER_TYPE_STUDENT));
        int totalCourses = (int) apeTaskService.count();
        int totalExams = (int) apeTestService.count();
        int totalArticles = (int) apeArticleService.count();
        int totalForums = (int) apeForumService.count();

        // 近7天活跃
        Date sevenDaysAgo = new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000);
        int recentLogins = (int) apeLoginLogService.count(
                new QueryWrapper<ApeLoginLog>().lambda().ge(ApeLoginLog::getLoginTime, sevenDaysAgo));
        int recentExams = (int) apeTestStudentService.count(
                new QueryWrapper<ApeTestStudent>().lambda().ge(ApeTestStudent::getCreateTime, sevenDaysAgo));
        int recentHomework = (int) apeHomeworkStudentService.count(
                new QueryWrapper<ApeHomeworkStudent>().lambda().ge(ApeHomeworkStudent::getCreateTime, sevenDaysAgo));

        sb.append("=== 平台运营数据快照 ===\n");
        sb.append("用户总数：").append(totalUsers).append("（教师 ").append(teacherCount)
                .append("、学生 ").append(studentCount).append("）\n");
        sb.append("课程总数：").append(totalCourses).append("\n");
        sb.append("考试总数：").append(totalExams).append("\n");
        sb.append("笔记文章：").append(totalArticles).append("\n");
        sb.append("论坛主题：").append(totalForums).append("\n\n");
        sb.append("=== 近7天活跃数据 ===\n");
        sb.append("登录人次：").append(recentLogins).append("\n");
        sb.append("考试提交：").append(recentExams).append("\n");
        sb.append("作业提交：").append(recentHomework).append("\n");

        return sb.toString();
    }

    /** 课程健康度诊断上下文 */
    private String buildCourseHealthContext(String taskId, ApeUser user) {
        if (StringUtils.isBlank(taskId)) {
            return "未指定课程，请选择一门课程进行诊断。\n";
        }
        ApeTask task = apeTaskService.getById(taskId);
        if (task == null) {
            return null;
        }
        if (!canTeachTask(user, task)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("课程名称：").append(task.getName()).append("\n");
        sb.append("授课教师：").append(safe(task.getTeacherName())).append("\n");
        sb.append("专业/分类：").append(safe(task.getMajor())).append("/").append(safe(task.getClassification())).append("\n");

        // 报名人数
        int enrolled = (int) apeTaskStudentService.count(
                new QueryWrapper<ApeTaskStudent>().lambda().eq(ApeTaskStudent::getTaskId, taskId).eq(ApeTaskStudent::getState, 0));
        sb.append("报名人数：").append(enrolled).append("\n");

        // 章节进度
        List<ApeChapter> chapters = getChapters(taskId);
        sb.append("章节总数：").append(chapters.size()).append("\n");
        if (!chapters.isEmpty()) {
            sb.append("章节列表：");
            for (int i = 0; i < Math.min(chapters.size(), 10); i++) {
                sb.append(chapters.get(i).getName()).append(i < Math.min(chapters.size(), 10) - 1 ? "、" : "");
            }
            sb.append("\n");
        }

        // 作业统计
        int homeworkTotal = 0;
        for (ApeChapter chapter : chapters) {
            int count = (int) apeHomeworkService.count(
                    new QueryWrapper<ApeHomework>().lambda().eq(ApeHomework::getChapterId, chapter.getId()));
            homeworkTotal += count;
        }
        int homeworkSubmitted = 0;
        for (ApeChapter chapter : chapters) {
            homeworkSubmitted += (int) apeHomeworkStudentService.count(
                    new QueryWrapper<ApeHomeworkStudent>().lambda().eq(ApeHomeworkStudent::getChapterId, chapter.getId()));
        }
        sb.append("作业题目总数：").append(homeworkTotal).append("，已提交人次：").append(homeworkSubmitted).append("\n");

        // 考试统计
        List<ApeTest> tests = apeTestService.list(
                new QueryWrapper<ApeTest>().lambda().eq(ApeTest::getTaskId, taskId).eq(ApeTest::getState, 0));
        sb.append("关联考试数：").append(tests.size()).append("\n");
        for (ApeTest test : tests) {
            int testSubmissions = (int) apeTestStudentService.count(
                    new QueryWrapper<ApeTestStudent>().lambda().eq(ApeTestStudent::getTestId, test.getId()));
            sb.append("  - ").append(test.getName()).append("：总分").append(test.getTotalScore())
                    .append("，时长").append(test.getTotalTime()).append("分钟")
                    .append("，提交人数").append(testSubmissions).append("\n");
        }

        return sb.toString();
    }

    /** 考试质量分析上下文 */
    private String buildExamAnalysisContext(String testId, ApeUser user) {
        if (StringUtils.isBlank(testId)) {
            return "未指定考试，请选择一场考试进行分析。\n";
        }
        ApeTest test = apeTestService.getById(testId);
        if (test == null) {
            return null;
        }
        if (!canAnalyzeTest(user, test)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("考试名称：").append(test.getName()).append("\n");
        sb.append("所属课程：").append(safe(test.getTaskName())).append("\n");
        sb.append("总分：").append(test.getTotalScore()).append("，时长：").append(test.getTotalTime()).append("分钟\n");
        sb.append("开始时间：").append(test.getStartTime()).append("，截止时间：").append(test.getEndTime()).append("\n");

        // 题目列表
        List<ApeTestItem> items = apeTestItemService.list(
                new QueryWrapper<ApeTestItem>().lambda().eq(ApeTestItem::getTestId, testId).orderByAsc(ApeTestItem::getSort));
        sb.append("题目总数：").append(items.size()).append("\n");

        // 成绩分布
        List<ApeTestStudent> submissions = apeTestStudentService.list(
                new QueryWrapper<ApeTestStudent>().lambda().eq(ApeTestStudent::getTestId, testId));
        int submitCount = submissions.size();
        sb.append("提交人数：").append(submitCount).append("\n");

        if (submitCount > 0) {
            // 按用户去重后计算总分
            java.util.Map<String, Integer> userScoreMap = new java.util.LinkedHashMap<>();
            for (ApeTestStudent s : submissions) {
                Integer prev = userScoreMap.getOrDefault(s.getUserId(), 0);
                userScoreMap.put(s.getUserId(), prev + (s.getPoint() != null ? s.getPoint() : 0));
            }
            int totalScore = test.getTotalScore() != null ? test.getTotalScore() : 100;
            int passCount = 0;
            int sum = 0;
            for (int score : userScoreMap.values()) {
                sum += score;
                if (score >= totalScore * 0.6) passCount++;
            }
            double avg = userScoreMap.isEmpty() ? 0 : (double) sum / userScoreMap.size();
            sb.append("参与学生数：").append(userScoreMap.size()).append("\n");
            sb.append("平均分：").append(String.format("%.1f", avg)).append(" / ").append(totalScore).append("\n");
            sb.append("及格率（≥60%）：").append(passCount).append("/").append(userScoreMap.size())
                    .append("（").append(String.format("%.0f%%", 100.0 * passCount / userScoreMap.size())).append("）\n");

            // 各题正确率（客观题）
            sb.append("\n各题答题情况：\n");
            for (ApeTestItem item : items) {
                int itemSubmissions = (int) apeTestStudentService.count(
                        new QueryWrapper<ApeTestStudent>().lambda()
                                .eq(ApeTestStudent::getTestId, testId)
                                .eq(ApeTestStudent::getItemId, item.getId()));
                int itemCorrect = 0;
                List<ApeTestStudent> itemAnswers = apeTestStudentService.list(
                        new QueryWrapper<ApeTestStudent>().lambda()
                                .eq(ApeTestStudent::getTestId, testId)
                                .eq(ApeTestStudent::getItemId, item.getId()));
                for (ApeTestStudent ans : itemAnswers) {
                    if (ans.getPoint() != null && ans.getScore() != null && ans.getPoint() >= ans.getScore()) {
                        itemCorrect++;
                    }
                }
                String typeName = Constants.getQuestionTypeName(item.getType());
                sb.append("  ").append(item.getSort()).append(". [").append(typeName).append("] ")
                        .append(shortText(item.getTitle(), 60))
                        .append("（满分").append(item.getScore()).append("分")
                        .append("，正确").append(itemCorrect).append("/").append(itemSubmissions);
                if (itemSubmissions > 0) {
                    sb.append("，").append(String.format("%.0f%%", 100.0 * itemCorrect / itemSubmissions));
                }
                sb.append("）\n");
            }
        }

        return sb.toString();
    }

    /** 课程统计图表数据 */
    @Log(name = "课程统计数据", type = BusinessType.OTHER)
    @GetMapping("/course-stats")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result courseStats(@RequestParam("taskId") String taskId) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) return Result.fail("请先登录");

        ApeTask task = apeTaskService.getById(taskId);
        if (task == null) return Result.fail("课程不存在");
        if (!canTeachTask(user, task)) return Result.fail("无权访问");

        JSONObject data = new JSONObject();
        data.put("courseName", task.getName());

        // 报名人数
        int enrolled = (int) apeTaskStudentService.count(
                new QueryWrapper<ApeTaskStudent>().lambda().eq(ApeTaskStudent::getTaskId, taskId).eq(ApeTaskStudent::getState, 0));
        data.put("enrolled", enrolled);

        // 章节统计
        List<ApeChapter> chapters = getChapters(taskId);
        data.put("chapterCount", chapters.size());

        JSONArray chapterNames = new JSONArray();
        for (ApeChapter ch : chapters) { chapterNames.add(ch.getName()); }
        data.put("chapterNames", chapterNames);

        // 作业统计
        int homeworkTotal = 0;
        int homeworkSubmitted = 0;
        for (ApeChapter ch : chapters) {
            homeworkTotal += (int) apeHomeworkService.count(
                    new QueryWrapper<ApeHomework>().lambda().eq(ApeHomework::getChapterId, ch.getId()));
            homeworkSubmitted += (int) apeHomeworkStudentService.count(
                    new QueryWrapper<ApeHomeworkStudent>().lambda().eq(ApeHomeworkStudent::getChapterId, ch.getId()));
        }
        data.put("homeworkTotal", homeworkTotal);
        data.put("homeworkSubmitted", homeworkSubmitted);

        // 考试统计
        List<ApeTest> tests = apeTestService.list(
                new QueryWrapper<ApeTest>().lambda().eq(ApeTest::getTaskId, taskId).eq(ApeTest::getState, 0));
        data.put("examCount", tests.size());
        int examTotalScore = 0;
        for (ApeTest t : tests) { if (t.getTotalScore() != null) examTotalScore += t.getTotalScore(); }
        data.put("examTotalScore", examTotalScore);

        return Result.success(data);
    }

    /** 考试统计图表数据 */
    @Log(name = "考试统计数据", type = BusinessType.OTHER)
    @GetMapping("/exam-stats")
    @RequiresRoles(value = {"admin", "teacher"}, logical = Logical.OR)
    public Result examStats(@RequestParam("testId") String testId) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) return Result.fail("请先登录");

        ApeTest test = apeTestService.getById(testId);
        if (test == null) return Result.fail("考试不存在");
        if (!canAnalyzeTest(user, test)) return Result.fail("无权访问");

        JSONObject data = new JSONObject();
        data.put("testName", test.getName());
        data.put("totalScore", test.getTotalScore());
        data.put("totalTime", test.getTotalTime());

        // 提交统计
        List<ApeTestStudent> submissions = apeTestStudentService.list(
                new QueryWrapper<ApeTestStudent>().lambda().eq(ApeTestStudent::getTestId, testId));
        data.put("totalSubmissions", submissions.size());

        // 按用户去重算分
        java.util.Map<String, Integer> userScores = new java.util.LinkedHashMap<>();
        for (ApeTestStudent s : submissions) {
            userScores.merge(s.getUserId(), s.getPoint() != null ? s.getPoint() : 0, Integer::sum);
        }
        data.put("studentCount", userScores.size());

        // 分数分布
        int[] buckets = new int[5]; // 0-19,20-39,40-59,60-79,80-100
        int passCount = 0;
        int totalScore = test.getTotalScore() != null ? test.getTotalScore() : 100;
        double sum = 0;
        for (int score : userScores.values()) {
            sum += score;
            if (score >= totalScore * 0.6) passCount++;
            int idx = Math.min(4, Math.max(0, score * 5 / totalScore));
            buckets[idx]++;
        }
        double avg = userScores.isEmpty() ? 0 : sum / userScores.size();

        data.put("avgScore", Math.round(avg * 10) / 10.0);
        data.put("passCount", passCount);
        data.put("passRate", userScores.isEmpty() ? 0 : Math.round(1000.0 * passCount / userScores.size()) / 10.0);

        JSONArray scoreDist = new JSONArray();
        String[] labels = {"0-19%", "20-39%", "40-59%", "60-79%", "80-100%"};
        for (int i = 0; i < 5; i++) {
            JSONObject b = new JSONObject();
            b.put("label", labels[i]);
            b.put("count", buckets[i]);
            scoreDist.add(b);
        }
        data.put("scoreDistribution", scoreDist);

        // 各题正确率
        List<ApeTestItem> items = apeTestItemService.list(
                new QueryWrapper<ApeTestItem>().lambda().eq(ApeTestItem::getTestId, testId).orderByAsc(ApeTestItem::getSort));
        JSONArray qAccuracy = new JSONArray();
        for (ApeTestItem item : items) {
            JSONObject q = new JSONObject();
            q.put("sort", item.getSort());
            q.put("title", shortText(item.getTitle(), 20));
            q.put("score", item.getScore());
            int total = (int) apeTestStudentService.count(
                    new QueryWrapper<ApeTestStudent>().lambda().eq(ApeTestStudent::getTestId, testId).eq(ApeTestStudent::getItemId, item.getId()));
            int correct = 0;
            for (ApeTestStudent ans : apeTestStudentService.list(
                    new QueryWrapper<ApeTestStudent>().lambda().eq(ApeTestStudent::getTestId, testId).eq(ApeTestStudent::getItemId, item.getId()))) {
                if (ans.getPoint() != null && ans.getScore() != null && ans.getPoint() >= ans.getScore()) correct++;
            }
            q.put("total", total);
            q.put("correct", correct);
            q.put("rate", total > 0 ? Math.round(1000.0 * correct / total) / 10.0 : 0);
            qAccuracy.add(q);
        }
        data.put("questionAccuracy", qAccuracy);

        return Result.success(data);
    }

    @Log(name = "AI历史查询", type = BusinessType.OTHER)
    @PostMapping("/history")
    public Result history(@RequestBody JSONObject jsonObject) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录后再查看 AI 历史");
        }

        String agentType = jsonObject.getString("agentType");
        int pageNumber = intParam(jsonObject, "pageNumber", 1, 1, 1000);
        int pageSize = intParam(jsonObject, "pageSize", 20, 1, 50);
        Page<ApeAiHistory> page = new Page<>(pageNumber, pageSize);
        QueryWrapper<ApeAiHistory> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(ApeAiHistory::getUserId, user.getId())
                .eq(StringUtils.isNotBlank(agentType), ApeAiHistory::getAgentType, StringUtils.trim(agentType))
                .orderByDesc(ApeAiHistory::getCreateTime);
        return Result.success(apeAiHistoryService.page(page, wrapper));
    }

    @Log(name = "AI历史删除", type = BusinessType.DELETE)
    @PostMapping("/history/delete")
    public Result deleteHistory(@RequestBody JSONObject jsonObject) {
        ApeUser user = ShiroUtils.getUserInfo();
        if (user == null) {
            return Result.fail("请先登录后再删除 AI 历史");
        }

        String id = jsonObject.getString("id");
        if (StringUtils.isBlank(id)) {
            return Result.fail("历史记录id不能为空");
        }

        QueryWrapper<ApeAiHistory> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(ApeAiHistory::getId, id)
                .eq(ApeAiHistory::getUserId, user.getId());
        boolean removed = apeAiHistoryService.remove(wrapper);
        if (removed) {
            return Result.success();
        }
        return Result.fail("历史记录不存在或无权删除");
    }

    
    private Result validateAiRequest(ApeUser user, String agentType, String userInput, boolean requirePlatformTopic) {
        return validateAiRequest(user, agentType, userInput, userInput, requirePlatformTopic);
    }

    private Result validateAiRequest(ApeUser user, String agentType, String userInput,
                                     String budgetText, boolean requirePlatformTopic) {
        AiLimitPolicy policy = getAiLimitPolicy(user);
        if (policy == null) {
            return Result.fail("当前账号角色暂不支持 AI 功能");
        }

        String input = StringUtils.trimToEmpty(userInput);
        if (input.length() > policy.inputMaxChars) {
            return Result.fail(policy.roleLabel + "单次 AI 输入不能超过 " + policy.inputMaxChars + " 字，请拆成更短的问题。");
        }

        if (containsPromptAttack(input)) {
            return Result.fail(AI_SECURITY_REJECT_MESSAGE);
        }

        if (requirePlatformTopic && isStudent(user) && !isStudentAiTopicRelated(input)) {
            return Result.fail(STUDENT_AI_OFF_TOPIC_MESSAGE);
        }

        AiUsage usage = getTodayAiUsage(user.getId());
        if (usage.callCount >= policy.dailyCallLimit) {
            return Result.fail("今日" + policy.roleLabel + "AI 使用次数已达上限（" + policy.dailyCallLimit + " 次），请明天再试。");
        }
        if (usage.totalTokens >= policy.dailyTokenLimit) {
            return Result.fail("今日" + policy.roleLabel + "AI token 额度已用完（" + policy.dailyTokenLimit + " tokens），请明天再试。");
        }
        int reservedTokens = Math.max(policy.reservedTokens, estimateTokens(budgetText));
        if (usage.totalTokens + reservedTokens > policy.dailyTokenLimit) {
            return Result.fail("今日" + policy.roleLabel + "AI 剩余额度不足，请明天再试。");
        }

        Result dbCooldownCheck = checkHistoryCooldown(usage.lastCreateTime, policy.cooldownSeconds);
        if (dbCooldownCheck != null) {
            return dbCooldownCheck;
        }
        return reserveAiCooldown(user.getId(), policy.cooldownSeconds);
    }

    private AiUsage getTodayAiUsage(String userId) {
        AiUsage usage = new AiUsage();
        if (StringUtils.isBlank(userId)) {
            return usage;
        }

        Date startOfToday = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        QueryWrapper<ApeAiHistory> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(ApeAiHistory::getUserId, userId)
                .ge(ApeAiHistory::getCreateTime, startOfToday)
                .orderByDesc(ApeAiHistory::getCreateTime);
        List<ApeAiHistory> histories = apeAiHistoryService.list(wrapper);
        usage.callCount = histories.size();
        for (ApeAiHistory history : histories) {
            usage.totalTokens += safeToken(history.getPromptTokens()) + safeToken(history.getCompletionTokens());
            if (usage.lastCreateTime == null && history.getCreateTime() != null) {
                usage.lastCreateTime = history.getCreateTime();
            }
        }
        return usage;
    }

    private Result checkHistoryCooldown(Date lastCreateTime, int cooldownSeconds) {
        if (lastCreateTime == null || cooldownSeconds <= 0) {
            return null;
        }
        long elapsedMillis = System.currentTimeMillis() - lastCreateTime.getTime();
        long cooldownMillis = cooldownSeconds * 1000L;
        if (elapsedMillis >= 0 && elapsedMillis < cooldownMillis) {
            long waitSeconds = Math.max(1, (cooldownMillis - elapsedMillis + 999) / 1000);
            return Result.fail("AI 请求过于频繁，请 " + waitSeconds + " 秒后再试。");
        }
        return null;
    }

    private Result reserveAiCooldown(String userId, int cooldownSeconds) {
        if (cooldownSeconds <= 0) {
            return null;
        }
        String key = StringUtils.defaultIfBlank(userId, "anonymous");
        long now = System.currentTimeMillis();
        long cooldownMillis = cooldownSeconds * 1000L;
        synchronized (AI_REQUEST_GUARD) {
            Long lastRequestTime = AI_REQUEST_GUARD.get(key);
            if (lastRequestTime != null && now - lastRequestTime < cooldownMillis) {
                long waitSeconds = Math.max(1, (cooldownMillis - (now - lastRequestTime) + 999) / 1000);
                return Result.fail("AI 请求过于频繁，请 " + waitSeconds + " 秒后再试。");
            }
            AI_REQUEST_GUARD.put(key, now);
        }
        return null;
    }

    private int estimateTokens(String text) {
        String value = StringUtils.trimToEmpty(text);
        if (value.isEmpty()) {
            return 0;
        }
        return Math.max(1, value.length());
    }

    private int safeToken(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private boolean containsPromptAttack(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        String normalized = input.toLowerCase();
        for (String keyword : AI_PROMPT_ATTACK_KEYWORDS) {
            if (normalized.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isStudentAiTopicRelated(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        String normalized = input.toLowerCase();
        for (String keyword : STUDENT_AI_OFF_TOPIC_KEYWORDS) {
            if (normalized.contains(keyword.toLowerCase())) {
                return false;
            }
        }
        for (String keyword : STUDENT_AI_TOPIC_KEYWORDS) {
            if (normalized.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private AiLimitPolicy getAiLimitPolicy(ApeUser user) {
        if (isAdmin(user)) {
            return new AiLimitPolicy("管理端", ADMIN_DAILY_AI_TOKEN_LIMIT, ADMIN_DAILY_AI_CALL_LIMIT,
                    ADMIN_AI_COOLDOWN_SECONDS, ADMIN_AI_INPUT_MAX_CHARS, ADMIN_AI_RESERVED_TOKENS);
        }
        if (isTeacher(user)) {
            return new AiLimitPolicy("教师端", TEACHER_DAILY_AI_TOKEN_LIMIT, TEACHER_DAILY_AI_CALL_LIMIT,
                    TEACHER_AI_COOLDOWN_SECONDS, TEACHER_AI_INPUT_MAX_CHARS, TEACHER_AI_RESERVED_TOKENS);
        }
        if (isStudent(user)) {
            return new AiLimitPolicy("学生端", STUDENT_DAILY_AI_TOKEN_LIMIT, STUDENT_DAILY_AI_CALL_LIMIT,
                    STUDENT_AI_COOLDOWN_SECONDS, STUDENT_AI_INPUT_MAX_CHARS, STUDENT_AI_RESERVED_TOKENS);
        }
        return null;
    }

    private String getRole(ApeUser user) {
        if (isAdmin(user)) {
            return Constants.ROLE_KEY_ADMIN;
        }
        if (isTeacher(user)) {
            return Constants.ROLE_KEY_TEACHER;
        }
        return Constants.ROLE_KEY_STUDENT;
    }

    private boolean isAdmin(ApeUser user) {
        return user != null && user.getUserType() != null && user.getUserType() == Constants.USER_TYPE_ADMIN;
    }

    private boolean isTeacher(ApeUser user) {
        return user != null && user.getUserType() != null && user.getUserType() == Constants.USER_TYPE_TEACHER;
    }

    private boolean isStudent(ApeUser user) {
        return user != null && user.getUserType() != null && user.getUserType() == Constants.USER_TYPE_STUDENT;
    }

    private boolean canAccessTask(ApeUser user, ApeTask task) {
        if (user == null || task == null) {
            return false;
        }
        if (isAdmin(user)) {
            return true;
        }
        if (isTeacher(user)) {
            return StringUtils.equals(user.getId(), task.getTeacherId());
        }
        if (isStudent(user)) {
            QueryWrapper<ApeTaskStudent> wrapper = new QueryWrapper<>();
            wrapper.lambda()
                    .eq(ApeTaskStudent::getUserId, user.getId())
                    .eq(ApeTaskStudent::getTaskId, task.getId())
                    .eq(ApeTaskStudent::getState, 0);
            return apeTaskStudentService.count(wrapper) > 0;
        }
        return false;
    }

    private boolean canTeachTask(ApeUser user, ApeTask task) {
        if (user == null || task == null) {
            return false;
        }
        return isAdmin(user) || (isTeacher(user) && StringUtils.equals(user.getId(), task.getTeacherId()));
    }

    private boolean canAnalyzeTest(ApeUser user, ApeTest test) {
        if (user == null || test == null) {
            return false;
        }
        if (isAdmin(user)) {
            return true;
        }
        if (!isTeacher(user)) {
            return false;
        }
        ApeTask task = apeTaskService.getById(test.getTaskId());
        return task != null && StringUtils.equals(user.getId(), task.getTeacherId());
    }

    private JSONObject replyData(String reply) {
        return replyData(reply, null);
    }

    private JSONObject replyData(String reply, ApeAiHistory history) {
        JSONObject data = new JSONObject();
        data.put("reply", reply);
        if (history != null) {
            data.put("historyId", history.getId());
            data.put("createTime", formatDate(history.getCreateTime()));
        }
        return data;
    }

    private Result providerFailure(AiGenerationResult result) {
        if (result == null || result.isSuccess()) {
            return result == null ? Result.fail("AI 服务未返回有效结果，请稍后再试。") : null;
        }
        return Result.fail(StringUtils.defaultIfBlank(result.getUserMessage(), "AI 服务暂时不可用，请稍后再试。"));
    }

    private ApeAiHistory saveHistory(String userId, String userName, String agentType,
                                     String userInput, String reply, String context,
                                     int promptTokens, int completionTokens) {
        ApeAiHistory history = new ApeAiHistory();
        history.setUserId(userId);
        history.setUserName(userName);
        history.setAgentType(agentType);
        history.setUserInput(shortText(userInput, 4000));
        history.setAiReply(reply);
        history.setContextSnapshot(shortText(context, 8000));
        history.setCreateTime(new Date());
        history.setPromptTokens(promptTokens);
        history.setCompletionTokens(completionTokens);
        apeAiHistoryService.save(history);
        return history;
    }

    private int intParam(JSONObject jsonObject, String key, int defaultValue, int min, int max) {
        Integer value = jsonObject.getInteger(key);
        if (value == null) {
            return defaultValue;
        }
        return Math.max(min, Math.min(value, max));
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private String text(JSONObject jsonObject, String key, String defaultValue) {
        String value = jsonObject.getString(key);
        return StringUtils.isBlank(value) ? defaultValue : value.trim();
    }

    private String buildStudyContext(ApeUser user, String taskId) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前学生：").append(safe(user.getUserName())).append("\n");
        List<ApeTaskStudent> taskStudents = getStudentTasks(user.getId(), taskId);
        if (taskStudents.isEmpty()) {
            sb.append("暂无已通过报名课程，学习计划需要先从选课或基础能力评估开始。\n");
            return sb.toString();
        }
        for (ApeTaskStudent taskStudent : taskStudents) {
            appendTaskProgress(sb, taskStudent.getTaskId(), user.getId());
        }
        return sb.toString();
    }

    private String buildTeachingContext(ApeUser user, String taskId) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前教师/用户：").append(safe(user.getUserName())).append("\n");
        List<String> taskIds = new ArrayList<>();
        String requestedTaskId = StringUtils.trimToNull(taskId);
        if (requestedTaskId != null) {
            ApeTask task = apeTaskService.getById(requestedTaskId);
            if (!canTeachTask(user, task)) {
                return null;
            }
            taskIds.add(task.getId());
        } else {
            QueryWrapper<ApeTask> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(!isAdmin(user), ApeTask::getTeacherId, user.getId());
            wrapper.last("limit 3");
            for (ApeTask task : apeTaskService.list(wrapper)) {
                taskIds.add(task.getId());
            }
        }
        if (taskIds.isEmpty()) {
            sb.append("暂无教师本人课程，备课方案将主要依据用户填写的主题生成。\n");
            return sb.toString();
        }
        for (String id : taskIds) {
            appendTaskOutline(sb, id, false);
        }
        return sb.toString();
    }

    private String buildCourseSummaryContext(ApeUser user, String taskId) {
        StringBuilder sb = new StringBuilder();
        String requestedTaskId = StringUtils.trimToNull(taskId);
        if (requestedTaskId != null) {
            ApeTask task = apeTaskService.getById(requestedTaskId);
            if (!canAccessTask(user, task)) {
                return null;
            }
            appendTaskOutline(sb, task.getId(), true);
            return sb.toString();
        }

        if (isAdmin(user) || isTeacher(user)) {
            QueryWrapper<ApeTask> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(isTeacher(user), ApeTask::getTeacherId, user.getId());
            wrapper.last("limit 3");
            List<ApeTask> tasks = apeTaskService.list(wrapper);
            if (tasks.isEmpty()) {
                sb.append(isAdmin(user) ? "暂无课程可总结。\n" : "暂无教师课程可总结。\n");
            }
            for (ApeTask task : tasks) {
                appendTaskOutline(sb, task.getId(), true);
            }
        } else if (isStudent(user)) {
            List<ApeTaskStudent> taskStudents = getStudentTasks(user.getId(), null);
            if (taskStudents.isEmpty()) {
                sb.append("暂无已报名课程可总结。\n");
            }
            for (ApeTaskStudent taskStudent : taskStudents) {
                appendTaskProgress(sb, taskStudent.getTaskId(), user.getId());
            }
        } else {
            return null;
        }
        return sb.toString();
    }

    private List<ApeTaskStudent> getStudentTasks(String userId, String taskId) {
        QueryWrapper<ApeTaskStudent> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(ApeTaskStudent::getUserId, userId)
                .eq(ApeTaskStudent::getState, 0)
                .eq(StringUtils.isNotBlank(taskId), ApeTaskStudent::getTaskId, taskId);
        wrapper.last("limit 4");
        return apeTaskStudentService.list(wrapper);
    }

    private void appendTaskProgress(StringBuilder sb, String taskId, String userId) {
        ApeTask task = apeTaskService.getById(taskId);
        if (task == null) {
            return;
        }
        sb.append("\n课程：").append(safe(task.getName()))
                .append("；教师：").append(safe(task.getTeacherName()))
                .append("；专业/分类：").append(safe(task.getMajor())).append("/").append(safe(task.getClassification()))
                .append("\n课程简介：").append(shortText(task.getTaskDescribe(), 160)).append("\n");

        List<ApeChapter> chapters = getChapters(taskId);
        int watched = 0;
        int homeworkDone = 0;
        int homeworkChapterTotal = 0;
        int homeworkTotal = 0;
        for (ApeChapter chapter : chapters) {
            if (hasWatched(chapter.getId(), userId)) {
                watched++;
            }
            int chapterHomeworkCount = countHomework(chapter.getId());
            homeworkTotal += chapterHomeworkCount;
            if (chapterHomeworkCount > 0) {
                homeworkChapterTotal++;
                if (hasHomeworkDone(chapter.getId(), userId)) {
                    homeworkDone++;
                }
            }
        }
        sb.append("学习进度：章节 ").append(watched).append("/").append(chapters.size())
                .append(" 已看；有作业章节 ").append(homeworkDone).append("/").append(homeworkChapterTotal)
                .append(" 已完成；作业题目总数 ").append(homeworkTotal).append("。\n");
        appendChapterBrief(sb, chapters);
    }

    private void appendTaskOutline(StringBuilder sb, String taskId, boolean includeHomework) {
        ApeTask task = apeTaskService.getById(taskId);
        if (task == null) {
            return;
        }
        sb.append("\n课程：").append(safe(task.getName()))
                .append("；教师：").append(safe(task.getTeacherName()))
                .append("；专业/分类：").append(safe(task.getMajor())).append("/").append(safe(task.getClassification()))
                .append("\n课程简介：").append(shortText(task.getTaskDescribe(), 220)).append("\n");
        List<ApeChapter> chapters = getChapters(taskId);
        appendChapterBrief(sb, chapters);
        if (includeHomework) {
            appendHomeworkBrief(sb, chapters);
        }
    }

    private void appendChapterBrief(StringBuilder sb, List<ApeChapter> chapters) {
        if (chapters.isEmpty()) {
            sb.append("章节：暂无章节数据。\n");
            return;
        }
        sb.append("章节列表：\n");
        int limit = Math.min(chapters.size(), 10);
        for (int i = 0; i < limit; i++) {
            ApeChapter chapter = chapters.get(i);
            sb.append(i + 1).append(". ").append(safe(chapter.getName()));
            if (StringUtils.isNotBlank(chapter.getRemark())) {
                sb.append("（").append(shortText(chapter.getRemark(), 80)).append("）");
            }
            sb.append("\n");
        }
        if (chapters.size() > limit) {
            sb.append("其余章节数量：").append(chapters.size() - limit).append("\n");
        }
    }

    private void appendHomeworkBrief(StringBuilder sb, List<ApeChapter> chapters) {
        sb.append("章节作业概览：\n");
        int count = 0;
        for (ApeChapter chapter : chapters) {
            QueryWrapper<ApeHomework> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(ApeHomework::getChapterId, chapter.getId()).orderByAsc(ApeHomework::getSort);
            wrapper.last("limit 3");
            List<ApeHomework> homeworkList = apeHomeworkService.list(wrapper);
            for (ApeHomework homework : homeworkList) {
                count++;
                sb.append("- ").append(safe(chapter.getName())).append("：")
                        .append(safe(homework.getTitle()))
                        .append("，题型：").append(Constants.getQuestionTypeName(homework.getType()))
                        .append("，分值：").append(homework.getScore() == null ? 0 : homework.getScore())
                        .append("\n");
                if (count >= 8) {
                    return;
                }
            }
        }
        if (count == 0) {
            sb.append("暂无作业题目。\n");
        }
    }

    private List<ApeChapter> getChapters(String taskId) {
        QueryWrapper<ApeChapter> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ApeChapter::getTaskId, taskId);
        return apeChapterService.list(wrapper);
    }

    private boolean hasWatched(String chapterId, String userId) {
        QueryWrapper<ApeChapterVideo> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ApeChapterVideo::getChapterId, chapterId).eq(ApeChapterVideo::getUserId, userId);
        return apeChapterVideoService.count(wrapper) > 0;
    }

    private int countHomework(String chapterId) {
        QueryWrapper<ApeHomework> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ApeHomework::getChapterId, chapterId);
        return apeHomeworkService.count(wrapper);
    }

    private boolean hasHomeworkDone(String chapterId, String userId) {
        QueryWrapper<ApeHomeworkStudent> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ApeHomeworkStudent::getChapterId, chapterId).eq(ApeHomeworkStudent::getUserId, userId);
        return apeHomeworkStudentService.count(wrapper) > 0;
    }

    private WrongContext buildWrongContext(ApeUser user, int limit, JSONArray itemIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前学生：").append(safe(user.getUserName())).append("\n");

        java.util.Set<String> idSet = null;
        if (itemIds != null && !itemIds.isEmpty()) {
            idSet = new java.util.HashSet<>();
            for (int i = 0; i < itemIds.size(); i++) idSet.add(itemIds.getString(i));
        }

        List<String> wrongLines = new ArrayList<>();
        QueryWrapper<ApeHomeworkStudent> homeworkWrapper = new QueryWrapper<>();
        homeworkWrapper.lambda().eq(ApeHomeworkStudent::getUserId, user.getId())
                .orderByAsc(ApeHomeworkStudent::getChapterId)
                .orderByAsc(ApeHomeworkStudent::getSort);
        List<ApeHomeworkStudent> homeworkList = apeHomeworkStudentService.list(homeworkWrapper).stream()
                .filter(item -> !isHomeworkAnswerCorrect(item))
                .collect(Collectors.toList());
        for (ApeHomeworkStudent item : homeworkList) {
            String itemId = "hw_" + item.getId();
            if (idSet != null && !idSet.contains(itemId)) continue;
            if (idSet == null && wrongLines.size() >= limit) break;
            fillHomeworkCourseName(item);
            wrongLines.add("作业错题｜课程：" + safe(item.getTaskName()) + "｜章节：" + safe(item.getChapterName())
                    + "｜题目：" + safe(item.getTitle())
                    + "｜题型：" + Constants.getQuestionTypeName(item.getType())
                    + "｜正确答案：" + safe(item.getAnswer())
                    + "｜学生答案：" + safe(item.getSolution())
                    + "｜得分：" + (item.getPoint() == null ? 0 : item.getPoint()) + "/" + (item.getScore() == null ? 0 : item.getScore()));
        }

        QueryWrapper<ApeTestStudent> testWrapper = new QueryWrapper<>();
        testWrapper.lambda().eq(ApeTestStudent::getUserId, user.getId())
                .orderByAsc(ApeTestStudent::getTestId)
                .orderByAsc(ApeTestStudent::getSort);
        List<ApeTestStudent> testList = apeTestStudentService.list(testWrapper).stream()
                .filter(this::isWrongTestAnswer)
                .collect(Collectors.toList());
        for (ApeTestStudent item : testList) {
            String itemId = "test_" + item.getId();
            if (idSet != null && !idSet.contains(itemId)) continue;
            if (idSet == null && wrongLines.size() >= limit) break;
            wrongLines.add("考试错题｜考试：" + safe(item.getTestName())
                    + "｜题目：" + safe(item.getTitle())
                    + "｜题型：" + Constants.getQuestionTypeName(item.getType())
                    + "｜正确答案：" + safe(item.getAnswer())
                    + "｜学生答案：" + safe(item.getSolution())
                    + "｜得分：" + (item.getPoint() == null ? 0 : item.getPoint()) + "/" + (item.getScore() == null ? 0 : item.getScore()));
        }

        int total = homeworkList.size() + testList.size();
        if (wrongLines.isEmpty()) {
            sb.append("暂无错题记录。请给出保持学习状态和预防遗忘的建议。\n");
        } else {
            sb.append("错题总数：").append(total).append("，以下为前 ").append(wrongLines.size()).append(" 道代表性错题：\n");
            for (int i = 0; i < wrongLines.size(); i++) {
                sb.append(i + 1).append(". ").append(wrongLines.get(i)).append("\n");
            }
        }
        return new WrongContext(sb.toString(), total);
    }

    private void fillHomeworkCourseName(ApeHomeworkStudent student) {
        if (StringUtils.isBlank(student.getChapterId())) {
            return;
        }
        ApeChapter chapter = apeChapterService.getById(student.getChapterId());
        if (chapter == null) {
            return;
        }
        student.setTaskName(chapter.getTaskName());
        student.setChapterName(chapter.getName());
    }

    private boolean isWrongTestAnswer(ApeTestStudent testStudent) {
        if (testStudent.getPoint() != null && testStudent.getScore() != null
                && testStudent.getPoint() >= testStudent.getScore()) {
            return false;
        }
        if (testStudent.getScore() != null && testStudent.getScore() > 0
                && testStudent.getPoint() == null) {
            return false;
        }
        return true;
    }

    private boolean isHomeworkAnswerCorrect(ApeHomeworkStudent homeworkStudent) {
        if (homeworkStudent == null || StringUtils.isBlank(homeworkStudent.getAnswer())
                || StringUtils.isBlank(homeworkStudent.getSolution())) {
            return false;
        }
        if (Integer.valueOf(1).equals(homeworkStudent.getType())) {
            return normalizeMultipleAnswer(homeworkStudent.getAnswer())
                    .equals(normalizeMultipleAnswer(homeworkStudent.getSolution()));
        }
        return homeworkStudent.getAnswer().trim().equals(homeworkStudent.getSolution().trim());
    }

    private String normalizeMultipleAnswer(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .sorted()
                .collect(Collectors.joining(","));
    }


    private String safe(String value) {
        return StringUtils.isBlank(value) ? "暂无" : value.trim();
    }

    private String shortText(String value, int maxLength) {
        if (StringUtils.isBlank(value)) {
            return "暂无";
        }
        String text = value.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /** 查询 AI 用量摘要 */
    @Log(name = "AI用量查询", type = BusinessType.OTHER)
    @GetMapping("/usage")
    @RequiresRoles("admin")
    public Result getAiUsage() {
        ApeUser user = ShiroUtils.getUserInfo();
        if (!isAdmin(user)) {
            return Result.fail("仅限管理员查看");
        }

        JSONObject result = new JSONObject();
        result.put("studentDailyTokenLimit", STUDENT_DAILY_AI_TOKEN_LIMIT);
        result.put("studentDailyCallLimit", STUDENT_DAILY_AI_CALL_LIMIT);
        result.put("studentCooldownSeconds", STUDENT_AI_COOLDOWN_SECONDS);
        result.put("studentInputMaxChars", STUDENT_AI_INPUT_MAX_CHARS);
        result.put("teacherDailyTokenLimit", TEACHER_DAILY_AI_TOKEN_LIMIT);
        result.put("teacherDailyCallLimit", TEACHER_DAILY_AI_CALL_LIMIT);
        result.put("teacherCooldownSeconds", TEACHER_AI_COOLDOWN_SECONDS);
        result.put("teacherInputMaxChars", TEACHER_AI_INPUT_MAX_CHARS);
        result.put("adminDailyTokenLimit", ADMIN_DAILY_AI_TOKEN_LIMIT);
        result.put("adminDailyCallLimit", ADMIN_DAILY_AI_CALL_LIMIT);
        result.put("adminCooldownSeconds", ADMIN_AI_COOLDOWN_SECONDS);
        result.put("adminInputMaxChars", ADMIN_AI_INPUT_MAX_CHARS);

        // 生成历史只在 Provider 返回完整成功结果后写入，因此它是调用数和 token 的唯一事实来源。
        int dbTotalPrompt = 0;
        int dbTotalCompletion = 0;
        int dbCallCount = 0;
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        Map<LocalDate, int[]> usageByDay = new HashMap<>();
        try {
            List<ApeAiHistory> allHistory = apeAiHistoryService.list();
            for (ApeAiHistory h : allHistory) {
                int promptTokens = safeToken(h.getPromptTokens());
                int completionTokens = safeToken(h.getCompletionTokens());
                dbTotalPrompt += promptTokens;
                dbTotalCompletion += completionTokens;
                dbCallCount++;
                if (h.getCreateTime() != null) {
                    LocalDate historyDate = h.getCreateTime().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    if (!historyDate.isBefore(weekStart) && !historyDate.isAfter(today)) {
                        int[] day = usageByDay.computeIfAbsent(historyDate, ignored -> new int[3]);
                        day[0]++;
                        day[1] += promptTokens;
                        day[2] += completionTokens;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从数据库汇总用量失败: {}", e.getMessage());
        }
        result.put("dbCallCount", dbCallCount);
        result.put("dbPromptTokens", dbTotalPrompt);
        result.put("dbCompletionTokens", dbTotalCompletion);
        result.put("dbTotalTokens", dbTotalPrompt + dbTotalCompletion);

        // 近 7 天趋势同样从生成历史聚合，不再维护第二套文件计数。
        JSONArray dailyUsage = new JSONArray();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            int[] day = usageByDay.getOrDefault(date, new int[3]);
            JSONObject dayData = new JSONObject();
            dayData.put("date", dateStr);
            dayData.put("count", day[0]);
            dayData.put("promptTokens", day[1]);
            dayData.put("completionTokens", day[2]);
            dayData.put("totalTokens", day[1] + day[2]);
            dailyUsage.add(dayData);
        }
        result.put("dailyUsage", dailyUsage);

        // 3. 汇总
        int weekPrompt = 0;
        int weekCompletion = 0;
        int weekCount = 0;
        for (int i = 0; i < dailyUsage.size(); i++) {
            JSONObject d = dailyUsage.getJSONObject(i);
            weekCount += d.getIntValue("count");
            weekPrompt += d.getIntValue("promptTokens");
            weekCompletion += d.getIntValue("completionTokens");
        }
        result.put("weekCount", weekCount);
        result.put("weekPromptTokens", weekPrompt);
        result.put("weekCompletionTokens", weekCompletion);
        result.put("weekTotalTokens", weekPrompt + weekCompletion);

        result.put("estimatedCost", null);
        result.put("costEstimateStatus", "UNCONFIGURED");

        return Result.success(result);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiController.class);

    private static class WrongContext {
        private final String content;
        private final int totalCount;

        private WrongContext(String content, int totalCount) {
            this.content = content;
            this.totalCount = totalCount;
        }
    }

    private static class AiUsage {
        private int callCount;
        private int totalTokens;
        private Date lastCreateTime;
    }

    private static class AiLimitPolicy {
        private final String roleLabel;
        private final int dailyTokenLimit;
        private final int dailyCallLimit;
        private final int cooldownSeconds;
        private final int inputMaxChars;
        private final int reservedTokens;

        private AiLimitPolicy(String roleLabel, int dailyTokenLimit, int dailyCallLimit,
                              int cooldownSeconds, int inputMaxChars, int reservedTokens) {
            this.roleLabel = roleLabel;
            this.dailyTokenLimit = dailyTokenLimit;
            this.dailyCallLimit = dailyCallLimit;
            this.cooldownSeconds = cooldownSeconds;
            this.inputMaxChars = inputMaxChars;
            this.reservedTokens = reservedTokens;
        }
    }
}
