package com.controllerClass;

import com.modelClass.Contact;
import com.modelClass.PersonalContact;
import com.modelClass.BusinessContact;
import com.database.DataBase;

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
                + "profile_picture BYTEA, "
                + "contact_type VARCHAR(50) NOT NULL, "
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

        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(contactsTable);
            stmt.executeUpdate(personalContactsTable);
            stmt.executeUpdate(businessContactsTable);
            System.out.println("✅ Tables created successfully!");
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
    public  int addContact(Contact contact, int userId) {
        ensureConnectionOpen();

        String insertContactSql = "INSERT INTO contacts (phoneuser_id, first_name, surname, phone, email, profile_picture, contact_type) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(insertContactSql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, contact.getFirstName());
            pstmt.setString(3, contact.getSurname());

            // ✅ Handle phone null values
            String phone = contact instanceof PersonalContact ?
                    ((PersonalContact) contact).getPhone() : ((BusinessContact) contact).getPhone();
            if (phone != null) {
                pstmt.setString(4, phone);
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            // ✅ Handle email null values
            String email = contact instanceof PersonalContact ?
                    ((PersonalContact) contact).getEmailId() : ((BusinessContact) contact).getEmailId();
            if (email != null) {
                pstmt.setString(5, email);
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            // ✅ Handle profile picture
            if (contact.getProfilePicture() != null) {
                try (FileInputStream fis = new FileInputStream(contact.getProfilePicture())) {
                    pstmt.setBinaryStream(6, fis, (int) contact.getProfilePicture().length());
                }
            } else {
                pstmt.setNull(6, Types.BINARY);
            }

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

                        System.out.println("✅ Contact Saved Successfully! Contact ID: " + contactId);
                        return contactId;
                    }
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ✅ Update Contact
    public boolean updateContact(int contactId, String firstName, String surname, String phone, String email, File profilePicture, String contactType, String companyName) {
        ensureConnectionOpen();

        StringBuilder query = new StringBuilder("UPDATE contacts SET ");
        boolean hasPrevious = false;

        if (firstName != null) {
            query.append("first_name = ?, ");
            hasPrevious = true;
        }
        if (surname != null) {
            query.append("surname = ?, ");
            hasPrevious = true;
        }
        if (phone != null) {
            query.append("phone = ?, ");
            hasPrevious = true;
        }
        if (email != null) {
            query.append("email = ?, ");
            hasPrevious = true;
        }
        if (profilePicture != null) {
            query.append("profile_picture = ?, ");
            hasPrevious = true;
        }
        if (contactType != null) {
            query.append("contact_type = ?, ");
            hasPrevious = true;
        }

        if (!hasPrevious) return false;

        query.delete(query.length() - 2, query.length());
        query.append(" WHERE contact_id = ?");

        try (PreparedStatement pstmt = con.prepareStatement(query.toString())) {
            int index = 1;
            if (firstName != null) pstmt.setString(index++, firstName);
            if (surname != null) pstmt.setString(index++, surname);
            if (phone != null) pstmt.setString(index++, phone);
            if (email != null) pstmt.setString(index++, email);
            if (profilePicture != null) {
                try (FileInputStream fis = new FileInputStream(profilePicture)) {
                    pstmt.setBinaryStream(index++, fis, (int) profilePicture.length());
                }
            }
            if (contactType != null) pstmt.setString(index++, contactType);

            pstmt.setInt(index, contactId);

            int updated = pstmt.executeUpdate();

            if (companyName != null) {
                String updateBusinessContact = "UPDATE business_contacts SET company_name = ? WHERE contact_id = ?";
                try (PreparedStatement businessStmt = con.prepareStatement(updateBusinessContact)) {
                    businessStmt.setString(1, companyName);
                    businessStmt.setInt(2, contactId);
                    businessStmt.executeUpdate();
                }
            }

            return updated > 0;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
 public void displayUserContacts(int userId) {
        ensureConnectionOpen();

        // ✅ Fixed column name in WHERE clause
        String sql = "SELECT contact_id, first_name, surname, phone, email, contact_type " +
                     "FROM contacts WHERE phoneuser_id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n📜 Your Contacts:");
            System.out.println("-------------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt("contact_id");
                String firstName = rs.getString("first_name");
                String surname = rs.getString("surname");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String contactType = rs.getString("contact_type");

                System.out.printf("🆔 %d | 👤 %s %s | 📞 %s | 📧 %s | 🔖 %s\n",
                        id, firstName, surname, phone, email, contactType);
            }
            System.out.println("-------------------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Error fetching contacts!");
        }
    }

// Delete Contact
 public boolean deleteContact(int contactId) {
        ensureConnectionOpen();
        String deleteQuery = "DELETE FROM contacts WHERE contact_id = ?";

        try (PreparedStatement pstmt = con.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, contactId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

