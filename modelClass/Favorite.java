package com.modelClass;

public class Favorite {
    private int favoriteId;
    private int userId;
    private int contactId;

    public Favorite(int favoriteId, int userId, int contactId) {
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.contactId = contactId;
    }

    public int getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(int favoriteId) {
        this.favoriteId = favoriteId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }
}

