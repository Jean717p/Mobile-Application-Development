package com.mad18.nullpointerexception.takeabook.requestBook;

import java.util.Date;

public class Review {

    private String UserID, Username, text;
    private Date reviewDate;
    private int rating;

    public Review(){};

    public Review(String userID, String username, String text, int rating,Date reviewDate) {
        this.UserID = userID;
        this.Username = username;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Date reviewDate) {
        this.reviewDate = reviewDate;
    }
}
