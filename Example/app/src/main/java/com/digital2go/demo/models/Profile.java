package com.digital2go.demo.models;

import com.google.gson.Gson;

/**
 * Created by Ulrick on 10/10/2016.
 */
public class Profile {
    private String first_name;
    private String last_name;
    private String username;
    private char sex = ' ';
    private int age;
    private String city;
    private String state;
    private int zipcode;
    private String email;

    private boolean badge, alert, sound;

    public Profile() {
    }

    public Profile(String first_name, String last_name, char sex, int age, String city, String state, int zipcode, String email, boolean badge, boolean alert, boolean sound) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.sex = sex;
        this.age = age;
        this.city = city;
        this.state = state;
        this.zipcode = zipcode;
        this.email = email;
        this.badge = badge;
        this.alert = alert;
        this.sound = sound;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public char getSex() {
        return sex;
    }

    public void setSex(char sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getZipcode() {
        return zipcode;
    }

    public void setZipcode(int zipcode) {
        this.zipcode = zipcode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isBadge() {
        return badge;
    }

    public void setBadge(boolean badge) {
        this.badge = badge;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public boolean isSound() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    public String getGender(){
        String gender = null;
        switch (sex){
            case 'f':
                gender =  "female";
                break;

            case 'm':
                gender = "male";
                break;
        }

        return gender;
    }


    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }
}
