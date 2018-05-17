package com.mad18.nullpointerexception.takeabook;



import android.net.Uri;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class User {

    String usr_name;
    String usr_city;
    String usr_mail;
    String usr_about;
    Map<String,Boolean> usr_books;
    GeoPoint usr_geoPoint;
    String profileImgStoragePath;

    public User(){}

    public User(String email, String displayName, String s, String s1, HashMap<String, Boolean> stringBooleanHashMap, GeoPoint geoPoint) {
        usr_mail = email;
        usr_name = displayName;
        usr_city = s;
        usr_about = s1;
        usr_books = stringBooleanHashMap;
        usr_geoPoint = geoPoint;
        profileImgStoragePath = "";
    }

    public String getUsr_name() {
        return usr_name;
    }

    public void setUsr_name(String usr_name) {
        this.usr_name = usr_name;
    }

    public String getUsr_city() {
        return usr_city;
    }

    public void setUsr_city(String usr_city) {
        this.usr_city = usr_city;
    }

    public String getUsr_mail() {
        return usr_mail;
    }

    public void setUsr_mail(String usr_mail) {
        this.usr_mail = usr_mail;
    }

    public String getUsr_about() {
        return usr_about;
    }

    public void setUsr_about(String usr_about) {
        this.usr_about = usr_about;
    }

    public Map<String, Boolean> getUsr_books() {
        return usr_books;
    }

    public void setUsr_books(Map<String, Boolean> usr_books) {
        this.usr_books = usr_books;
    }

    public GeoPoint getUsr_geoPoint() {
        return usr_geoPoint;
    }

    public void setUsr_geoPoint(GeoPoint usr_geoPoint) {
        this.usr_geoPoint = usr_geoPoint;
    }

    public String getProfileImgStoragePath() {
        return profileImgStoragePath;
    }

    public void setProfileImgStoragePath(String profileImgStoragePath) {
        this.profileImgStoragePath = profileImgStoragePath;
    }
}
