package iudx.data.ingestion.server.authenticator.authorization;

import java.util.stream.Stream;

public enum IUDXRole {
  CONSUMER("consumer"), PROVIDER("provider"), DELEGATE("delegate"), ADMIN("admin");

  private final String role;

  IUDXRole(String role) {
    this.role = role;
  }

  public String getRole() {
    return this.role;
  }

  public static IUDXRole fromRole(final String role) {
    return Stream.of(values())
        .filter(v -> v.role.equalsIgnoreCase(role))
        .findAny()
        .orElse(null);
  }

}
