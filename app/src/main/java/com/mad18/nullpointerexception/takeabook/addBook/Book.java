package com.mad18.nullpointerexception.takeabook.addBook;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class Book{

    private String ISBN;
    private String title;
    private Map<String,Boolean> authors;
    private String publisher;
    private Integer editionYear;

    //Constructor
    public Book(String ISBN,String title, Map<String, Boolean> authors, String publisher, Integer editionYear){
        this.ISBN = ISBN;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.editionYear = editionYear;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Boolean> getAuthors() {
        return authors;
    }

    public void setAuthors(Map<String, Boolean> authors) {
        this.authors = authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getEditionYear() {
        return editionYear;
    }

    public void setEditionYear(Integer editionYear) {
        this.editionYear = editionYear;
    }
}
