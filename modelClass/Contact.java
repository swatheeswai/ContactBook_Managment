package com.modelClass;

import java.io.File;

public abstract class Contact { // 🔥 Abstract Class
    
    private File profilePicture;
    private String firstName;
    private String surname;

    // Constructor
    public Contact(File profilePicture, String firstName, String surname) {
        this.profilePicture = profilePicture;
        this.firstName = firstName;
        this.surname = surname;
    }

    // ✅ Getters & Setters
    public File getProfilePicture() { 
        return profilePicture; 
    }
    public void setProfilePicture(File profilePicture) { 
        this.profilePicture = profilePicture; 
    }

    public String getFirstName() { 
        return firstName; 
    }
    public void setFirstName(String firstName) { 
        this.firstName = firstName; 
    }

    public String getSurname() { 
        return surname; 
    }
    public void setSurname(String surname) { 
        this.surname = surname; 
    }

    // 🔥 Abstract Method (To be implemented in child classes)
    public abstract String getContactType();

    @Override
    public String toString() {
        return "👤 Name: " + firstName + " " + surname +
               "\n📂 Profile Picture: " + (profilePicture != null ? profilePicture.getName() : "No Image") +
               "\n📝 Contact Type: " + getContactType();
    }
}

