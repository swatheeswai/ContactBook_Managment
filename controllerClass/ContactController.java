package com.controllerClass;

import com.modelClass.Contact;
import com.modelClass.PersonalContact;
import com.modelClass.BusinessContact;
import com.database.DataBase;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.io.File;

public class ContactController {
    private  Connection con;

    public ContactController() {
        con = DataBase.getInstance(); // ✅ Fixed Database reference
        createTables();
    }

    // ✅ Create Tables
   public void createTables() {
    String contactsTable = "CREATE TABLE IF NOT EXISTS contacts ("
            + "contact_id SERIAL PRIMARY KEY, "
            + "phoneuser_id INT NOT NULL, "
            + "first_name VARCHAR(100) NOT NULL, "
            + "surname VARCHAR(100), "
            + "phone VARCHAR(20) UNIQUE, "
            + "email VARCHAR(100) UNIQUE, "
            + "profile_picture VARCHAR(255), " 
            + "contact_type VARCHAR(50) NOT NULL, "
            + "block_contact BOOLEAN DEFAULT FALSE, " 
            + "FOREIGN KEY (phoneuser_id) REFERENCES phoneusers(phoneuser_id) ON DELETE CASCADE"
            + ");";

    String personalContactsTable = "CREATE TABLE IF NOT EXISTS personal_contacts ("
            + "personal_id SERIAL PRIMARY KEY, "
            + "contact_id INT NOT NULL, "
            + "email_id VARCHAR(100), "
            + "FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE"
            + ");";

    String businessContactsTable = "CREATE TABLE IF NOT EXISTS business_contacts ("
            + "business_id SERIAL PRIMARY KEY, "
            + "contact_id INT NOT NULL, "
            + "company_name VARCHAR(100), "
            + "FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE"
            + ");";

    // ✅ New Sync Table
    String emailSyncTable = "CREATE TABLE IF NOT EXISTS email_sync ("
            + "sync_id SERIAL PRIMARY KEY, "
            + "contact_id INT UNIQUE NOT NULL, "
            + "first_name VARCHAR(100) NOT NULL, "
            + "surname VARCHAR(100), "
            + "phone VARCHAR(20), "
            + "email VARCHAR(100), "
            + "profile_picture VARCHAR(255), "
            + "contact_type VARCHAR(50), "
            + "FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE"
            + ");";

    try (Statement stmt = con.createStatement()) {
        stmt.executeUpdate(contactsTable);
        stmt.executeUpdate(personalContactsTable);
        stmt.executeUpdate(businessContactsTable);
        stmt.executeUpdate(emailSyncTable); // ✅ Sync table create logic added
       // System.out.println("✅ Tables created successfully!");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    // ✅ Ensure connection open
    private  void ensureConnectionOpen() {
        try {
            if (con == null || con.isClosed()) {
                System.out.println("🔄 Reconnecting to the database...");
                con = DataBase.getInstance();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 
  // ✅ Add Contact
public int addContact(Contact contact, int userId) {
    ensureConnectionOpen();

    // 🛠️ Create folder if not exists
    File folder = new File("/home/zoho/swathi2/PhoneBookApp/com/profile_pictures");
    if (!folder.exists()) {
        folder.mkdirs(); // ✅ Create folder if not exists
    }

    String filePath = null;
    if (contact.getProfilePicture() != null) {
        if (contact.getProfilePicture().exists()) { // ✅ Check if file exists
            try {
                filePath = "/home/zoho/swathi2/PhoneBookApp/com/profile_pictures/" +
                           System.currentTimeMillis() + "_" + contact.getProfilePicture().getName();
                File destFile = new File(filePath);
                Files.copy(contact.getProfilePicture().toPath(), destFile.toPath());
            } catch (IOException e) {
                System.out.println("❌ Error saving profile picture: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ Error: File not found at path - " + contact.getProfilePicture().getAbsolutePath());
        }
    }

    String insertContactSql = "INSERT INTO contacts (phoneuser_id, first_name, surname, phone, email, profile_picture, contact_type) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement pstmt = con.prepareStatement(insertContactSql, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setInt(1, userId);
        pstmt.setString(2, contact.getFirstName());
        pstmt.setString(3, contact.getSurname());
        pstmt.setString(4, contact instanceof PersonalContact ? 
                         ((PersonalContact) contact).getPhone() : 
                         ((BusinessContact) contact).getPhone());
        pstmt.setString(5, contact instanceof PersonalContact ? 
                         ( ((PersonalContact) contact).getEmailId().isEmpty() ? null : ((PersonalContact) contact).getEmailId()) : 
                         ( ((BusinessContact) contact).getEmailId().isEmpty() ? null : ((BusinessContact) contact).getEmailId()));
        pstmt.setString(6, filePath); // ✅ Store path instead of file
        pstmt.setString(7, contact.getContactType());

        int affectedRows = pstmt.executeUpdate();

        if (affectedRows > 0) {
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int contactId = generatedKeys.getInt(1);
                    
                    // ✅ Insert into personal or business table
                    if (contact instanceof PersonalContact) {
                        String insertPersonalContactSql = "INSERT INTO personal_contacts (contact_id, email_id) VALUES (?, ?)";
                        try (PreparedStatement personalStmt = con.prepareStatement(insertPersonalContactSql)) {
                            personalStmt.setInt(1, contactId);
                            personalStmt.setString(2, ((PersonalContact) contact).getEmailId());
                            personalStmt.executeUpdate();
                        }
                    } else if (contact instanceof BusinessContact) {
                        String insertBusinessContactSql = "INSERT INTO business_contacts (contact_id, company_name) VALUES (?, ?)";
                        try (PreparedStatement businessStmt = con.prepareStatement(insertBusinessContactSql)) {
                            businessStmt.setInt(1, contactId);
                            businessStmt.setString(2, ((BusinessContact) contact).getCompanyName());
                            businessStmt.executeUpdate();
                        }
                    }
                    System.out.println("-----------------------------------------------------------");
                    System.out.println("✅ Contact Saved Successfully! Contact ID: " + contactId);
                    System.out.println("-----------------------------------------------------------");
                    return contactId;
                }
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}

    // ✅ Update Contact
public boolean updateContact(int contactId, int userId, String firstName, String surname, String phone, String email, File profilePicture, String contactType, String companyName) {
    ensureConnectionOpen();

    // ✅ Step 1: Check if contact exists for the current user
    String checkQuery = "SELECT contact_id FROM contacts WHERE contact_id = ? AND phoneuser_id = ?";
    try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {
        checkStmt.setInt(1, contactId);
        checkStmt.setInt(2, userId);
        try (ResultSet rs = checkStmt.executeQuery()) {
            if (!rs.next()) { 
                System.out.println("❌ Update Failed! Contact Not Found for Current User.");
                return false; // 🚨 Contact does not exist for this user
            }
        }
    } catch (SQLException e) {
        System.out.println("❌ Error checking contact: " + e.getMessage());
        e.printStackTrace();
        return false;
    }

    StringBuilder query = new StringBuilder("UPDATE contacts SET ");
    boolean hasPrevious = false;
    String filePath = null;

    if (profilePicture != null) {
        try {
            // ✅ Step 2: Old Profile Picture Remove
            String selectQuery = "SELECT profile_picture FROM contacts WHERE contact_id = ?";
            try (PreparedStatement selectStmt = con.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, contactId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        String oldFilePath = rs.getString("profile_picture");
                        if (oldFilePath != null) {
                            File oldFile = new File(oldFilePath);
                            if (oldFile.exists()) {
                                oldFile.delete(); // ✅ Old file delete
                            }
                        }
                    }
                }
            }

            // ✅ Step 3: Save New Profile Picture
            filePath = "com/profile_pictures/" + System.currentTimeMillis() + "_" + profilePicture.getName();
            File destFile = new File(filePath);
            Files.copy(profilePicture.toPath(), destFile.toPath());

            query.append("profile_picture = ?, ");
            hasPrevious = true;

        } catch (IOException | SQLException e) {
            System.out.println("❌ Error saving new profile picture: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Step 4: Update Other Fields
    if (firstName != null && !firstName.trim().isEmpty()) {
        query.append("first_name = ?, ");
        hasPrevious = true;
    }
    if (surname != null && !surname.trim().isEmpty()) {
        query.append("surname = ?, ");
        hasPrevious = true;
    }
    if (phone != null && !phone.trim().isEmpty()) {
        query.append("phone = ?, ");
        hasPrevious = true;
    }
    if (email != null && !email.trim().isEmpty()) {
        query.append("email = ?, ");
        hasPrevious = true;
    }
    if (contactType != null && !contactType.trim().isEmpty()) {
        query.append("contact_type = ?, ");
        hasPrevious = true;
    }

    if (!hasPrevious) {
        System.out.println("⚠️ No Changes Provided. Nothing to Update.");
        return false;
    }

    // ✅ Step 5: Finalize Query
    query.delete(query.length() - 2, query.length());
    
    // ✅ FIXED HERE — Added phoneuser_id check
    query.append(" WHERE contact_id = ? AND phoneuser_id = ?");

    try (PreparedStatement pstmt = con.prepareStatement(query.toString())) {
        int index = 1;
        if (firstName != null && !firstName.trim().isEmpty()) pstmt.setString(index++, firstName);
        if (surname != null && !surname.trim().isEmpty()) pstmt.setString(index++, surname);
        if (phone != null && !phone.trim().isEmpty()) pstmt.setString(index++, phone);
        if (email != null && !email.trim().isEmpty()) pstmt.setString(index++, email);
        if (filePath != null) pstmt.setString(index++, filePath);
        if (contactType != null && !contactType.trim().isEmpty()) pstmt.setString(index++, contactType);

        // ✅ Added phoneuser_id check
        pstmt.setInt(index++, contactId);
        pstmt.setInt(index, userId);

        int updated = pstmt.executeUpdate();

        // ✅ Step 6: Business Contact Update
        if (companyName != null && !companyName.trim().isEmpty()) {
            String updateBusinessContact = "UPDATE business_contacts SET company_name = ? WHERE contact_id = ? AND EXISTS (SELECT 1 FROM contacts WHERE contact_id = ? AND phoneuser_id = ?)";
            try (PreparedStatement businessStmt = con.prepareStatement(updateBusinessContact)) {
                businessStmt.setString(1, companyName);
                businessStmt.setInt(2, contactId);
                businessStmt.setInt(3, contactId);
                businessStmt.setInt(4, userId);
                businessStmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("❌ Error updating business contact: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (updated > 0) {
            
            return true;
        } else {
            System.out.println("❌ Update Failed! Contact Not Found.");
            return false;
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}



// ✅ Display Contacts
public void displayUserContacts(int userId) {
    ensureConnectionOpen();

    String sql = "SELECT contact_id, first_name, surname, phone, email, profile_picture, contact_type " +
                 "FROM contacts WHERE phoneuser_id = ? AND block_contact = FALSE"; // ✅ Ignore blocked contacts

    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("\n📜 Your Contacts:");
        System.out.println("-------------------------------------------------");
        boolean hasContacts = false;

        while (rs.next()) {
            hasContacts = true;
            int id = rs.getInt("contact_id");
            String firstName = rs.getString("first_name");
            String surname = rs.getString("surname");
            String phone = rs.getString("phone");
            String email = rs.getString("email");
            String contactType = rs.getString("contact_type");
            String profilePicture = rs.getString("profile_picture");

            System.out.printf("🆔 %d | 👤 %s %s | 📞 %s | 📧 %s | 🔖 %s\n",
                    id, firstName, surname, phone, email, contactType);

            if (profilePicture != null) {
                System.out.println("🖼️ Profile Picture: " + profilePicture);
            }
        }

        if (!hasContacts) {
            System.out.println("⚠️ No contacts found.");
        }

        System.out.println("-------------------------------------------------");
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("❌ Error fetching contacts!");
    }
}



// Delete Contact
public boolean deleteContact(int contactId, int userId) {
    ensureConnectionOpen();
    String deleteQuery = "DELETE FROM contacts WHERE contact_id = ? AND phoneuser_id = ?"; // ✅ Match user and contact

    try (PreparedStatement pstmt = con.prepareStatement(deleteQuery)) {
        pstmt.setInt(1, contactId);
        pstmt.setInt(2, userId); // ✅ Ensure it's current user's contact
        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

    
 public void syncContactsToEmail(int userId) {
    String syncSql = "INSERT INTO email_sync (contact_id, first_name, surname, phone, email, profile_picture, contact_type) " +
                     "SELECT contact_id, first_name, surname, phone, email, profile_picture, contact_type " +
                     "FROM contacts WHERE phoneuser_id = ? " + // ✅ Only current user contacts
                     "ON CONFLICT (contact_id) DO UPDATE " +
                     "SET first_name = EXCLUDED.first_name, " +
                     "    surname = EXCLUDED.surname, " +
                     "    phone = EXCLUDED.phone, " +
                     "    email = EXCLUDED.email, " +
                     "    profile_picture = EXCLUDED.profile_picture, " +
                     "    contact_type = EXCLUDED.contact_type";

    try (PreparedStatement pstmt = con.prepareStatement(syncSql)) {
        pstmt.setInt(1, userId); 
        int rowsAffected = pstmt.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("✅ Contacts synced to email_sync table successfully!");
        } else {
            System.out.println("⚠️ No new contacts to sync.");
        }
    } catch (SQLException e) {
        System.out.println("❌ Error syncing contacts: " + e.getMessage());
        e.printStackTrace();
    }
}
public void searchContacts(String searchTerm, int userId) {
    String searchQuery = "SELECT * FROM contacts " +
                         "WHERE phoneuser_id = ? AND " +
                         "(LOWER(first_name) ILIKE ? OR " +
                         "LOWER(surname) ILIKE ? OR " +
                         "phone ILIKE ? OR " +
                         "email ILIKE ?)";

    try (PreparedStatement pstmt = con.prepareStatement(searchQuery)) {
        pstmt.setInt(1, userId);
        String searchPattern = "%" + searchTerm.toLowerCase() + "%";
        pstmt.setString(2, searchPattern);
        pstmt.setString(3, searchPattern);
        pstmt.setString(4, searchPattern);
        pstmt.setString(5, searchPattern);

        ResultSet rs = pstmt.executeQuery();

        System.out.println("\n🔍 Search Results:");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("------------------------------------------------");
            System.out.println("📌 Contact ID   : " + rs.getInt("contact_id"));
            System.out.println("👤 First Name   : " + rs.getString("first_name"));
            System.out.println("👤 Surname      : " + rs.getString("surname"));
            System.out.println("📞 Phone        : " + rs.getString("phone"));
            System.out.println("📧 Email        : " + rs.getString("email"));
            System.out.println("📂 Profile Pic  : " + rs.getString("profile_picture"));
            System.out.println("🔖 Type         : " + rs.getString("contact_type"));
            System.out.println("------------------------------------------------");
        }
        if (!found) {
            System.out.println("⚠️ No contacts found matching: " + searchTerm);
        }
    } catch (SQLException e) {
        System.out.println("❌ Error searching contacts: " + e.getMessage());
        e.printStackTrace();
    }
}
public void blockContact(int contactId, int userId) {
    // ✅ Check if contact exists for current user
    String checkQuery = "SELECT contact_id FROM contacts WHERE contact_id = ? AND phoneuser_id = ?";

    try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {
        checkStmt.setInt(1, contactId);
        checkStmt.setInt(2, userId);
        try (ResultSet rs = checkStmt.executeQuery()) {
            if (!rs.next()) {
                System.out.println("❌ Block Failed! Contact Not Found for Current User.");
                return;
            }
        }
    } catch (SQLException e) {
        System.out.println("❌ Error checking contact: " + e.getMessage());
        e.printStackTrace();
        return;
    }

   String sql = "UPDATE contacts SET block_contact = TRUE WHERE contact_id = ? AND phoneuser_id = ?";


    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        pstmt.setInt(1, contactId);
        pstmt.setInt(2, userId);
        int rowsAffected = pstmt.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("✅ Contact blocked successfully!");
        } else {
            System.out.println("⚠️ No contact found with the given ID.");
        }
    } catch (SQLException e) {
        System.out.println("❌ Error blocking contact: " + e.getMessage());
        e.printStackTrace();
    }
}
public void unBlockContact(int contactId, int userId) {
    // ✅ Check if contact exists for current user
    String checkQuery = "SELECT contact_id FROM contacts WHERE contact_id = ? AND phoneuser_id = ?";

    try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {
        checkStmt.setInt(1, contactId);
        checkStmt.setInt(2, userId);
        try (ResultSet rs = checkStmt.executeQuery()) {
            if (!rs.next()) {
                System.out.println("❌ Block Failed! Contact Not Found for Current User.");
                return;
            }
        }
    } catch (SQLException e) {
        System.out.println("❌ Error checking contact: " + e.getMessage());
        e.printStackTrace();
        return;
    }

   String sql = "UPDATE contacts SET block_contact = FALSE WHERE contact_id = ? AND phoneuser_id = ?";


    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        pstmt.setInt(1, contactId);
        pstmt.setInt(2, userId);
        int rowsAffected = pstmt.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("✅ Contact UnBlocked successfully!");
        } else {
            System.out.println("⚠️ No contact found with the given ID.");
        }
    } catch (SQLException e) {
        System.out.println("❌ Error blocking contact: " + e.getMessage());
        e.printStackTrace();
    }
}
public void displayBlockedContacts(int userId) {
    String sql = "SELECT * FROM contacts WHERE phoneuser_id = ? AND block_contact = TRUE";

    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("\n🚫 Blocked Contacts:");
        boolean hasBlockedContacts = false;
        while (rs.next()) {
            hasBlockedContacts = true;
            System.out.println("👤 Name: " + rs.getString("first_name") + " " + rs.getString("surname"));
            System.out.println("📞 Phone: " + rs.getString("phone"));
            System.out.println("📧 Email: " + rs.getString("email"));
            System.out.println("-----------------------");
        }

        if (!hasBlockedContacts) {
            System.out.println("⚠️ No blocked contacts found.");
        }

    } catch (SQLException e) {
        System.out.println("❌ Error displaying blocked contacts: " + e.getMessage());
        e.printStackTrace();
    }
}


         

}

