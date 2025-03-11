package com.modelClass;

import java.io.File;

public class BusinessContact extends Contact {
    private String phone;
    private String emailId;
    private String companyName;

    public BusinessContact(File profilePicture, String firstName, String surname, String phone, String emailId, String companyName) {
        super(profilePicture, firstName, surname);
        this.phone = phone;
        this.emailId = emailId;
        this.companyName = companyName;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    @Override
    public String getContactType() {
        return "Business";
    }

    @Override
    public String toString() {
        return super.toString() +
               "\n📞 Phone: " + phone +
               "\n📧 Email: " + emailId +
               "\n🏢 Company: " + companyName;
    }
}

