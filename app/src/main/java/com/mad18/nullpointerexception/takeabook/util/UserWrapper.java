package com.mad18.nullpointerexception.takeabook.util;

import android.os.Parcel;
import android.os.Parcelable;


import java.util.LinkedList;
import java.util.List;


public class UserWrapper implements Parcelable{

    String user_wrapper_name;
    String user_wrapper_city;
    String user_wrapper_mail;
    String user_wrapper_about;
    String user_wrapper_id;
    List<String> user_wrapper_books;
    //GeoPoint user_wrapper_geoPoint;
    String user_wrapper_profileImgStoragePath;
    private double user_wrapper_lat, user_wrapper_longitude;

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
        parcel.writeDouble(this.user_wrapper_lat);
        parcel.writeDouble(this.user_wrapper_longitude);

    }

    public UserWrapper(String user_wrapper_name, String user_wrapper_city, String user_wrapper_mail,
                       String user_wrapper_about, String user_wrapper_id, List<String> user_wrapper_books, String user_wrapper_profileImgStoragePath,
                       Double lat, Double longitude) {
        this.user_wrapper_name = user_wrapper_name;
        this.user_wrapper_city = user_wrapper_city;
        this.user_wrapper_mail = user_wrapper_mail;
        this.user_wrapper_about = user_wrapper_about;
        this.user_wrapper_id = user_wrapper_id;
        this.user_wrapper_books = user_wrapper_books;
        this.user_wrapper_profileImgStoragePath = user_wrapper_profileImgStoragePath;
        this.user_wrapper_lat = lat;
        this.user_wrapper_longitude = longitude;
    }

    public UserWrapper(User user){
        this.user_wrapper_name = user.getUsr_name();
        this.user_wrapper_city = user.getUsr_city();
        this.user_wrapper_mail = user.getUsr_mail();
        this.user_wrapper_about = user.getUsr_about();
        this.user_wrapper_id = user.getUsr_id();
        this.user_wrapper_books = new LinkedList<>(user.getUsr_books().keySet());
        this.user_wrapper_profileImgStoragePath = user.getProfileImgStoragePath();
        this.user_wrapper_lat = user.getUsr_geoPoint().getLatitude();
        this.user_wrapper_longitude = user.getUsr_geoPoint().getLongitude();
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
        this.user_wrapper_lat = in.readDouble();
        this.user_wrapper_longitude = in.readDouble();
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
                ", lat='" + Double.toString(user_wrapper_lat) + '\'' +
                ", longitude='" + Double.toString(user_wrapper_longitude) + '\'' +
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

    public String getUser_wrapper_profileImgStoragePath() {
        return user_wrapper_profileImgStoragePath;
    }

    public void setUser_wrapper_profileImgStoragePath(String user_wrapper_profileImgStoragePath) {
        this.user_wrapper_profileImgStoragePath = user_wrapper_profileImgStoragePath;
    }
    public double getUser_wrapper_lat() {
        return user_wrapper_lat;
    }

    public void setUser_wrapper_lat(double user_wrapper_lat) {
        this.user_wrapper_lat = user_wrapper_lat;
    }

    public double getUser_wrapper_longitude() {
        return user_wrapper_longitude;
    }

    public void setUser_wrapper_longitude(double user_wrapper_longitude) {
        this.user_wrapper_longitude = user_wrapper_longitude;
    }

}
