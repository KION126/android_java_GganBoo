package com.example.gganboo.profile;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    public String userId; // 사용자 ID
    public String email; // 사용자 이메일
    public String name; // 사용자 이름
    public String description; // 사용자 설명/소개
    public String imageUrl; // 사용자 프로필 이미지 URL
    public List<String> followers; // 팔로워 목록
    public List<String> following; // 팔로잉 목록

    public UserProfile() {
        this.followers = new ArrayList<>(); // 팔로워 리스트 초기화
        this.following = new ArrayList<>(); // 팔로잉 리스트 초기화
    }

    // 사용자 프로필 생성자
    public UserProfile(String userId, String email, String name, String description, String imageUrl) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.followers = new ArrayList<>(); // 팔로워 리스트 초기화
        this.following = new ArrayList<>(); // 팔로잉 리스트 초기화
        // 기본적으로 관리자 ID 추가
        this.followers.add("null"); // 기본 팔로워 추가
        this.following.add("null"); // 기본 팔로잉 추가
    }

    // Getters and setters
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
