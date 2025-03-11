package com.modelClass;

import java.util.List;

public class Group {
    private int groupId;
    private int userId;
    private String groupName;
    private List<Contact> members;

    // Constructor
    public Group(int userId, String groupName) {
        this.userId = userId;
        this.groupName = groupName;
    }

    // Getters and Setters
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Contact> getMembers() {
        return members;
    }

    public void setMembers(List<Contact> members) {
        this.members = members;
    }
}

