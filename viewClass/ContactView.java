package com.viewClass;

import com.controllerClass.ContactController;
import com.controllerClass.GroupController;
import com.controllerClass.FavoriteController;
import com.modelClass.PersonalContact;
import com.modelClass.BusinessContact;
import com.viewClass.GroupView;
import com.viewClass.FavoriteView;


import java.io.File;
import java.util.Scanner;
import java.util.InputMismatchException;
public class ContactView {
    private final ContactController contactController;
    private final GroupView groupview;
    private final FavoriteView favoriteview;
    private final int userId ;

    // Constructor takes ContactController as a parameter
    public ContactView(ContactController contactController,int userId) {
        this.contactController = contactController;
        this.userId = userId;
        GroupController groupController = new GroupController();
        this.groupview = new GroupView(groupController,userId);
        FavoriteController favoriteController = new FavoriteController();
        this.favoriteview = new FavoriteView(favoriteController,userId);
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("📞 Welcome to the Contact Management System!");

        while (true) {
            System.out.println("\n🔹 Select an option:"); 
            System.out.println("1️⃣    Add Personal Contact");
            System.out.println("2️⃣    Add Business Contact");
            System.out.println("3️⃣    Update Contact");
            System.out.println("4️⃣    Delete Contact");
            System.out.println("5️⃣    Display All Contacts");
            System.out.println("6️⃣    Manage Groups"); 
            System.out.println("7️⃣    Manage Favorites"); // ✅ Updated to Favorites
            System.out.println("8️⃣    Exit");

            System.out.print("➡ Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    addPersonalContact(scanner);
                    break;
                case 2:
                    addBusinessContact(scanner);
                    break;
                case 3:
                    updateContact(scanner);
                    break;
                case 4:
                    deleteContact(scanner);
                    break;
                case 5:
                     contactController.displayUserContacts(userId);
                     break;
                case 6:
                      groupview.start();
                      break;
                case 7:
                      favoriteview.start();
                      break;
                case 8:
                    System.out.println("📴 Exiting application.");
                    return;
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }

    private void addPersonalContact(Scanner scanner) {
       try{
        System.out.println("\n📌 Enter details for Personal Contact:");

        System.out.print("👤 First Name: ");
        String firstName = scanner.nextLine();

        System.out.print("👤 Surname: ");
        String surname = scanner.nextLine();

        System.out.print("📞 Phone: ");
        String phone = scanner.nextLine();
        

        System.out.print("📧 Email: ");
        String email = scanner.nextLine();

        System.out.print("📂 Profile Picture (path, or leave empty): ");
        String profilePicturePath = scanner.nextLine();
        File profilePictureFile = profilePicturePath.isEmpty() ? null : new File(profilePicturePath);

        // Create and add contact
        PersonalContact personalContact = new PersonalContact(profilePictureFile, firstName, surname, phone, email);
        contactController.addContact(personalContact,userId);
        System.out.println("✅ Personal contact added successfully!");
    }
    catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
            }
        catch(InputMismatchException e){
           System.out.println("❌ Invalid phone number! Please enter numbers only.");
           scanner.nextLine();
           }
        catch (Exception e) {
        System.out.println("❌ Unexpected error: " + e.getMessage());
    }
    }

    private void addBusinessContact(Scanner scanner) {
       try{
        System.out.println("\n📌 Enter details for Business Contact:");

        System.out.print("👤 First Name: ");
        String firstName = scanner.nextLine();

        System.out.print("👤 Surname: ");
        String surname = scanner.nextLine();

        System.out.print("📞 Phone: ");
        String phone = scanner.nextLine();
        scanner.nextLine(); // Consume newline

        System.out.print("📧 Email: ");
        String email = scanner.nextLine();

        System.out.print("🏢 Company Name: ");
        String companyName = scanner.nextLine();

        System.out.print("📂 Profile Picture (path, or leave empty): ");
        String profilePicturePath = scanner.nextLine();
        File profilePictureFile = profilePicturePath.isEmpty() ? null : new File(profilePicturePath);

        // Create and add contact
        BusinessContact businessContact = new BusinessContact(profilePictureFile, firstName, surname, phone, email, companyName);
        contactController.addContact(businessContact,userId);
        System.out.println("✅ Business contact added successfully!");
    }
        catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
            }
        catch(InputMismatchException e){
           System.out.println("❌ Invalid phone number! Please enter numbers only.");
           scanner.nextLine();
           }
        catch (Exception e) {
        System.out.println("❌ Unexpected error: " + e.getMessage());
    }
    }
    

    private void updateContact(Scanner scanner) {
       try{
        System.out.print("\n🆔 Enter Contact ID to Update: ");
        int contactId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.println("\n🔄 Enter new details (leave blank to keep unchanged):");

        System.out.print("👤 First Name: ");
        String firstName = scanner.nextLine();
        firstName = firstName.isEmpty() ? null : firstName;

        System.out.print("👤 Surname: ");
        String surname = scanner.nextLine();
        surname = surname.isEmpty() ? null : surname;

        System.out.print("📞 Phone: ");
        String phone = scanner.nextLine();
        phone = phone.isEmpty() ? null : phone;

        System.out.print("📧 Email: ");
        String email = scanner.nextLine();
        email = email.isEmpty() ? null : email;

        System.out.print("📂 Profile Picture (path, or leave empty): ");
        String profilePicturePath = scanner.nextLine();
        File profilePicture = profilePicturePath.isEmpty() ? null : new File(profilePicturePath);

        System.out.print("🏢 Company Name (if Business Contact): ");
        String companyName = scanner.nextLine();
        companyName = companyName.isEmpty() ? null : companyName;

        System.out.print("🔖 Contact Type (personal/business): ");
        String contactType = scanner.nextLine();
        contactType = contactType.isEmpty() ? null : contactType.toLowerCase();

        boolean updated = contactController.updateContact(contactId, firstName, surname, phone, email, profilePicture, contactType, companyName);

        if (updated) {
            System.out.println("✅ Contact Updated Successfully!");
        } else {
            System.out.println("❌ Update Failed. Contact Not Found!");
        }
    }
        catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
            }
        catch(InputMismatchException e){
           System.out.println("❌ Invalid phone number! Please enter numbers only.");
           scanner.nextLine();
           }
        catch (Exception e) {
        System.out.println("❌ Unexpected error: " + e.getMessage());
    }
    }

    private void deleteContact(Scanner scanner) {
        System.out.print("\n🗑️ Enter Contact ID to Delete: ");
        int contactId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        boolean deleted = contactController.deleteContact(contactId);
        if (deleted) {
            System.out.println("✅ Contact Deleted Successfully!");
        } else {
            System.out.println("❌ Delete Failed. Contact Not Found!");
        }
    }
}

