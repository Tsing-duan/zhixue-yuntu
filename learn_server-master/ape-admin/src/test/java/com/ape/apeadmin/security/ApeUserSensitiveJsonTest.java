package com.ape.apeadmin.security;

import com.ape.apesystem.domain.ApeUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApeUserSensitiveJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void passwordAndSaltAreAbsentFromSerializedUser() throws Exception {
        ApeUser user = new ApeUser()
                .setId("demo-user")
                .setPassword("bcrypt-hash-sentinel")
                .setSalt("salt-sentinel");

        String json = objectMapper.writeValueAsString(user);

        assertThat(json).doesNotContain("password", "salt", "bcrypt-hash-sentinel", "salt-sentinel");
    }

    @Test
    void passwordIsWriteOnlyButSaltCannotBeDeserialized() throws Exception {
        ApeUser user = objectMapper.readValue(
                "{\"password\":\"new-password\",\"salt\":\"attacker-controlled-salt\"}",
                ApeUser.class);

        assertThat(user.getPassword()).isEqualTo("new-password");
        assertThat(user.getSalt()).isNull();
    }
}
