package com.mad18.nullpointerexception.takeabook.addBook;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable{

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
    private String totalItems;
    private String id;

    //Constructor
    public Book(String totalItems, String id){
        this.totalItems = totalItems;
        this.id = id;
    }

    public String getTotalItems(){
        return totalItems;
    }

    public void setTotalItems(String id){
        this.totalItems = totalItems;
    }

    public String getId(){
        return id;
    }

    public void setId(String totalItems){
        this.id = id;
    }
    public Book(Parcel in){
        this.totalItems = in.readString();
        this.id = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.totalItems);
        dest.writeString(this.id);
    }
    @Override
    public String toString() {
        return "Book{"+"totalItems='"+totalItems+'\''+
        ", id='" + id + '\'' +
                '}';
    }
}
