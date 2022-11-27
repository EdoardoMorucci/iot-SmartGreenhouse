package iot.unipi.it.db;

import java.sql.*;

public class DatabaseManager {

    @SuppressWarnings("finally")
    private static Connection makeConnection() {
        Connection conn = null;

        String dbIP = "localhost";
        String dbPort = "3306";
        String dbUsername = "root";
        String dbPassword = "Password1!";
        String dbName = "sensors";


        //when the object of the class is created the connection is performed
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");//dynamically loads the driver that extends DriverManager
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return conn;
        }

        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://" + dbIP + ":" + dbPort +
                            "/" + dbName + "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=CET",
                    dbUsername,
                    dbPassword);
            //The Driver Manager provides the connection specified in the parameter string
            if (conn == null) {
                System.err.println("connection failed");
            }
        } catch (SQLException e) {
            System.err.println("SQL exception occurs");
            e.printStackTrace();
        }
        finally {
            return conn;
        }

    }

    public static void insert_water_level(int water_level, int level_state, String unit, int ourTimestamp) {
        String sql = "INSERT INTO waterlevel (water_level, level_state, unit, time_stamp) VALUES(?, ?, ?, ?)";
        try{
            Connection conn = makeConnection();
            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setInt(1, water_level);
            stm.setInt(2, level_state);
            stm.setString(3, unit);
            stm.setInt(4, ourTimestamp);
            stm.executeUpdate();
            conn.close();
        }catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static void insert_pH(float pH, int state_pH, int ourTimestamp) {
        String sql = "INSERT INTO ph (pH, pH_state, time_stamp) VALUES(?, ?, ?)";
        try{
            Connection conn = makeConnection();
            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setFloat(1, pH);
            stm.setInt(2, state_pH);
            stm.setInt(3, ourTimestamp);
            stm.executeUpdate();
            conn.close();
        }catch (SQLException se) {
            se.printStackTrace();
        }
    }
    public static void insert_temperature(float temperature, String unit, int ourTimestamp){
        String sql = "INSERT INTO temperature (temperature, unit, time_stamp) VALUES(?, ?, ?)";
        try{
            Connection conn = makeConnection();
            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setFloat(1, temperature);
            stm.setString(2, unit);
            stm.setInt(3, ourTimestamp);
            stm.executeUpdate();
            conn.close();
        }catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static void insert_humidity(int humidity, String unit, int ourTimestamp){
        String sql = "INSERT INTO humidity (humidity, unit, time_stamp) VALUES(?, ?, ?)";
        try{
            Connection conn = makeConnection();
            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setInt(1, humidity);
            stm.setString(2, unit);
            stm.setInt(3, ourTimestamp);
            stm.executeUpdate();
            conn.close();
        }catch (SQLException se) {
            se.printStackTrace();
        }
    }


    public static void print_data(String table, String offset, String limit) {
        String sql = "SELECT * FROM " + table + " ORDER BY id DESC " + "LIMIT " + limit + " OFFSET " + offset;
        try {
            Connection conn = makeConnection();
            Statement stm = conn.createStatement();
            print_out(stm.executeQuery(sql));
            conn.close();
        }catch(SQLException se) {
            se.printStackTrace();
        }
    }

    private static void print_out(ResultSet rs) {

        try {
            ResultSetMetaData md = rs.getMetaData();
            int n_col = md.getColumnCount();
            for(int i=1; i<n_col+1; i++) {
                System.out.print(md.getColumnName(i)+ "\t");
                if(i==2)
                    System.out.print("\t");
            }
            System.out.print("\n");
            while(rs.next()){

                for(int z=1; z<n_col+1; z++) {
                    System.out.print(rs.getObject(z).toString()+"\t");
                }
                System.out.print("\n");
            }
        }catch(SQLException se) {
            se.printStackTrace();
        }
    }




}
