package com.cinar.authentication.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserContextHolder {
    private static final ThreadLocal<UserDetails> userContext = new ThreadLocal<>();
    public static void setUser(UserDetails user){
        userContext.set(user);
    }
    public static UserDetails getUser(){
        return userContext.get();
    }
    public static void clearUser(){
        userContext.remove();
    }
}
