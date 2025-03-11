package com.modelClass;

public class User {
    private String name;
    private String email;
    private long phoneNo;
    private String password;

    public User(String name, String email, long phoneNo, String password) {
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
        this.password = password;
    }

    public String getName() { 
    return name; 
    }
    public String getEmail() { 
    return email; 
    }
    public long getPhoneNo() { 
    return phoneNo; 
    }
    public String getPassword() { 
    return password; 
    }
}

