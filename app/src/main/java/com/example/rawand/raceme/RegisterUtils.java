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
 */
public class RegisterUtils extends Activity{


    /**
     * @param email
     * @return true if email is unique and false if not.
     */
    public static boolean isEmailUnique(String email){
//        DatabaseConnection dbConnection;
//
//        try {
//            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            return false;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        String sqlQuery = "SELECT user_table.id FROM coac11.user_table WHERE (email = '" + email + "');";
//        DatabaseQuery dbQuery = new DatabaseQuery(dbConnection,sqlQuery);
//
//        if(dbQuery.getRowCount() == 0){
//            return true;
//        }
//
//        return false;

        String sqlQuery = "SELECT COUNT(*) FROM coac11.user_table WHERE (email ='" + email + "');";


        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
            Statement statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            resultSet.first();
            if(resultSet.getInt(1) == 0){
                dbConnection.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * @param username
     * @return true if username is unique and false if not.
     */
    public static boolean isUsernameUnique(String username){

        String sqlQuery = "SELECT COUNT(*) FROM coac11.user_table WHERE (username ='" + username + "');";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
            Statement statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            resultSet.first();
            if(resultSet.getInt(1) == 0){
                dbConnection.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
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
        }

        String sqlQuery = "INSERT INTO `coac11`.`user_table` (`username`, `email`, `password`, `firstname`, `surname`, `gender`)"
                +         "             VALUES ('" + username + "', '"+email+ "', '" + password + "', '" + firstname + "', '" + surname +"', '" + gender + "');";

        Statement statement = null;
        try {
            statement = dbConnection.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.executeUpdate(sqlQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

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
        }

        String sqlQuery =
                "SELECT user_table.id, user_table.username, user_table.email,"
                        + "       user_table.firstname, user_table.surname, user_table.profile_img,"
                        + "           user_table.is_active, user_table.gender FROM coac11.user_table"
                        + "WHERE (username ='" + login + "' or email = '" + login + "');";


        DatabaseQuery query = new DatabaseQuery(dbConnection , sqlQuery);

        if(query.getRowCount() >= 1) {
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
