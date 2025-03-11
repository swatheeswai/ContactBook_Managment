package com.controllerClass;

import com.database.DataBase;
import com.modelClass.Favorite;

import java.sql.*;

public class FavoriteController {

    private Connection con;

    public FavoriteController() {
        con = DataBase.getInstance();
        createFavoriteTable();
    }

    // ✅ Create Favorite Table
    private void createFavoriteTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS favorites ("
                + "favorite_id SERIAL PRIMARY KEY, "
                + "phoneuser_id INT NOT NULL, "
                + "contact_id INT NOT NULL, "
                + "UNIQUE(phoneuser_id, contact_id), "
                + "FOREIGN KEY (phoneuser_id) REFERENCES phoneusers(phoneuser_id) ON DELETE CASCADE, "
                + "FOREIGN KEY (contact_id) REFERENCES contacts(contact_id) ON DELETE CASCADE"
                + ");";

        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("✅ Favorite table created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Add to Favorites (Only for Current User)
    public boolean addFavorite(int userId, int contactId) {
        // 🔥 Check if contact belongs to the current user
        String checkContactSql = "SELECT 1 FROM contacts WHERE contact_id = ? AND phoneuser_id = ?";
        try (PreparedStatement checkStmt = con.prepareStatement(checkContactSql)) {
            checkStmt.setInt(1, contactId);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // ✅ If contact belongs to user, add to favorites
                String insertFavoriteSql = "INSERT INTO favorites (phoneuser_id, contact_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = con.prepareStatement(insertFavoriteSql)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, contactId);
                    int rows = insertStmt.executeUpdate();
                    if (rows > 0) {
                        System.out.println("✅ Contact added to favorites!");
                        return true;
                    }
                } catch (SQLException e) {
                    if (e.getSQLState().equals("23505")) { // UNIQUE constraint violation
                        System.out.println("❌ Contact already in favorites!");
                    } else {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("❌ Contact doesn't belong to the current user!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Remove from Favorites (Only if contact belongs to current user)
    public boolean removeFavorite(int userId, int contactId) {
        String sql = "DELETE FROM favorites WHERE phoneuser_id = ? AND contact_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, contactId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Contact removed from favorites!");
                return true;
            } else {
                System.out.println("❌ Contact not found in favorites or doesn't belong to the user!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Display Favorites (Only for Current User)
    public void displayFavorites(int userId) {
        String sql = "SELECT c.contact_id, c.first_name, c.surname, c.phone, c.email " +
                     "FROM favorites f " +
                     "JOIN contacts c ON f.contact_id = c.contact_id " +
                     "WHERE f.phoneuser_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n⭐ Your Favorite Contacts:");
            boolean hasFavorites = false;
            while (rs.next()) {
                hasFavorites = true;
                int contactId = rs.getInt("contact_id");
                String firstName = rs.getString("first_name");
                String surname = rs.getString("surname");
                String phone = rs.getString("phone");
                String email = rs.getString("email");

                System.out.printf("🆔 %d | 👤 %s %s | 📞 %s | 📧 %s\n",
                        contactId, firstName, surname, phone, email);
            }

            if (!hasFavorites) {
                System.out.println("❌ No favorites found!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

