package com.example.rawand.raceme;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by RAWAND on 29/11/2014.
 *
 * Contains any helper methods that is used by RegisterActivity.
 */

public class RegisterUtils extends Activity{


    /**
     * Query database to check if email exists
     *
     * @param email
     * @return true if email is unique and false if not.
     */
    public static boolean isEmailUnique(String email){

        DatabaseConnection dbConnection = null;
        try {
            dbConnection = new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        String sqlQuery = "SELECT COUNT(*) FROM coac11.user_table WHERE (email ='" + email + "');";

        DatabaseQuery query = new DatabaseQuery(dbConnection,sqlQuery);
        if(query.run()){
            //If the query has run successfully, check the result.
            if(Integer.parseInt((String) query.get(0,0)) == 0){
                //The statement returned 0, meaning username is unique. Return true.
                return true;
            }
        }
        return false;

    }


    /**
     * Query database to check if email exists.
     *
     * @param username
     * @return true if username is unique and false if not.
     */
    public static boolean isUsernameUnique(String username){

        DatabaseConnection dbConnection = null;
        try {
            dbConnection = new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        String sqlQuery = "SELECT COUNT(*) FROM coac11.user_table WHERE (username ='" + username + "');";

        DatabaseQuery query = new DatabaseQuery(dbConnection,sqlQuery);
        if(query.run()){
            //If the query has run successfully, check the result.
            if(Integer.parseInt((String) query.get(0,0)) == 0){
                //The statement returned 0, meaning username is unique. Return true.
                return true;
            }
        }
        return false;

    }

    /**
     * Method to register a new user.
     *
     * @param firstname
     * @param surname
     * @param email
     * @param username
     * @param password
     * @param gender
     * @return true if user added to database, false if not.
     */
    public static boolean registerUser(String firstname, String surname, String email, String username, String password, String gender){
        DatabaseConnection dbConnection;

        try {
            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

        String sqlQuery = "INSERT INTO `coac11`.`user_table` (`username`, `email`, `password`, `firstname`, `surname`, `gender`)"
                +         "             VALUES ('" + username + "', '"+email+ "', '" + password + "', '" + firstname + "', '" + surname +"', '" + gender + "');";

        DatabaseQuery query = new DatabaseQuery(dbConnection,sqlQuery);
        if(query.run()){
            return true;
        };
        return false;


    }

    /**
     * Get the user details by their email or username
     *
     * @param login Username or email
     * @return User object with user details.
     */

    public static User getUserDetails(String login){
        DatabaseConnection dbConnection = null;
        User userData = null;
        try {
            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            userData = new User();
        } catch (SQLException e) {
            e.printStackTrace();
            userData = new User();
        } catch (InstantiationException e) {
            e.printStackTrace();
            userData = new User();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            userData = new User();
        }

        String sqlQuery =
                "SELECT user_table.id, user_table.username, user_table.email,"
                        + "       user_table.firstname, user_table.surname, user_table.profile_img,"
                        + "           user_table.is_active, user_table.gender FROM coac11.user_table"
                        + "WHERE (username ='" + login + "' or email = '" + login + "');";


        DatabaseQuery query = new DatabaseQuery(dbConnection , sqlQuery);

        if(query.run() && query.getRowCount() >= 1) {
            String user_id = query.get(0, 0);
            String username = query.get(0, 1);
            String email = query.get(0, 2);
            String firstname = query.get(0, 3);
            String surname = query.get(0, 4);
            String profile_img = query.get(0, 5);
            String is_active = query.get(0, 6);
            String gender = query.get(0, 7);

            userData = new User(user_id, username, email, firstname,
                    surname, profile_img, is_active, gender);
        }

        return userData;
    }
}
