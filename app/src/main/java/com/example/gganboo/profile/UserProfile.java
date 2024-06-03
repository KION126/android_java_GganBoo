package com.example.gganboo.profile;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    public String userId;
    public String email;
    public String name;
    public String description;
    public String imageUrl;
    public List<String> followers;
    public List<String> following;

    public UserProfile() {
        // Default constructor required for calls to DataSnapshot.getValue(UserProfile.class)
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
    }

    public UserProfile(String userId, String email, String name, String description, String imageUrl) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.followers = new ArrayList<>();
        this.following = new ArrayList<>();
        // 기본적으로 관리자 ID 추가
        this.followers.add("null");
        this.following.add("null");
    }

    // Getters and setters if needed
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }
}
