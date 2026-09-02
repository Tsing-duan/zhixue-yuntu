package com.ape.apeadmin.ai;

import com.ape.apeadmin.controller.ai.AiController;
import com.ape.apecommon.constant.Constants;
import com.ape.apesystem.domain.ApeAiHistory;
import com.ape.apesystem.domain.ApeChapter;
import com.ape.apesystem.domain.ApeChapterVideo;
import com.ape.apesystem.domain.ApeHomework;
import com.ape.apesystem.domain.ApeHomeworkStudent;
import com.ape.apesystem.domain.ApeLoginLog;
import com.ape.apesystem.domain.ApeTask;
import com.ape.apesystem.domain.ApeTaskStudent;
import com.ape.apesystem.domain.ApeTest;
import com.ape.apesystem.domain.ApeTestItem;
import com.ape.apesystem.domain.ApeTestStudent;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeAiHistoryService;
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
import com.ape.apesystem.service.ai.DeepSeekClient;
import com.ape.apesystem.service.impl.AiServiceImpl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DelegatingSubject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiGenerationEndpointContractTest {

    private HttpServer server;
    private AtomicInteger providerAttempts;
    private AtomicReference<Integer> providerStatus;
    private AtomicReference<String> providerBody;
    private AtomicReference<String> lastProviderRequest;
    private MockMvc mockMvc;
    private ApeAiHistoryService historyService;
    private ApeTaskService taskService;
    private ApeTaskStudentService taskStudentService;
    private AtomicLong historyIds;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws IOException {
        initializeLambdaMetadata();
        Map<String, Long> requestGuard = (Map<String, Long>) ReflectionTestUtils.getField(AiController.class, "AI_REQUEST_GUARD");
        if (requestGuard != null) {
            requestGuard.clear();
        }

        providerAttempts = new AtomicInteger();
        providerStatus = new AtomicReference<>(200);
        providerBody = new AtomicReference<>(successBody());
        lastProviderRequest = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", this::respond);
        server.start();

        DeepSeekClient client = new DeepSeekClient(
                "test-only-key", "http://127.0.0.1:" + server.getAddress().getPort(),
                "deepseek-v4-flash", "high", 4096, 5000, 1000, "test");
        AiController controller = new AiController();
        ReflectionTestUtils.setField(controller, "aiService", new AiServiceImpl(client));

        historyService = mock(ApeAiHistoryService.class);
        historyIds = new AtomicLong(100);
        when(historyService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        when(historyService.save(any(ApeAiHistory.class))).thenAnswer(invocation -> {
            ApeAiHistory history = invocation.getArgument(0);
            history.setId(String.valueOf(historyIds.incrementAndGet()));
            if (history.getCreateTime() == null) history.setCreateTime(new Date());
            return true;
        });
        ReflectionTestUtils.setField(controller, "apeAiHistoryService", historyService);

        taskService = mock(ApeTaskService.class);
        when(taskService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(controller, "apeTaskService", taskService);

        taskStudentService = mock(ApeTaskStudentService.class);
        when(taskStudentService.count(any(QueryWrapper.class))).thenReturn(1);
        when(taskStudentService.list(any(QueryWrapper.class))).thenReturn(
                Collections.singletonList(new ApeTaskStudent().setTaskId("course-1").setUserId("student" ).setState(0)));
        ReflectionTestUtils.setField(controller, "apeTaskStudentService", taskStudentService);

        ApeChapterService chapterService = mock(ApeChapterService.class);
        when(chapterService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        when(chapterService.getById(any())).thenReturn(new ApeChapter().setId("chapter-1").setTaskId("course-1")
                .setTaskName("Portfolio Demo Course").setName("Demo Chapter"));
        ReflectionTestUtils.setField(controller, "apeChapterService", chapterService);

        ApeChapterVideoService chapterVideoService = mock(ApeChapterVideoService.class);
        when(chapterVideoService.count(any(QueryWrapper.class))).thenReturn(0);
        ReflectionTestUtils.setField(controller, "apeChapterVideoService", chapterVideoService);

        ApeHomeworkService homeworkService = mock(ApeHomeworkService.class);
        when(homeworkService.count(any(QueryWrapper.class))).thenReturn(0);
        when(homeworkService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(controller, "apeHomeworkService", homeworkService);

        ApeHomeworkStudentService homeworkStudentService = mock(ApeHomeworkStudentService.class);
        when(homeworkStudentService.count(any(QueryWrapper.class))).thenReturn(0);
        when(homeworkStudentService.list(any(QueryWrapper.class))).thenReturn(Collections.singletonList(
                new ApeHomeworkStudent().setId("hw-1").setUserId("student").setChapterId("chapter-1")
                        .setTitle("Demo Homework Question").setType(0).setAnswer("A").setSolution("B")
                        .setPoint(0).setScore(5)));
        ReflectionTestUtils.setField(controller, "apeHomeworkStudentService", homeworkStudentService);

        ApeTestStudentService testStudentService = mock(ApeTestStudentService.class);
        when(testStudentService.count(any(QueryWrapper.class))).thenReturn(0);
        when(testStudentService.list(any(QueryWrapper.class))).thenReturn(Collections.singletonList(
                new ApeTestStudent().setId("test-answer-1").setUserId("student").setTestId("test-1")
                        .setTestName("Portfolio Demo Exam").setTitle("Demo Exam Question")
                        .setType(0).setAnswer("A").setSolution("B").setPoint(0).setScore(5)));
        ReflectionTestUtils.setField(controller, "apeTestStudentService", testStudentService);

        ApeTestService testService = mock(ApeTestService.class);
        when(testService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(controller, "apeTestService", testService);

        ApeTestItemService testItemService = mock(ApeTestItemService.class);
        when(testItemService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        ReflectionTestUtils.setField(controller, "apeTestItemService", testItemService);

        ApeUserService userService = mock(ApeUserService.class);
        when(userService.count()).thenReturn(0);
        when(userService.count(any(QueryWrapper.class))).thenReturn(0);
        ReflectionTestUtils.setField(controller, "apeUserService", userService);

        ApeLoginLogService loginLogService = mock(ApeLoginLogService.class);
        when(loginLogService.count(any(QueryWrapper.class))).thenReturn(0);
        ReflectionTestUtils.setField(controller, "apeLoginLogService", loginLogService);
        ApeArticleService articleService = mock(ApeArticleService.class);
        when(articleService.count()).thenReturn(0);
        ReflectionTestUtils.setField(controller, "apeArticleService", articleService);
        ApeForumService forumService = mock(ApeForumService.class);
        when(forumService.count()).thenReturn(0);
        ReflectionTestUtils.setField(controller, "apeForumService", forumService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private void initializeLambdaMetadata() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        Class<?>[] entities = {
                ApeAiHistory.class, ApeUser.class, ApeLoginLog.class, ApeTask.class,
                ApeTaskStudent.class, ApeChapter.class, ApeChapterVideo.class,
                ApeHomework.class, ApeHomeworkStudent.class, ApeTest.class,
                ApeTestItem.class, ApeTestStudent.class
        };
        for (Class<?> entity : entities) {
            if (TableInfoHelper.getTableInfo(entity) == null) {
                MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "ai-contract-test");
                assistant.setCurrentNamespace(entity.getName());
                TableInfoHelper.initTableInfo(assistant, entity);
            }
        }
    }

    @AfterEach
    void tearDown() {
        ThreadContext.remove();
        if (server != null) server.stop(0);
    }

    @ParameterizedTest(name = "{0} success persists one owned history")
    @MethodSource("generationEndpoints")
    void eachGenerationEndpointUsesProviderAndPersistsOnlyFinalAnswer(
            String name, String path, String requestJson, int userType, String userId, String expectedAgentType) throws Exception {
        prepareOwnedCourse(userId, userType);
        bindUser(userId, userType);

        mockMvc.perform(post(path).contentType("application/json").content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.reply").value("DeepSeek final answer"))
                .andExpect(jsonPath("$.data.historyId").isNotEmpty())
                .andExpect(jsonPath("$.data.createTime").isNotEmpty())
                .andExpect(jsonPath("$.data.reasoning_content").doesNotExist());

        assertThat(providerAttempts.get()).isEqualTo(1);
        assertThat(lastProviderRequest.get()).contains("deepseek-v4-flash").doesNotContain("password", "salt", "reasoning_content");
        assertRolePromptIsolation(lastProviderRequest.get(), userType);
        ArgumentCaptor<ApeAiHistory> history = ArgumentCaptor.forClass(ApeAiHistory.class);
        verify(historyService).save(history.capture());
        assertThat(history.getValue().getUserId()).isEqualTo(userId);
        assertThat(history.getValue().getAgentType()).isEqualTo(expectedAgentType);
        assertThat(history.getValue().getAiReply()).isEqualTo("DeepSeek final answer");
        assertThat(history.getValue().getPromptTokens()).isEqualTo(12);
        assertThat(history.getValue().getCompletionTokens()).isEqualTo(8);
    }

    @ParameterizedTest(name = "{0} provider failure never persists history")
    @MethodSource("generationEndpoints")
    void eachGenerationEndpointFailsClosedWithoutHistory(
            String name, String path, String requestJson, int userType, String userId, String expectedAgentType) throws Exception {
        providerStatus.set(503);
        providerBody.set("{\"error\":{\"message\":\"private provider detail\"}}");
        prepareOwnedCourse(userId, userType);
        bindUser(userId, userType);

        mockMvc.perform(post(path).contentType("application/json").content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value("AI 服务资源暂时不足，请稍后再试。"));

        assertThat(providerAttempts.get()).isEqualTo(1);
        verify(historyService, never()).save(any(ApeAiHistory.class));
    }

    @Test
    void cooldownRejectsSecondRequestBeforeProviderAndHistory() throws Exception {
        prepareOwnedCourse("student-cooldown", Constants.USER_TYPE_STUDENT);
        bindUser("student-cooldown", Constants.USER_TYPE_STUDENT);

        mockMvc.perform(post("/ai/chat").contentType("application/json")
                        .content("{\"message\":\"请解释课程学习重点\"}"))
                .andExpect(jsonPath("$.code").value(1000));
        mockMvc.perform(post("/ai/chat").contentType("application/json")
                        .content("{\"message\":\"请继续解释课程学习重点\"}"))
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("请求过于频繁")));

        assertThat(providerAttempts.get()).isEqualTo(1);
        verify(historyService).save(any(ApeAiHistory.class));
    }

    @Test
    void teacherCannotSendForeignCourseToProvider() throws Exception {
        when(taskService.getById("course-1")).thenReturn(ownedCourse("other-teacher"));
        bindUser("teacher-owner", Constants.USER_TYPE_TEACHER);

        mockMvc.perform(post("/ai/course-summary").contentType("application/json")
                        .content("{\"taskId\":\"course-1\"}"))
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value("课程不存在或无权访问"));

        assertThat(providerAttempts.get()).isZero();
        verify(historyService, never()).save(any(ApeAiHistory.class));
    }

    static Stream<Arguments> generationEndpoints() {
        return Stream.of(
                Arguments.of("chat", "/ai/chat", "{\"message\":\"请解释课程学习重点\"}",
                        Constants.USER_TYPE_STUDENT, "student-chat", "chat"),
                Arguments.of("study-plan", "/ai/study-plan", "{\"taskId\":\"course-1\"}",
                        Constants.USER_TYPE_STUDENT, "student-study", "study-plan"),
                Arguments.of("wrong-agent", "/ai/wrong-agent", "{\"limit\":3}",
                        Constants.USER_TYPE_STUDENT, "student-wrong", "wrong-agent"),
                Arguments.of("lesson-prepare", "/ai/lesson-prepare", "{\"taskId\":\"course-1\"}",
                        Constants.USER_TYPE_TEACHER, "teacher-lesson", "lesson-prepare"),
                Arguments.of("course-summary", "/ai/course-summary", "{\"taskId\":\"course-1\"}",
                        Constants.USER_TYPE_TEACHER, "teacher-summary", "course-summary"),
                Arguments.of("data-agent", "/ai/data-agent", "{\"agentType\":\"course-health\",\"taskId\":\"course-1\"}",
                        Constants.USER_TYPE_TEACHER, "teacher-data", "course-health"));
    }

    private void prepareOwnedCourse(String userId, int userType) {
        String teacherId = userType == Constants.USER_TYPE_TEACHER ? userId : "demo-teacher";
        when(taskService.getById("course-1")).thenReturn(ownedCourse(teacherId));
        when(taskStudentService.list(any(QueryWrapper.class))).thenReturn(Collections.singletonList(
                new ApeTaskStudent().setTaskId("course-1").setUserId(userId).setState(0)));
    }

    private ApeTask ownedCourse(String teacherId) {
        return new ApeTask().setId("course-1").setName("Portfolio Demo Course")
                .setTeacherId(teacherId).setTeacherName("Demo Teacher")
                .setMajor("Software Engineering").setClassification("Demo")
                .setTaskDescribe("A portfolio-safe course fixture");
    }

    private void assertRolePromptIsolation(String requestBody, int userType) {
        if (userType == Constants.USER_TYPE_TEACHER) {
            assertThat(requestBody).contains("面向教师").doesNotContain("面向学生", "面向管理员");
            return;
        }
        if (userType == Constants.USER_TYPE_ADMIN) {
            assertThat(requestBody).contains("面向管理员").doesNotContain("面向学生", "面向教师");
            return;
        }
        assertThat(requestBody).contains("面向学生").doesNotContain("面向教师", "面向管理员");
    }

    private void bindUser(String id, int userType) {
        ThreadContext.remove();
        ApeUser current = new ApeUser().setId(id).setUserType(userType)
                .setLoginAccount(id).setUserName(id).setStatus(0);
        DefaultSecurityManager manager = new DefaultSecurityManager();
        SecurityUtils.setSecurityManager(manager);
        Subject subject = new DelegatingSubject(new SimplePrincipalCollection(current, "test"),
                true, null, null, manager);
        ThreadContext.bind(subject);
    }

    private void respond(HttpExchange exchange) throws IOException {
        providerAttempts.incrementAndGet();
        lastProviderRequest.set(readAll(exchange.getRequestBody()));
        byte[] bytes = providerBody.get().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(providerStatus.get(), bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String readAll(InputStream input) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuilder text = new StringBuilder();
        int count;
        while ((count = input.read(buffer)) != -1) {
            text.append(new String(buffer, 0, count, StandardCharsets.UTF_8));
        }
        return text.toString();
    }

    private String successBody() {
        return "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{"
                + "\"content\":\"DeepSeek final answer\",\"reasoning_content\":\"private reasoning\"}}],"
                + "\"usage\":{\"prompt_tokens\":12,\"completion_tokens\":8,\"total_tokens\":20}}";
    }
}
