package lab.store.auth;

public interface ProfileGateway {
    void create(String id, String name, String email);
}
