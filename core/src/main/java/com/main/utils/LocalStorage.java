package com.main.utils;

public class LocalStorage {
    private static String username;
    private static String email;

    public static void setUser(String uname, String mail) {
        username = uname;
        email = mail;
    }

    public static String getUsername() {
        return username;
    }

    public static String getEmail() {
        return email;
    }
}
