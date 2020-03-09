package com.cloudogu.scm.teamscale.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XmlEncryptionAdapterTest {

  private final XmlEncryptionAdapter xmlEncryptionAdapter = new XmlEncryptionAdapter();

  @Test
  void shouldEncryptPassword() {
    String secret = "secret";

    String encryptedPassword = xmlEncryptionAdapter.marshal(secret);

    assertThat(secret).isNotEqualTo(encryptedPassword);
    assertThat(encryptedPassword).startsWith("{enc}");
  }

  @Test
  void shouldDecryptPassword() {
    String encrypted = "{enc}WIN8F-BXeJwsgfOxc8IJhwq53eMG3A==";

    String decrypted = xmlEncryptionAdapter.unmarshal(encrypted);

    assertThat(encrypted).isNotEqualTo(decrypted);
    assertThat(decrypted).doesNotStartWith("{enc}");
  }

}
