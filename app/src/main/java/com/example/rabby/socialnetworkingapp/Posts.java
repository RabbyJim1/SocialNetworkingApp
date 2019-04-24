package com.example.rabby.socialnetworkingapp;

public class Posts {

    private String uid, time, date, description, profileImage, fullName, postimage;


    public Posts()
    {

    }


    public Posts(String uid, String time, String date, String description, String profileImage, String fullName, String postimage) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.description = description;
        this.profileImage = profileImage;
        this.fullName = fullName;
        this.postimage = postimage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }
}
