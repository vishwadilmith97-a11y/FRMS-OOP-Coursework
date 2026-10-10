package lk.ijse.fleet;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private final List<SystemUser> users = new ArrayList<>();

    public SystemUser registerUser(String username, String password, String fullName) {
        if (findByUsername(username) != null) {
            System.out.println("Username already taken");
            return null;
        }
        SystemUser user = new SystemUser(username, password, fullName);
        users.add(user);
        System.out.println("User registered successfully: " + username);
        return user;
    }

    public SystemUser login(String username, String password) {
        for (SystemUser u : users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public SystemUser findByUsername(String username) {
        for (SystemUser u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }
}
