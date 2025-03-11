package com.controllerClass;

import com.database.DataBase;
import com.modelClass.Group;
import com.modelClass.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupController {

    private  Connection con;

    public GroupController() {
        con = DataBase.getInstance();
        createGroupTables();
    }

    // ✅ Create Group Tables
    private void createGroupTables() {
        String groupsTable = "CREATE TABLE IF NOT EXISTS groups ("
                + "group_id SERIAL PRIMARY KEY, "
                + "phoneuser_id INT  NOT NULL, "
                + "group_name VARCHAR(100) NOT NULL, "
                + "FOREIGN KEY (phoneuser_id) REFERENCES phoneusers(phoneuser_id) ON DELETE CASCADE"
                + ");";

       String groupMembersTable = "CREATE TABLE IF NOT EXISTS group_members ("
                + "member_id SERIAL PRIMARY KEY, "
                + "group_id INT NOT NULL, "
                + "contact_id INT NOT NULL, "
                + "FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE, "
                + "FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE, "
                + "CONSTRAINT unique_group_contact UNIQUE (group_id, contact_id)" // ✅ UNIQUE constraint added
                + ");";

        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(groupsTable);
            stmt.executeUpdate(groupMembersTable);
            System.out.println("✅ Group tables created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Ensure Connection Open
    private void ensureConnectionOpen() {
        try {
            if (con == null || con.isClosed()) {
                System.out.println("🔄 Reconnecting to the database...");
                con = DataBase.getInstance();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Add Group
   public int addGroup(Group group) {
    ensureConnectionOpen(); 
    String sql = "INSERT INTO groups (phoneuser_id, group_name) VALUES (?, ?) RETURNING group_id";
    
    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        pstmt.setInt(1, group.getUserId()); // ✅ Set parameters BEFORE executing
        pstmt.setString(2, group.getGroupName());

        try (ResultSet rs = pstmt.executeQuery()) { // ✅ Execute after setting parameters
            if (rs.next()) {
                int groupId = rs.getInt("group_id");
                System.out.println("✅ Group created with ID: " + groupId);
                return groupId;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}


    // ✅ Add Contact to Group (Only current user's contact allowed)
    public boolean addContactToGroup(int groupId, int contactId,int userId) {
        ensureConnectionOpen();
        String sql = "INSERT INTO group_members (group_id, contact_id) " +
                 "SELECT ?, ? " +
                 "WHERE EXISTS (SELECT 1 FROM contacts WHERE contact_id = ? AND phoneuser_id = ?)";
       try (PreparedStatement pstmt = con.prepareStatement(sql)) {
    pstmt.setInt(1, groupId);
    pstmt.setInt(2, contactId);
    pstmt.setInt(3, contactId);
    pstmt.setInt(4, userId);

    int rows = pstmt.executeUpdate();
    if (rows > 0) {
        System.out.println("✅ Contact added to group successfully!");
        return true;
    }
    } catch (SQLException e) {
           if (e.getSQLState().equals("23505")) { // ✅ UNIQUE constraint violation
               System.out.println("❌ Contact already exists in this group!");
           } else {
                 e.printStackTrace();
           }
     }
        return false;
    }
    

 public void displayGroups(int userId) {
    ensureConnectionOpen();

    // ✅ Use correct column names
    String sql = "SELECT g.group_id, g.group_name, " +
                 "COALESCE(c.first_name || ' ' || c.surname, c.first_name) AS contact_name, " +
                 "c.phone " +
                 "FROM groups g " +
                 "LEFT JOIN group_members gm ON g.group_id = gm.group_id " +
                 "LEFT JOIN contacts c ON gm.contact_id = c.contact_id " +
                 "WHERE g.phoneuser_id = ?";

    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("\n📜 Your Groups:");
        boolean hasGroups = false;
        while (rs.next()) {
            hasGroups = true;
            int groupId = rs.getInt("group_id");
            String groupName = rs.getString("group_name");
            String contactName = rs.getString("contact_name");
            String contactNumber = rs.getString("phone");

            // ✅ Display Group Details
            System.out.printf("\n🆔 Group ID: %d | 📛 Group Name: %s\n", groupId, groupName);

            if (contactName != null && contactNumber != null) {
                // ✅ Display Contact Details inside Group
                System.out.printf("➡️ Contact: %s | 📞 %s\n", contactName, contactNumber);
            } else {
                System.out.println("➡️ No Contacts in this Group.");
            }
        }

        if (!hasGroups) {
            System.out.println("❌ No groups found.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
  public boolean removeContactFromGroup(int contactId,int groupId){
          ensureConnectionOpen();
         String sql = "DELETE FROM group_members WHERE contact_id = ? AND group_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                  pstmt.setInt(1, contactId);
                  pstmt.setInt(2, groupId);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
            System.out.println("✅ Contact removed from group successfully!");
            return true;
        } else {
            System.out.println("❌ Contact not found in group.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
    // ✅ Update Contact in Group
public boolean updateContactFromGroup(int groupId, int oldContactId, int newContactId) {
    ensureConnectionOpen();

    String checkSql = "SELECT 1 FROM group_members WHERE group_id = ? AND contact_id = ?";
    try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
        checkStmt.setInt(1, groupId);
        checkStmt.setInt(2, oldContactId);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            // ✅ If record exists, update it
            String updateSql = "UPDATE group_members SET contact_id = ? WHERE group_id = ? AND contact_id = ?";
            try (PreparedStatement updateStmt = con.prepareStatement(updateSql)) {
                updateStmt.setInt(1, newContactId);
                updateStmt.setInt(2, groupId);
                updateStmt.setInt(3, oldContactId);

                int rows = updateStmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("✅ Contact updated in group successfully!");
                    return true;
                } else {
                    System.out.println("❌ Update Failed. No matching record found!");
                }
            }
        } else {
            System.out.println("❌ Update Failed. Contact or Group Not Found!");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

}

