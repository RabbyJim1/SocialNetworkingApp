package com.example.rabby.socialnetworkingapp;

public class FindFriends {

    private String profileImage, status, fullName;

    public FindFriends(String profileImage, String status, String fullName) {
        this.profileImage = profileImage;
        this.status = status;
        this.fullName = fullName;
    }
    public FindFriends(){

    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
