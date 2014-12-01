package com.example.rawand.raceme;


import java.sql.SQLException;

/**
 * Created by RAWAND on 29/11/2014.
 * This class will handle the authentication process when the user attempts to log in.
 * If the user is authorised, it will allow a user object to be obtained containing user details.
 */


public class Authentication {

    private DatabaseConnection dbConnection;
    private User userData;

    public Authentication(){

    }

    public boolean connect(){
        try {
            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
            return true;
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (SQLException e2){
            e2.printStackTrace();
        }
        return false;
    }

    public void close(){
        dbConnection.close();
    }

    public Boolean authenticate(String login, String password){

        String sql_query =
                "SELECT user_table.id, user_table.username, user_table.email,"
                + "       user_table.firstname, user_table.surname, user_table.profile_img,"
                + "           user_table.is_active, user_table.gender FROM coac11.user_table"
                + " WHERE (username ='" + login + "' or email = '" + login + "') AND password COLLATE utf8_bin = '" + password + "';";

        DatabaseQuery query = new DatabaseQuery(dbConnection , sql_query);

        if(query.getRowCount() >= 1){
            String user_id = query.get(0,0);
            String username = query.get(0,1);
            String email= query.get(0,2);
            String firstname= query.get(0,3);
            String surname= query.get(0,4);
            String profile_img= query.get(0,5);
            String is_active= query.get(0,6);
            String gender = query.get(0,7);

            userData = new User(user_id,username,email, firstname,
                                        surname, profile_img, is_active, gender);
            return true;
        }
        return false;
    }

    public User getUser(){
        return userData;
    }




}
