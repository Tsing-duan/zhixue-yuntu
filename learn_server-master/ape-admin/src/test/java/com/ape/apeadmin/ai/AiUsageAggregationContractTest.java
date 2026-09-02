package com.ape.apeadmin.ai;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ape.apeadmin.controller.ai.AiController;
import com.ape.apecommon.constant.Constants;
import com.ape.apecommon.domain.Result;
import com.ape.apesystem.domain.ApeAiHistory;
import com.ape.apesystem.domain.ApeUser;
import com.ape.apesystem.service.ApeAiHistoryService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DelegatingSubject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiUsageAggregationContractTest {

    @AfterEach
    void tearDown() {
        ThreadContext.remove();
    }

    @Test
    void successfulHistoryIsTheOnlySourceForCallsTokensAndDailyTrend() {
        LocalDate today = LocalDate.now();
        ApeAiHistory first = history(today.minusDays(1), 12, 8);
        ApeAiHistory second = history(today, 7, 5);

        ApeAiHistoryService historyService = mock(ApeAiHistoryService.class);
        when(historyService.list()).thenReturn(Arrays.asList(first, second));

        AiController controller = new AiController();
        ReflectionTestUtils.setField(controller, "apeAiHistoryService", historyService);
        bindAdmin();

        Result response = controller.getAiUsage();
        assertThat(response.getCode()).isEqualTo(1000);
        JSONObject usage = (JSONObject) response.getData();
        assertThat(usage.getIntValue("dbCallCount")).isEqualTo(2);
        assertThat(usage.getIntValue("dbPromptTokens")).isEqualTo(19);
        assertThat(usage.getIntValue("dbCompletionTokens")).isEqualTo(13);
        assertThat(usage.getIntValue("dbTotalTokens")).isEqualTo(32);
        assertThat(usage.getIntValue("weekCount")).isEqualTo(2);
        assertThat(usage.getIntValue("weekPromptTokens")).isEqualTo(19);
        assertThat(usage.getIntValue("weekCompletionTokens")).isEqualTo(13);
        assertThat(usage.getIntValue("weekTotalTokens")).isEqualTo(32);
        assertThat(usage.containsKey("estimatedCost")).isTrue();
        assertThat(usage.get("estimatedCost")).isNull();
        assertThat(usage.getString("costEstimateStatus")).isEqualTo("UNCONFIGURED");

        JSONArray days = usage.getJSONArray("dailyUsage");
        assertThat(days).hasSize(7);
        assertThat(days.stream().map(JSONObject.class::cast)
                .mapToInt(day -> day.getIntValue("count")).sum()).isEqualTo(2);
        assertThat(days.getJSONObject(6).getString("date")).isEqualTo(today.toString());
        assertThat(days.getJSONObject(6).getIntValue("count")).isEqualTo(1);
    }

    private ApeAiHistory history(LocalDate date, int promptTokens, int completionTokens) {
        Date createdAt = Date.from(date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant());
        return new ApeAiHistory()
                .setId(date.toString())
                .setUserId("demo-user")
                .setAgentType("chat")
                .setAiReply("safe answer")
                .setPromptTokens(promptTokens)
                .setCompletionTokens(completionTokens)
                .setCreateTime(createdAt);
    }

    private void bindAdmin() {
        ApeUser admin = new ApeUser().setId("demo-admin")
                .setUserType(Constants.USER_TYPE_ADMIN).setLoginAccount("demo-admin");
        DefaultSecurityManager manager = new DefaultSecurityManager();
        SecurityUtils.setSecurityManager(manager);
        Subject subject = new DelegatingSubject(new SimplePrincipalCollection(admin, "test"),
                true, null, null, manager);
        ThreadContext.bind(subject);
    }
}
