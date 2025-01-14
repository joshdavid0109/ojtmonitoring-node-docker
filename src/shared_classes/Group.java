package shared_classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {
    private String name;
    private List<String> members;
    private User admin;

    public Group() {

    }

    public Group(String name) {
        this.name = name;
        members = new ArrayList<>();
    }

    public Group(String groupname, List<String> members) {
        this.name = groupname;
        this.members = members;
    }

    public Group(String groupname, List<String> members, User admin) {
        this.name = groupname;
        this.members = members;
        this.admin = admin;
    }

    public User getAdmin() {
        return admin;
    }

    public String getName() {
        return name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void addMember(String member) {
        members.add(member);
    }

    public void removeMember(String member) {
        members.remove(member);
    }

    public boolean containsMember(String member) {
        return members.contains(member);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
