package com.viewClass;

import com.controllerClass.FavoriteController;

import java.util.Scanner;

public class FavoriteView {

    private final FavoriteController favoriteController;
    private final int userId;

    public FavoriteView(FavoriteController favoriteController, int userId) {
        this.favoriteController = favoriteController;
        this.userId = userId;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n⭐ Favorite Management:");
            System.out.println("1️⃣    Add to Favorites");
            System.out.println("2️⃣    Remove from Favorites");
            System.out.println("3️⃣    Display Favorites");
            System.out.println("4️⃣    Exit");
            System.out.print("➡ Enter choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    addToFavorites(scanner);
                    break;
                case 2:
                    removeFromFavorites(scanner);
                    break;
                case 3:
                    favoriteController.displayFavorites(userId);
                    break;
                case 4:
                    System.out.println("📴 Exiting favorite management.");
                    return;
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }

    // ✅ Add to Favorites
    private void addToFavorites(Scanner scanner) {
        System.out.print("\n🆔 Enter Contact ID to Add to Favorites: ");
        int contactId = scanner.nextInt();

        boolean success = favoriteController.addFavorite(userId, contactId);
        if (!success) {
            System.out.println("❌ Failed to add to favorites.");
        }
    }

    // ✅ Remove from Favorites
    private void removeFromFavorites(Scanner scanner) {
        System.out.print("\n🆔 Enter Contact ID to Remove from Favorites: ");
        int contactId = scanner.nextInt();

        boolean success = favoriteController.removeFavorite(userId, contactId);
        if (!success) {
            System.out.println("❌ Failed to remove from favorites.");
        }
    }
}

