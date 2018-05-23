package com.mad18.nullpointerexception.takeabook;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.mad18.nullpointerexception.takeabook.addBook.BookWrapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserWrapper implements Parcelable{

    String user_wrapper_name;
    String user_wrapper_city;
    String user_wrapper_mail;
    String user_wrapper_about;
    String user_wrapper_id;
    List<String> user_wrapper_books;
    //GeoPoint user_wrapper_geoPoint;
    String user_wrapper_profileImgStoragePath;

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UserWrapper createFromParcel(Parcel in) {
            return new UserWrapper(in);
        }

        public UserWrapper[] newArray(int size) {
            return new UserWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.user_wrapper_name);
        parcel.writeString(this.user_wrapper_city);
        parcel.writeString(this.user_wrapper_mail);
        parcel.writeString(this.user_wrapper_about);
        parcel.writeString(this.user_wrapper_id);
        parcel.writeStringList(this.user_wrapper_books);
        parcel.writeString(this.user_wrapper_profileImgStoragePath);
    }

    public UserWrapper(String user_wrapper_name, String user_wrapper_city, String user_wrapper_mail,
                       String user_wrapper_about, String user_wrapper_id, List<String> user_wrapper_books, String user_wrapper_profileImgStoragePath) {
        this.user_wrapper_name = user_wrapper_name;
        this.user_wrapper_city = user_wrapper_city;
        this.user_wrapper_mail = user_wrapper_mail;
        this.user_wrapper_about = user_wrapper_about;
        this.user_wrapper_id = user_wrapper_id;
        this.user_wrapper_books = user_wrapper_books;
        //this.user_wrapper_geoPoint = user_wrapper_geoPoint;
        this.user_wrapper_profileImgStoragePath = user_wrapper_profileImgStoragePath;
    }

    public UserWrapper(User user){
        this.user_wrapper_name = user.getUsr_name();
        this.user_wrapper_city = user.getUsr_city();
        this.user_wrapper_mail = user.getUsr_mail();
        this.user_wrapper_about = user.getUsr_about();
        this.user_wrapper_id = user.getUsr_id();
        this.user_wrapper_books = new LinkedList<>(user.getUsr_books().keySet());
        //this.user_wrapper_geoPoint = user.getUsr_geoPoint();
        this.user_wrapper_profileImgStoragePath = user.getProfileImgStoragePath();
    }

    public UserWrapper(Parcel in){
        this.user_wrapper_name = in.readString();
        this.user_wrapper_city = in.readString();
        this.user_wrapper_mail = in.readString();
        this.user_wrapper_about = in.readString();
        this.user_wrapper_id = in.readString();
        this.user_wrapper_books = in.createStringArrayList();
        //this.user_wrapper_geoPoint = in.re;
        this.user_wrapper_profileImgStoragePath = in.readString();
    }

    @Override
    public String toString() {
        String Totbooks= ", books='";
//        for (String key : authors.keySet()) {
//            Totauthors = Totauthors + key + '\'';
//        }
        Totbooks = Totbooks +this.user_wrapper_books.toString()+'\'';

        return "UserWrapper{"+"name='"+user_wrapper_name+'\''+
                ", city='" + user_wrapper_city + '\'' +
                ", mail='" + user_wrapper_mail + '\'' +

                ", about='" + user_wrapper_about + '\'' +
                ", id='" + user_wrapper_id + '\'' +

                Totbooks+
                ", imgStrgPath='" + user_wrapper_profileImgStoragePath + '\'' +
                '}';
    }

    public String getUser_wrapper_name() {
        return user_wrapper_name;
    }

    public void setUser_wrapper_name(String user_wrapper_name) {
        this.user_wrapper_name = user_wrapper_name;
    }

    public String getUser_wrapper_city() {
        return user_wrapper_city;
    }

    public void setUser_wrapper_city(String user_wrapper_city) {
        this.user_wrapper_city = user_wrapper_city;
    }

    public String getUser_wrapper_mail() {
        return user_wrapper_mail;
    }

    public void setUser_wrapper_mail(String user_wrapper_mail) {
        this.user_wrapper_mail = user_wrapper_mail;
    }

    public String getUser_wrapper_about() {
        return user_wrapper_about;
    }

    public void setUser_wrapper_about(String user_wrapper_about) {
        this.user_wrapper_about = user_wrapper_about;
    }

    public String getUser_wrapper_id() {
        return user_wrapper_id;
    }

    public void setUser_wrapper_id(String user_wrapper_id) {
        this.user_wrapper_id = user_wrapper_id;
    }

    public List<String> getUser_wrapper_books() {
        return user_wrapper_books;
    }

    public void setUser_wrapper_books(List<String> user_wrapper_books) {
        this.user_wrapper_books = user_wrapper_books;
    }

//    public GeoPoint getUser_wrapper_geoPoint() {
//        return user_wrapper_geoPoint;
//    }
//
//    public void setUser_wrapper_geoPoint(GeoPoint user_wrapper_geoPoint) {
//        this.user_wrapper_geoPoint = user_wrapper_geoPoint;
//    }

    public String getUser_wrapper_profileImgStoragePath() {
        return user_wrapper_profileImgStoragePath;
    }

    public void setUser_wrapper_profileImgStoragePath(String user_wrapper_profileImgStoragePath) {
        this.user_wrapper_profileImgStoragePath = user_wrapper_profileImgStoragePath;
    }


}
