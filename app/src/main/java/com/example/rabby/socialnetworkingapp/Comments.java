package com.example.rabby.socialnetworkingapp;

public class Comments {
    private String comment, date, time, uid, userProfileImage, userFullName;

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public Comments(){

    }

    public Comments(String comment, String date, String time, String uid, String userProfileImage, String userFullName) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.uid = uid;
        this.userProfileImage = userProfileImage;
        this.userFullName = userFullName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }
}
