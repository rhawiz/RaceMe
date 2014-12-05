package com.example.rawand.raceme;

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

    public static HashMap<String,Object> friendsGetUsers(String userId){

        DatabaseConnection dbConnection = null;

        HashMap outputMap = new HashMap();

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
        String sqlQuery ="SELECT user_table.id, user_table.username, user_table.email, user_table.firstname, user_table.surname, user_table.profile_img,user_table.gender FROM coac11.user_table WHERE (user_table.id <> " + userId + ") AND" +
                            " user_table.id NOT IN (SELECT user_1_id from coac11.friends_table WHERE user_2_id = " + userId + ") AND" +
                            " user_table.id NOT IN (SELECT user_2_id from coac11.friends_table WHERE user_1_id = " + userId + ")" +
                            " AND user_table.is_active = 1; ";

        if(dbConnection == null) {
            return outputMap;
        }

        DatabaseQuery dbQuery = new DatabaseQuery(dbConnection, sqlQuery);

        if(!dbQuery.run()){
            return outputMap;
        }

        String[] displayList = new String[dbQuery.getRowCount()];
        User[] userDetailList= new User[dbQuery.getRowCount()];

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



            displayList[i] = (String) currentRow.get(1) + " (" + currentRow.get(3)+" "+currentRow.get(4) + ")";
            userDetailList[i] = user;
        }

        outputMap.put("displayList",displayList);
        outputMap.put("detailList",userDetailList);

        return outputMap;

    }

    public static ArrayList<String> getFriendListSimple(String id){
        String sqlQuery = "(SELECT username, firstname, surname FROM coac11.user_table" +
                " WHERE user_table.id" +
                " IN ((SELECT friends_table.user_1_id AS ABC FROM coac11.friends_table WHERE friends_table.user_2_id = " + id + ") ))" +
                " UNION" +
                " (SELECT username, firstname, surname FROM coac11.user_table" +
                " WHERE user_table.id" +
                " IN ((SELECT friends_table.user_2_id AS ABC FROM coac11.friends_table WHERE friends_table.user_1_id = " + id + ")));";


        DatabaseConnection dbConnection = null;
        ArrayList friendsArray = new ArrayList();
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
            friendsArray.add(dbQuery.get(i,0) + " (" + dbQuery.get(i,1) + " " + dbQuery.get(i,2) + ")");
        }
        return friendsArray;
    }

    public static boolean sendFriendRequest(String userSenderId, String userReceiverId){
        String sqlQuery = "INSERT INTO `coac11`.`friends_table` (`" + userSenderId + "`, `" + userReceiverId + "`) VALUES ('1', '2');";

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
            dbQuery.get(0,0);
            return true;
        }
        return false;
    }

}
