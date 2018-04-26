package com.mad18.nullpointerexception.takeabook;

import java.util.HashMap;
import java.util.Map;

public class User {

    String usr_name;
    String usr_city;
    String usr_mail;
    String usr_about;
    Map<String,Boolean> usr_books;

    public User(){}

    public User(String email, String displayName, String s, String s1, HashMap<String, Boolean> stringBooleanHashMap) {
        usr_mail = email;
        usr_name = displayName;
        usr_city = s;
        usr_about = s1;
        usr_books = stringBooleanHashMap;
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
}
