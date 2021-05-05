package bgu.spl.net.api;

public interface User {

    public boolean login(String password);
    public boolean logout();
    public boolean isLoggedIn();

}
