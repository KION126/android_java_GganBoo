package com.example.gganboo.profile;

public class UserProfile {
    public String userId;
    public String email;
    public String name;
    public String description;
    public String imageUrl;

    public UserProfile() {
        // Default constructor required for calls to DataSnapshot.getValue(UserProfile.class)
    }

    public UserProfile(String userId, String email, String name, String description, String imageUrl) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
