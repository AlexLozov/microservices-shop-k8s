package lab.store.auth;

public record Credential(String id, String passwordHash, String role) { }
