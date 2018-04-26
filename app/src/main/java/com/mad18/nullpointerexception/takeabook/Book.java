package com.mad18.nullpointerexception.takeabook;

import java.util.Map;

public class Book {

    String book_ISBN;
    String book_title;
    String book_author;
    String book_publisher;
    int book_editionYear;
    int book_condition;
    Map<String,Boolean> book_authors;

    public Book() {}

    public Book(String ISBN, String title, String author, String publisher, int editionYear, int condition,Map<String,Boolean> authors){
        book_ISBN = ISBN;
        book_title = title;
        book_author = author;
        book_publisher = publisher;
        book_editionYear = editionYear;
        book_condition = condition;
        book_authors = authors;
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

    public String getBook_author() {
        return book_author;
    }

    public void setBook_author(String book_author) {
        this.book_author = book_author;
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
}
