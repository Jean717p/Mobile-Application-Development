package com.mad18.nullpointerexception.takeabook.util;



import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    String usr_name;
    String usr_city;
    String usr_mail;
    String usr_about;
    String usr_id;
    Map<String,Boolean> usr_books;
    GeoPoint usr_geoPoint;
    String profileImgStoragePath;
    List<String> registrationTokens;

    public User(){}

    public User(String email, String displayName, String city, String about, HashMap<String, Boolean> stringBooleanHashMap, GeoPoint geoPoint, String id, List<String> tokens) {
        usr_mail = email;
        usr_name = displayName;
        usr_city = city;
        usr_about = about;
        usr_books = stringBooleanHashMap;
        usr_geoPoint = geoPoint;
        profileImgStoragePath = "";
        usr_id = id;
        registrationTokens = tokens;
    }

    public User (UserWrapper userWrapper){
        usr_mail = userWrapper.getUser_wrapper_mail();
        usr_name = userWrapper.getUser_wrapper_name();
        usr_city = userWrapper.getUser_wrapper_city();
        usr_about = userWrapper.getUser_wrapper_about();
        List<String> uwBooks = userWrapper.getUser_wrapper_books();
        for (String y : uwBooks) {
            usr_books.put(y, true);
        }
        //usr_geoPoint = userWrapper.getUser_wrapper_geoPoint();
        profileImgStoragePath = "";
        usr_id = userWrapper.getUser_wrapper_id();
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

    public String getUsr_id() {
        return usr_id;
    }

    public void setUsr_id(String usr_id) {
        this.usr_id = usr_id;
    }

    public List<String> getRegistrationTokens() {
        return registrationTokens;
    }

    public void setRegistrationTokens(List<String> registrationTokens) {
        this.registrationTokens = registrationTokens;
    }
}
