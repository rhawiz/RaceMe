package com.example.rawand.raceme;

import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by RAWAND on 04/12/2014.
 */
public class DatabaseHelper {

    public static User getActiveUserDetails(String userId){
        String sqlQuery = "SELECT id,username,email,firstname,surname,profile_img,gender FROM coac11.user_table WHERE id = " +  userId +" AND is_active = 1;";
        DatabaseConnection dbConnection = null;

        try {
            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return new User();
        } catch (SQLException e) {
            e.printStackTrace();
            return new User();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return new User();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new User();
        }

        DatabaseQuery dbQuery = new DatabaseQuery(dbConnection, sqlQuery);

        if(!dbQuery.run()){
            return new User();
        }

        User user = new User(dbQuery.get(0,0),dbQuery.get(0,1),dbQuery.get(0,2),dbQuery.get(0,3),dbQuery.get(0,4),dbQuery.get(0,5),"1",dbQuery.get(0,6));
        return user;

    }

    public static ArrayList<User> getNotFriendsList(String userId){

        DatabaseConnection dbConnection = null;

        ArrayList<User> outputList = new ArrayList<User>();

        try {
            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        String sqlQuery ="SELECT user_table.id, user_table.username, user_table.email, user_table.firstname, user_table.surname, user_table.profile_img,user_table.gender FROM coac11.user_table WHERE (user_table.id <> " + userId + ") AND\n" +
                            " user_table.id NOT IN (SELECT user_1_id from coac11.friends_table WHERE user_2_id = " + userId + " AND friends_table.accepted = '1') AND\n" +
                            " user_table.id NOT IN (SELECT user_2_id from coac11.friends_table WHERE user_1_id = " + userId + " AND friends_table.accepted = '1')\n" +
                            " AND user_table.is_active = 1;";
        if(dbConnection == null) {
            return outputList;
        }

        DatabaseQuery dbQuery = new DatabaseQuery(dbConnection, sqlQuery);

        if(!dbQuery.run()){
            return outputList;
        }

        for (int i = 0; i < dbQuery.getRowCount(); i++) {

            ArrayList currentRow = dbQuery.get(i);
            User user = new User(
                    (String) currentRow.get(0),
                    (String) currentRow.get(1),
                    (String) currentRow.get(2),
                    (String) currentRow.get(3),
                    (String) currentRow.get(4),
                    (String) currentRow.get(5),
                    "1",
                    (String) currentRow.get(6)
            );

            outputList.add(user);

        }

        return outputList;

    }

    public static ArrayList<User> getFriendsList(String id){
        String sqlQuery = "(SELECT user_table.id, user_table.username, user_table.email, user_table.firstname, user_table.surname, user_table.profile_img,user_table.gender FROM coac11.user_table" +
                " WHERE user_table.id" +
                " IN ((SELECT friends_table.user_1_id AS ABC FROM coac11.friends_table WHERE friends_table.user_2_id = " + id + " AND friends_table.accepted='1') ))" +
                " UNION" +
                " (SELECT user_table.id, user_table.username, user_table.email, user_table.firstname, user_table.surname, user_table.profile_img,user_table.gender FROM coac11.user_table" +
                " WHERE user_table.id" +
                " IN ((SELECT friends_table.user_2_id AS ABC FROM coac11.friends_table WHERE friends_table.user_1_id = " + id + " AND friends_table.accepted='1')));";

        DatabaseConnection dbConnection = null;
        ArrayList<User> friendsArray = new ArrayList<User>();
        try {
            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        DatabaseQuery dbQuery = new DatabaseQuery(dbConnection, sqlQuery);

        if(!dbQuery.run()){
            return friendsArray;
        }

        for (int i = 0; i < dbQuery.getRowCount(); i++) {
            ArrayList currentRow = dbQuery.get(i);
            User user = new User(
                    (String) currentRow.get(0),
                    (String) currentRow.get(1),
                    (String) currentRow.get(2),
                    (String) currentRow.get(3),
                    (String) currentRow.get(4),
                    (String) currentRow.get(5),
                    "1",
                    (String) currentRow.get(6)
            );
            friendsArray.add(user);
        }
        return friendsArray;
    }

    public static boolean sendFriendRequest(String userSenderId, String userReceiverId){
        String sqlQuery = "INSERT INTO `coac11`.`friends_table` (`user_1_id`, `user_2_id`) VALUES ('" + userSenderId + "', '" + userReceiverId + "');";
        DatabaseConnection dbConnection = null;
        try {
            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        DatabaseQuery dbQuery = new DatabaseQuery(dbConnection, sqlQuery);

        if(dbQuery.run()){
            return true;
        }
        return false;
    }

    public static boolean sendFriendRequest(User userSenderId, User userReceiverId){
        return sendFriendRequest(userSenderId.getUserId(),userReceiverId.getUserId());
    }


    public static ArrayList<User> getFriendRequestUsers(String userId){
        return new ArrayList<User>();
    }

    public static boolean updateUser(String id, String email, String firstname, String surname, String profileImg, String gender){
        String sqlQuery = "UPDATE `coac11`.`user_table` SET `email`='" + email + "', `firstname`='" + firstname + "', `surname`='" + surname + "', `profile_img`='" + profileImg + "', `gender`='" + gender + "' WHERE `id`='" + id + "';";

        DatabaseConnection dbConnection = null;

        try {
            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        DatabaseQuery dbQuery = new DatabaseQuery(dbConnection, sqlQuery);

        if(dbQuery.run()){
            return true;
        }
        return false;
    }

}
