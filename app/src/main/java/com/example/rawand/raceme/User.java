package com.example.rawand.raceme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by RAWAND on 29/11/2014.
 *
 * Class to store user information
 */
public class User {

    private String user_id;
    private String username;
    private String email;
    private String firstname;
    private String surname;
    private String profile_img;
    private String is_active;
    private String gender;
    private boolean isEmpty;

    /**
     * Constructor to create empty user
     */
    User(){
        this.user_id ="";
        this.username ="";
        this.email ="";
        this.firstname ="";
        this.surname ="";
        this.profile_img ="";
        this.is_active ="";
        this.gender ="";
        isEmpty = true;

    }

    User(	String user_id,
             String username,
             String email,
             String firstname,
             String surname,
             String profile_img,
             String is_active,
             String gender)
    {
        isEmpty = false;
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.firstname = firstname;
        this.surname = surname;
        this.profile_img = profile_img;
        this.is_active = is_active;
        this.gender = gender;
    }

    //Getters

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return user_id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getSurname() {
        return surname;
    }

    public String getProfileImg() {
        return profile_img;
    }

    public String getIsActive() {
        return is_active;
    }

    public String getGender() {
        return gender;
    }


    /**
     * Check if the user object contains no user.
     * @return
     */
    public boolean isEmpty(){
        return isEmpty;
    }

    @Override
    public String toString(){

        return getUsername() + " ( "+getFirstname() + " " + getSurname() + " )";
    }

    /**
     * Get all user details as a string
     * @return String user details.
     */
    public String allToString(){

        return "[" + getUserId() + "," + getUsername() +
                "," + getEmail() + "," + getFirstname() +
                "," + getSurname() + ","+ getProfileImg() +
                "," + getIsActive() + "," + getGender() + "]";
    }

}