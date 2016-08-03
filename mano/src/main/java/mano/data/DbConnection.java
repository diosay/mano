/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.data;

import java.sql.*;

/**
 *
 * @author junhwong
 */
public class DbConnection {
    private String driver="com.mysql.jdbc.Driver";
    private String url="jdbc:mysql://127.0.0.1/ecp?zeroDateTimeBehavior=convertToNull";
    private String username="root";
    private String password="rootroot";
    private Connection connection;
    public void connect() throws ClassNotFoundException, SQLException{
        Class.forName(driver);
        connection=DriverManager.getConnection(url, username, password);
        
        Statement s=connection.createStatement();
        s.close();
        connection.close();
        ResultSet set=s.executeQuery("");
        
    }
}
