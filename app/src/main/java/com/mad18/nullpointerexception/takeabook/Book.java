package com.mad18.nullpointerexception.takeabook;

import android.os.Build;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Map;

public class Book {

    String book_ISBN;
    String book_title;
    String book_first_author;
    String book_publisher;
    String book_thumbnail_url;
    int book_editionYear;
    int book_condition;
    String book_userid;
    Map<String,Boolean> book_authors;
    Map<String,Boolean> book_categories;
    String book_description;
    GeoPoint book_location;


    public Book() {}

    public String getUserid() {
        return book_userid;
    }

    public void setUserid(String userid) {
        this.book_userid = userid;
    }

    public Book(String ISBN, String title, String publisher, int editionYear, int condition, String user, Map<String,Boolean> authors, String description, String thumbnail, Map<String,Boolean> categories, GeoPoint geoPoint){
        book_ISBN = ISBN;
        book_title = title;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            book_first_author = authors.keySet().stream().findFirst().orElse(null);
        }
        else{
            if(authors.size()>0){
                book_first_author = (String) (authors.keySet().toArray())[0];
            }
        }

        book_publisher = publisher;
        book_editionYear = editionYear;
        book_condition = condition;
        book_authors = authors;
        book_userid = user;
        book_description = description;
        book_thumbnail_url = thumbnail;
        book_categories = categories;
        book_location = geoPoint;

    }

    public String getBook_ISBN() {
        return book_ISBN;
    }

    public void setBook_ISBN(String book_ISBN) {
        this.book_ISBN = book_ISBN;
    }

    public String getBook_title() {
        return book_title;
    }

    public void setBook_title(String book_title) {
        this.book_title = book_title;
    }

    public String getBook_first_author() {
        return book_first_author;
    }

    public void setBook_first_author(String book_first_author) {
        this.book_first_author = book_first_author;
    }

    public String getBook_publisher() {
        return book_publisher;
    }

    public void setBook_publisher(String book_publisher) {
        this.book_publisher = book_publisher;
    }

    public int getBook_editionYear() {
        return book_editionYear;
    }

    public void setBook_editionYear(int book_editionYear) {
        this.book_editionYear = book_editionYear;
    }

    public int getBook_condition() {
        return book_condition;
    }

    public void setBook_condition(int book_condition) {
        this.book_condition = book_condition;
    }

    public Map<String, Boolean> getBook_authors() {
        return book_authors;
    }

    public void setBook_authors(Map<String, Boolean> book_authors) {
        this.book_authors = book_authors;
    }

    public String getBook_thumbnail_url() {
        return book_thumbnail_url;
    }

    public void setBook_thumbnail_url(String book_thumbnail_url) {
        this.book_thumbnail_url = book_thumbnail_url;
    }

    public Map<String, Boolean> getBook_categories() {
        return book_categories;
    }

    public void setBook_categories(Map<String, Boolean> book_categories) {
        this.book_categories = book_categories;
    }

    public String getBook_userid() {
        return book_userid;
    }

    public void setBook_userid(String book_userid) {
        this.book_userid = book_userid;
    }

    public String getBook_description() {
        return book_description;
    }

    public void setBook_description(String book_description) {
        this.book_description = book_description;
    }

    public GeoPoint getBook_location() {
        return book_location;
    }

    public void setBook_location(GeoPoint book_location) {
        this.book_location = book_location;
    }
}
