package com.modelClass;

import java.io.File;

public class PersonalContact extends Contact {
    private String phone;
    private String emailId;

    public PersonalContact(File profilePicture, String firstName, String surname, String phone, String emailId) {
        super(profilePicture, firstName, surname);
        this.phone = phone;
        this.emailId = emailId;
    }

    public String getPhone() { 
        return phone; 
    }
    public void setPhone(String phone) {
        this.phone = phone; 
     }

    public String getEmailId() { 
        return emailId; 
    }
    public void setEmailId(String emailId) {
         this.emailId = emailId; 
     }

    @Override
    public String getContactType() {
        return "Personal";
    }

    @Override
    public String toString() {
        return super.toString() +
               "\n📞 Phone: " + phone +
               "\n📧 Email: " + emailId;
    }
}
