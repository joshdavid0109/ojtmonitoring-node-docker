package shared_classes;

public record User(String id, String name, String age, String username, String password, String status, String banStatus) {

    @Override
    public String toString() {
        return "User: " + name + "\nAge: " + age + "\nUsername: " + username;
    }
}
