package com.mad18.nullpointerexception.takeabook.util;

import java.util.Date;

public class Review {

    private String UserID, Username, text, userPic;
    private Date reviewDate;
    private float rating;

    public Review(){}

    public Review(String userID, String username,String userPic, String text, float rating,Date reviewDate) {
        this.UserID = userID;
        this.Username = username;
        this.userPic = userPic;
        this.text = text;
        this.rating = rating;
        this.reviewDate = reviewDate;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }
}
