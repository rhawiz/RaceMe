package com.example.rawand.raceme;

/**
 * Created by RAWAND on 29/11/2014.
 */
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

    DatabaseQuery(DatabaseConnection db, String query){
        this.sqlQuery = query;
        this.db = db;
        run();

    }

    private void run(){
        try {
            statement = db.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(sqlQuery);
            columnCount = resultSet.getMetaData().getColumnCount();
            resultSet.last();
            rowCount = resultSet.getRow();
            constructArray();
        } catch (SQLException e1) {
            e1.printStackTrace();
        } catch(NullPointerException e2){
            e2.printStackTrace();
        }


        //TODO:Find method of ensuring db connection gets closed.
        //db.close();

    }

    public int getRowCount(){
        return rowCount;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public ArrayList getArray(){
        return resultsArray;
    }

    private void constructArray() throws SQLException{
        resultSet.first();
        resultsArray = new ArrayList<ArrayList>();
        for(int i=0;i<rowCount;i++){
            ArrayList temp_array = new ArrayList();
            for(int j=1;j<=columnCount;j++){
                temp_array.add(resultSet.getString(resultSet.getMetaData().getColumnName(j)));
            }
            resultSet.next();
            resultsArray.add(temp_array);
        }
    }

    public String get(int row, int col){
        if(row < rowCount && col < columnCount){
            return (String) resultsArray.get(row).get(col);
        }
        return null;
    }

    public ArrayList get(int row){
        return resultsArray.get(row);
    }




}
