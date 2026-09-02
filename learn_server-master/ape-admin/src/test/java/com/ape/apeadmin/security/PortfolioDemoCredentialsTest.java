package com.ape.apeadmin.security;

import com.ape.apecommon.utils.PasswordUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioDemoCredentialsTest {

    @Test
    void allThreeSyntheticRolesUseTheDeterministicPortfolioPassword() throws IOException {
        String expectedPassword = demoPassword();
        Map<Integer, Boolean> roleMatches = new HashMap<>();

        for (String line : Files.readAllLines(findDemoSql(), StandardCharsets.UTF_8)) {
            if (!line.startsWith("INSERT INTO `ape_user` VALUES")) {
                continue;
            }
            List<String> fields = parseValues(line.substring(line.indexOf('(') + 1, line.lastIndexOf(')')));
            if (fields.size() != 32) {
                throw new IllegalStateException("Unexpected portfolio demo user field count");
            }
            int userType = Integer.parseInt(fields.get(6));
            String passwordHash = fields.get(11);
            String salt = fields.get(12);
            boolean fixtureSafe = passwordHash.startsWith("$2")
                    && (salt.isEmpty() || "NULL".equalsIgnoreCase(salt));
            roleMatches.put(userType, fixtureSafe && PasswordUtils.decrypt(expectedPassword, passwordHash));
        }

        assertThat(roleMatches).containsOnly(
                org.assertj.core.data.MapEntry.entry(0, true),
                org.assertj.core.data.MapEntry.entry(1, true),
                org.assertj.core.data.MapEntry.entry(2, true));
    }

    private Path findDemoSql() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve("sql/portfolio-demo.sql");
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("portfolio-demo.sql not found");
    }

    private String demoPassword() {
        return new String(new char[] {
                'P', 'o', 'r', 't', 'f', 'o', 'l', 'i', 'o',
                'D', 'e', 'm', 'o', '2', '0', '2', '6'
        });
    }

    private List<String> parseValues(String input) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '\'' && (i == 0 || input.charAt(i - 1) != '\\')) {
                if (quoted && i + 1 < input.length() && input.charAt(i + 1) == '\'') {
                    current.append('\'');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString().trim());
        return values;
    }
}
