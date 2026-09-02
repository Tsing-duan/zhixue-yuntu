package com.ape.apeadmin.security;

import com.ape.apeframework.filter.JwtFilter;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JwtFilterHttpStatusTest {

    static Stream<AuthenticationException> authenticationFailures() {
        return Stream.of(
                new AuthenticationException("missing token"),
                new IncorrectCredentialsException("expired token"),
                new UnknownAccountException("unknown account"),
                new LockedAccountException("locked account"));
    }

    @ParameterizedTest
    @MethodSource("authenticationFailures")
    void authenticationFailuresReturnHttp401(AuthenticationException failure) throws Exception {
        TestJwtFilter filter = new TestJwtFilter(failure);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = filter.check(new MockHttpServletRequest(), response);

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    private static final class TestJwtFilter extends JwtFilter {
        private final AuthenticationException failure;

        private TestJwtFilter(AuthenticationException failure) {
            this.failure = failure;
        }

        @Override
        protected boolean executeLogin(ServletRequest request, ServletResponse response) {
            throw failure;
        }

        private boolean check(ServletRequest request, ServletResponse response) throws Exception {
            return isAccessAllowed(request, response, null);
        }
    }
}
