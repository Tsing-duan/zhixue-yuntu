package com.ape.apeadmin.ai;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ape.apeadmin.controller.GlobalExceptionHandler;
import com.ape.apeadmin.controller.ai.AiController;
import com.ape.apeadmin.security.AiQuotaGuard;
import com.ape.apecommon.annotation.Log;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.assertj.core.api.Assertions.assertThat;

class AiSafetyBoundaryTest {
    @Test
    void unavailableQuotaStoreFailsClosedWithoutProvider() {
        AiQuotaGuard guard = new AiQuotaGuard(new StringRedisTemplate(), 120000, 5000);
        assertThat(guard.acquire("test-user",30,20000,15,6000,0,0).getError()).contains("暂时不可用");
    }

    @Test
    void aiExceptionNeverEchoesInputInResponseOrLog() {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> captured = new ListAppender<>();
        captured.start(); logger.addAppender(captured);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest("POST","/ai/study-plan")));
        try {
            assertThat(new GlobalExceptionHandler().handleRuntimeException(new IllegalArgumentException("private-input-canary")).getMessage())
                    .doesNotContain("private-input-canary").contains("检查输入");
            assertThat(captured.list).hasSize(1);
            assertThat(captured.list.get(0).getFormattedMessage()).contains("IllegalArgumentException").doesNotContain("private-input-canary");
            assertThat(captured.list.get(0).getThrowableProxy()).isNull();
        } finally { RequestContextHolder.resetRequestAttributes(); logger.detachAppender(captured); captured.stop(); }
    }

    @Test
    void aiAuditAnnotationsNeverSaveRequestParameters() {
        for (java.lang.reflect.Method method : AiController.class.getDeclaredMethods()) {
            Log log = method.getAnnotation(Log.class);
            if (log != null) assertThat(log.isSaveRequestData()).as(method.getName()).isFalse();
        }
    }
}
