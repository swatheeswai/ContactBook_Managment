package com.viewClass;

import com.controllerClass.UserController;
import com.controllerClass.ContactController;
import com.modelClass.User;
import com.viewClass.ContactView;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.sql.SQLException;


public class UserView {
    private final Scanner scanner = new Scanner(System.in);
    private final UserController userController = new UserController();
    private final ContactController contactController = new ContactController(); // Create ContactController instance

    public void showUserView() {
        while (true) {
            System.out.println("\n📱 Phone Book Management System");
            System.out.println("1️⃣    Register User");
            System.out.println("2️⃣    Login");
            System.out.println("3️⃣    Logout");
            System.out.println("4️⃣    Exit");
            System.out.print("Enter choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                        if (userController.isUserLoggedIn()) {
                            logout();
                       } else {
                    System.out.println("⚠️ You need to log in first!");
                }
                break;
                case 4:
                    System.out.println("📴 Exiting...");
                    return;
                default:
                    System.out.println("❌ Invalid choice! Try again.");
            }
        }
    }

    public void register() {
       try{
        System.out.print("👤 Enter your Name: ");
        String name = scanner.nextLine();

        System.out.print("📧 Enter Your Email: ");
        String email = scanner.nextLine();

        System.out.print("📞 Enter Phone Number: ");
        long phone = scanner.nextLong();
        scanner.nextLine(); // Consume newline

        System.out.print("🔑 Enter Password: ");
        String password = scanner.nextLine();
         
         if(name.isEmpty() || email.isEmpty() || password.isEmpty()){
              throw new IllegalArgumentException("❌ All fields are required! Please fill them.");
              }

        User user = new User(name, email, phone, password);
        if (userController.registerUser(user)) {
            System.out.println("✅ Registration Successful!");
        } else {
            System.out.println("❌ Registration Failed. Try again later.");
        }
        }
        catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
            }
        catch(InputMismatchException e){
           System.out.println("❌ Invalid phone number! Please enter numbers only.");
           scanner.nextLine();
           }
        catch(Exception e){
           System.out.println("❌ error");
           }
        
        
    }

    public void login() {
        System.out.print("📧 Enter Your Email: ");
        String email = scanner.nextLine();

        System.out.print("🔑 Enter Password: ");
        String password = scanner.nextLine();

        int userId = userController.loginUser(email, password);
        if (userId != -1) {
            System.out.println("✅ Login Successful! User ID: " + userId);
            
            // ✅ Fixed: Pass `contactController` when creating `ContactView`
            ContactView contactView = new ContactView(contactController,userId);
            contactView.start();

        } else {
            System.out.println("❌ Login Failed! Incorrect email or password.");
        }
    }
    public void logout(){
        userController.logoutUser();
   }      
    
}

