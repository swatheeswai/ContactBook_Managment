package com.viewClass;

import com.controllerClass.GroupController;
import com.modelClass.Group;

import java.util.Scanner;

public class GroupView {
    private final GroupController groupController;
    private final int userId;

    public GroupView(GroupController groupController, int userId) {
        this.groupController = groupController;
        this.userId = userId;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n🔹 Group Management:");
            System.out.println("1️⃣   Create Group");
            System.out.println("2️⃣   Add Contact to Group");
            System.out.println("3️⃣   Remove Contact from Group");
            System.out.println("4️⃣   Update Contact in Group"); // ✅ NEW CASE ADDED
            System.out.println("5️⃣   Display Group Contacts");
            System.out.println("6️⃣   Exit");
            System.out.print("➡ Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    createGroup(scanner);
                    break;
                case 2:
                    addContactToGroup(scanner);
                    break;
                case 3:
                     removeContactFromGroup(scanner);
                     break;
                case 4:
                      updateContactFromGroup(scanner);
                      break;
                case 5:
                    groupController.displayGroups(userId);
                    break;
                case 6:
                    System.out.println("📴 Exiting Group Management.");
                    return;
                default:
                    System.out.println("❌ Invalid choice. Try again.");
            }
        }
    }

    private void createGroup(Scanner scanner) {
        System.out.print("\n📛 Group Name: ");
        String groupName = scanner.nextLine();

        Group group = new Group(userId, groupName);
        int groupId = groupController.addGroup(group);

        if (groupId != -1) {
            System.out.println("✅ Group created successfully! Group ID: " + groupId);
        } else {
            System.out.println("❌ Failed to create group.");
        }
    }

    private void addContactToGroup(Scanner scanner) {
    System.out.print("\n🆔 Group ID: ");
    int groupId = scanner.nextInt();

    System.out.print("📞 Contact ID to add: ");
    int contactId = scanner.nextInt();

    // ✅ Pass userId as 3rd parameter!
    if (groupController.addContactToGroup(groupId, contactId, userId)) {
        System.out.println("✅ Contact added to group successfully!");
    } else {
        System.out.println("❌ Failed to add contact to group.");
    }
}
     private void removeContactFromGroup(Scanner scanner){
        System.out.print("🆔 Enter Group ID: ");
        int groupId = scanner.nextInt();

        System.out.print("🆔 Enter Contact ID: ");
        int contactId = scanner.nextInt();
        
       if(groupController.removeContactFromGroup(contactId,groupId)){
                 System.out.println("✅ Contact removed from group!");
           }
       else{
               System.out.println("❌ Failed to remove contact!");
               }
        }
     private void updateContactFromGroup(Scanner scanner){
           System.out.println("🆔 Enter Group ID: ");
           int groupId = scanner.nextInt();
           
           System.out.println("🆔 Enter Old Contact ID: ");
           int oldContactId = scanner.nextInt();
           
           System.out.println("🆔 Enter New Contact ID: ");
           int newContactId = scanner.nextInt();
           
            boolean updated = groupController.updateContactFromGroup(groupId, oldContactId, newContactId);
    
               if (updated) {
                     System.out.println("✅ Contact Updated Successfully in Group!");
               } else {
                      System.out.println("❌ Update Failed. Contact or Group Not Found!");
               } 
          }
                        
        }

