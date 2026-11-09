package com.ape.apeadmin.ai;

import com.ape.apesystem.service.ai.DeepSeekClient;
import com.ape.apesystem.service.impl.AiServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.Locale;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class AiLanguageTest {
    @Test
    void outputLanguageChangesWithoutChangingReferenceMaterialOrRoleSafety() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        AiServiceImpl service = new AiServiceImpl(client);
        try {
            LocaleContextHolder.setLocale(Locale.ENGLISH);
            service.runAgent("student", "AI 自动学习规划", "My study goal", "用户原创内容");
            ArgumentCaptor<String> system = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
            verify(client).generate(system.capture(), payload.capture());
            assertThat(system.getValue()).contains("Response language: English", "学习规划", "输入边界");
            assertThat(payload.getValue()).contains("用户原创内容", "My study goal");
            reset(client);
            LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);
            service.chat("student", "A user question");
            verify(client).generate(contains("回答语言：简体中文"), eq("A user question"));
        } finally { LocaleContextHolder.resetLocaleContext(); }
    }
}
