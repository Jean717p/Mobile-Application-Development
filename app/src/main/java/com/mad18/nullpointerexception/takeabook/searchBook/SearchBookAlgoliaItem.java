package com.mad18.nullpointerexception.takeabook.searchBook;

public class SearchBookAlgoliaItem {
    String title, author, thumbnailURL, ISBN;

    public SearchBookAlgoliaItem(String title, String author, String thumbnailURL, String ISBN) {
        this.title = title;
        this.author = author;
        this.thumbnailURL = thumbnailURL;
        this.ISBN = ISBN;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
}
