package bgu.spl.net.impl.BGRSServer.staff;

import bgu.spl.net.api.User;

public class Admin implements User {

    private boolean status = false;
    private final String username;
    private final String password;
    private final Object loginLock = new Object();

    public Admin(String username,String password){
        this.username = username;
        this.password = password;
        status = false;
    }

    @Override
    public boolean login(String password) {
        synchronized (loginLock) {
            if (!isLoggedIn() & this.password.equals(password)) {
                status = true;
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean logout() {
        if(isLoggedIn()){
            status = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean isLoggedIn() {
        return status;
    }
}
