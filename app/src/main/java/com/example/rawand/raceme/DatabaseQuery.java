package com.example.rawand.raceme;

/**
 * Created by RAWAND on 29/11/2014.
 *
 * Database Query class handle database queries and store data retrieved from the database if applicable.
 */
import android.util.Log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class DatabaseQuery {


    String sqlQuery;
    DatabaseConnection db;
    ResultSet resultSet;
    Statement statement;
    int columnCount;
    int rowCount;
    private ArrayList<ArrayList> resultsArray;
    String queryType;

    DatabaseQuery(DatabaseConnection db, String query){
        this.sqlQuery = query;
        this.db = db;

        queryType = query.trim().split(" ")[0];

    }


    /**
     * Run query on database connection.
     *
     * @return True if successfully queried database and false if it fails for any reason.
     */
    public boolean run(){
        if(queryType.equals("INSERT") ||queryType.equals("UPDATE")) {
            try {
                statement = db.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                statement.executeUpdate(sqlQuery);
                columnCount = 0;
                rowCount = 0;
                constructArray();
            } catch (SQLException e1) {
                close();
                e1.printStackTrace();
                return false;
            } catch (NullPointerException e2) {
                close();
                e2.printStackTrace();
                return false;
            }
        }
        else{
            try {
                statement = db.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                resultSet = statement.executeQuery(sqlQuery);
                columnCount = resultSet.getMetaData().getColumnCount();
                resultSet.last();
                rowCount = resultSet.getRow();
                constructArray();
            } catch (SQLException e1) {
                close();
                e1.printStackTrace();
                return false;
            } catch (NullPointerException e2) {
                close();
                e2.printStackTrace();
                return false;
            }
        }


        //TODO:Find method of ensuring db connection gets closed.
        close();
        return true;

    }


    /**
     * Close the database connection
     */
    public void close(){
        if(db!=null)
            db.close();
    }


    /**
     * Get the number of rows the query returned from the database
     *
     * @return Integer number of rows
     */
    public int getRowCount(){
        return rowCount;
    }

    /**
     * Get the number of columns returned from the database.
     *
     * @return Integer number of columns
     */
    public int getColumnCount(){
        return columnCount;
    }


    /**
     * Return the sql query that was run
     *
     * @return String sql Query
     */
    public String getSqlQuery() {
        return sqlQuery;
    }


    /**
     * Return the query results as an ArrayList
     * @return
     */
    public ArrayList getArray(){
        return resultsArray;
    }


    /**
     * Construct the result arraylist
     * @throws SQLException
     */
    private void constructArray() throws SQLException{
        resultsArray = new ArrayList<ArrayList>();
        if(rowCount > 0) {
        //Only construct array if the query is expecting results. I.E. SELECT only.
            resultSet.first();
            for (int i = 0; i < rowCount; i++) {
                ArrayList temp_array = new ArrayList();
                for (int j = 1; j <= columnCount; j++) {
                    temp_array.add(resultSet.getString(resultSet.getMetaData().getColumnName(j)));
                }
                resultSet.next();
                resultsArray.add(temp_array);
            }
        }
    }

    /**
     * Get results by row and column
     *
     * @param row Row
     * @param col Column
     * @return String result at row, col
     */
    public String get(int row, int col){
        if(row < rowCount && col < columnCount){
            return (String) resultsArray.get(row).get(col);
        }
        return null;
    }


    /**
     * Overload function, get indicated row as ArrayList
     * @param row Row
     * @return ArrayList row
     */
    public ArrayList get(int row){
        return resultsArray.get(row);
    }




}
