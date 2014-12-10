package com.example.rawand.raceme;

import android.util.Log;

import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by RAWAND on 04/12/2014.
 *
 * Static class to perform general database related tasks.
 * These must be called in a threaded task.
 */
public class DatabaseHelper {


    /**
     * Get an active registered users details.
     *
     * @param userId
     * @return User object containing user details. User object will be empty if query failed.
     */
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


    /**
     * Get an arrayList of user objects that are NOT friends with the specified user.
     *
     * @param userId current users
     * @return ArrayList of User objects
     */
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


    /**
     * Get an ArrayList of User objects that are friends with a specified user.
     * @param id current user
     * @return ArrayList of User objects
     */
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

    /**
     * Send a friend request and update database accordingly.
     * @param userSenderId User id that is sending the friend request
     * @param userReceiverId User id that should receive the friend request.
     * @return True if successfully queried the database, false if failed.
     */

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


    /**
     * Overload function of sendFriendRequest taking User objects instead of Id's.
     * @param userSenderId User object sending friend request.
     * @param userReceiverId User object receiving friend request.
     * @return True if successfully queried the database, false if failed.
     */

    public static boolean sendFriendRequest(User userSenderId, User userReceiverId){
        return sendFriendRequest(userSenderId.getUserId(),userReceiverId.getUserId());
    }


    /**
     * Get a list of User objects that have sent the specified user friend requests.
     * @param userId the user Id.
     * @return ArrayList of user Objects
     */
    public static ArrayList<User> getFriendRequestUsers(String userId){
        String sqlQuery = "SELECT user_table.id, user_table.username, user_table.email, user_table.firstname, user_table.surname, user_table.profile_img, user_table.IS_ACTIVE, user_table.gender "+
        " FROM coac11.user_table WHERE id IN ("+
                " SELECT user_1_id"+
        " FROM coac11.friends_table WHERE user_2_id = " + userId +" AND accepted = 0);";


        ArrayList<User> userList = new ArrayList<User>();

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

        if(!dbQuery.run()){
            return userList;
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
                    (String) currentRow.get(6),
                    (String) currentRow.get(7)
            );
            userList.add(user);
        }

        return userList;

    }


    /**
     * Accept a friend request and update database accordingly.
     * @param senderId Original sender Id
     * @param receiverId Original receiver Id.
     * @return True if successfully queried database, false if failed.
     */

    public static boolean acceptFriendRequest(String senderId, String receiverId){
        String sqlQuery = "UPDATE coac11.friends_table SET accepted=1 WHERE (user_1_id= " + senderId + " AND user_2_id = " + receiverId + ");";

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

    /**
     * Overloaded method of acceptFriendRequest taking User objects instead
     * @param senderUser Original sender User object
     * @param receiverUser Original receiver User object.
     * @return True if successfully queried database, false if failed.
     */
    public static boolean acceptFriendRequest(User senderUser, User receiverUser){
        return acceptFriendRequest(senderUser.getUserId(),receiverUser.getUserId());
    }


    /**
     * Update a user with new values
     *
     * @param id User id
     * @param email User email
     * @param firstname User firstname
     * @param surname User surname
     * @param profileImg User profile img
     * @param gender User gender
     * @return True if successfully queried database, false if failed.
     */
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


    /**
     * Get a list of races a specified user has done.
     * @param userId User id
     * @return ArrayList of RaceSession objects
     */
    public static ArrayList<RaceSession> getUserRaces(String userId){

        String sqlQuery = "SELECT user_id, race_type,json_gps_coords,start_time,end_time FROM coac11.race_log_table WHERE race_log_table.user_id = 1;";


        ArrayList<RaceSession> raceSessionList = new ArrayList<RaceSession>();

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

        if(!dbQuery.run()){
            return raceSessionList;
        }

        for (int i = 0; i < dbQuery.getRowCount(); i++) {

            ArrayList currentRow = dbQuery.get(i);


            RaceSession currentRaceSession = new RaceSession(
                    (String) currentRow.get(0),
                    (String) currentRow.get(1),
                    Utilities.toLocationArray((String[]) new Gson().fromJson(((String) currentRow.get(2)), String[].class) ),
                    Utilities.getDateFromString(((String) currentRow.get(3)).substring(0,((String) currentRow.get(3)).length()-2)),
                    Utilities.getDateFromString(((String) currentRow.get(4)).substring(0,((String) currentRow.get(4)).length()-2))
            );
            raceSessionList.add(currentRaceSession);
        }

        return raceSessionList;
    }


    /**
     * Get the list of achievements that the user has completed.
     * @param userId user id
     * @return ArrayList of Strings specifying completed achievements.
     */
    public static ArrayList<String> getUserAcheivement( String userId ){

        String sqlQuery = "SELECT challenges_table.challenge_source" +
                " FROM coac11.user_challenge_table" +
                " LEFT JOIN coac11.challenges_table on user_challenge_table.challenge_id = challenges_table.challenge_id" +
                " WHERE user_id =" + userId;

        ArrayList<String> outputArrayList = new ArrayList<String>();



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

        if(!dbQuery.run()){
            return null;
        }
        for (int i = 0; i < dbQuery.getRowCount(); i++) {
            ArrayList currentRow = dbQuery.get(i);

            outputArrayList.add((String) currentRow.get(0));


        }

        return outputArrayList;


    }

    public static User getUserDetails(String id){
        String sqlQuery = "SELECT id, username, email, firstname, surname, profile_img, IS_ACTIVE, gender FROM coac11.user_table WHERE id = " + id;

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

        if(!dbQuery.run()){
            return new User();
        }


        if(dbQuery.getRowCount()== 0) {
            return new User();
        }


        return new User(dbQuery.get(0,0),dbQuery.get(0,1),dbQuery.get(0,2),dbQuery.get(0,3),dbQuery.get(0,4),dbQuery.get(0,5),dbQuery.get(0,6),dbQuery.get(0,7));


    }






}
