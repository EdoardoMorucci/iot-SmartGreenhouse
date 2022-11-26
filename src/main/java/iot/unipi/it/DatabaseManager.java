package iot.unipi.it;

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

    public static void insert_water_level(int water_level, int level_state, String unit, Timestamp ourTimestamp) {
        String sql = "INSERT INTO water_level (water_level, level_state, unit, time_stamp) VALUES(?, ?, ?, ?)";
        try{
            Connection conn = makeConnection();
            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setInt(1, water_level);
            stm.setInt(2, level_state);
            stm.setString(3, unit);
            stm.setTimestamp(4, ourTimestamp);
            stm.executeUpdate();
            conn.close();
        }catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static void insert_pH(float pH, int state_pH, Timestamp ourTimestamp) {
        String sql = "INSERT INTO pH (pH, state_pH, time_stamp) VALUES(?, ?, ?)";
        try{
            Connection conn = makeConnection();
            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setFloat(1, pH);
            stm.setInt(2, state_pH);
            stm.setTimestamp(3, ourTimestamp);
            stm.executeUpdate();
            conn.close();
        }catch (SQLException se) {
            se.printStackTrace();
        }
    }


    public static void print_data(String table, String offset, String limit) {
        String sql="SELECT * FROM "+table+" ORDER BY id DESC "+"LIMIT "+limit+" OFFSET "+offset;
        try {
            Connection conn=makeConnection();
            Statement stm=conn.createStatement();
            print_data2(stm.executeQuery(sql));
            conn.close();
        }catch(SQLException se) {
            se.printStackTrace();
        }
    }

    private static void print_data2(ResultSet rs) {

        try {
            ResultSetMetaData md=rs.getMetaData();
            int n_col=md.getColumnCount();
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
