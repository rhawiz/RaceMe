package com.example.rawand.raceme;

/**
 * Created by RAWAND on 29/11/2014.
 *
 * Data structure to perform database connection activities and store connection states.
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    //Only supports MySql for time being.

    private String url;
    private String username;
    private String password;
    private Connection connect;

    DatabaseConnection(String url, String username, String password) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        this.url = url;
        this.username = username;
        this.password = password;
        connect();
    }

    public void close(){
        try {
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void connect() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        connect = DriverManager.getConnection("jdbc:mysql://"+url,username,password);


    }

    public Connection getConnection(){

        return connect;

    }

}
