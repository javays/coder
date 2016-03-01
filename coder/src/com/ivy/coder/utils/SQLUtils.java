/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-18
 */
public class SQLUtils {

    private static Connection connection;
    
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName(SysProps.getJdbc_class());
                connection = DriverManager.getConnection(SysProps.getJdbc_url(), 
                                                        SysProps.getDb_user(), 
                                                        SysProps.getDb_pass());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(-1);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(-2);
            }
        }
        
        return connection;
    }
    
    public static void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void closeStmt(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void closeRs(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
