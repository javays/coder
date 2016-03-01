/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder.parser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.ivy.coder.entity.SQLMeta;
import com.ivy.coder.entity.Table;
import com.ivy.coder.entity.TableColumn;
import com.ivy.coder.utils.SQLUtils;
import com.ivy.coder.utils.StringUtils;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-19
 */
public class MySQLScriptParser extends SQLScriptParser {
	
	private static final Logger logger = Logger.getLogger(MySQLScriptParser.class);

    /* 
     * (non-Javadoc)
     * @see com.ivy.coder.parser.SQLScriptParser#findTable(java.lang.String)
     */
    @Override
    public Table findTable(String tableName) {
    	Table table = new Table();
    	table.setTableName(tableName.toUpperCase());
        
        String sql = "select c.COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA from INFORMATION_SCHEMA.columns c where c.TABLE_NAME = ?;";
        
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = SQLUtils.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, tableName.toUpperCase());
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String typeName = rs.getString("DATA_TYPE");
                String columnKey = rs.getString("COLUMN_KEY");
                String extra = rs.getString("EXTRA");
                
                TableColumn column = new TableColumn();
                column.setName(columnName);
                column.setFullName(columnName);
                column.setType(typeName);
                column.setColumnKey(columnKey);
                column.setExtra(extra);
                
                table.addColumn(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            SQLUtils.closeRs(rs);
        }
        
        return table;
    }

    /* (non-Javadoc)
     * @see com.sf.code.creator.SQLScriptAnalyzer#findSQLMeta(java.lang.String)
     */
    @Override
    public SQLMeta parseSql(String sql) {
    	if (StringUtils.isEmpty(sql)) {
			return null;
		}
    	
    	sql = fillCriteria(sql);
    	
        SQLMeta sqlMeta = new SQLMeta();
        
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            connection = SQLUtils.getConnection();
            pstmt = connection.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String columnName = rsmd.getColumnName(i);
                String _columnNameInSql = StringUtils.exist(sql, "\\s+" + columnName + "\\s+");
                if (!StringUtils.isEmpty(_columnNameInSql)) {
                    columnName = _columnNameInSql.trim();
                }
                String typeName = rsmd.getColumnTypeName(i);
                int size = rsmd.getColumnDisplaySize(i);
                int precision = rsmd.getPrecision(i);
                
                TableColumn column = new TableColumn();
                column.setName(columnName);
                column.setFullName(columnName);
                column.setType(typeName);
                column.setLength(size);
                column.setDecimalDigits(precision);
                sqlMeta.addColumn(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally{
            SQLUtils.closeRs(rs);
            SQLUtils.closeStmt(pstmt);
        }
        return sqlMeta;
    }
    
    /* (non-Javadoc)
     * @see com.sf.code.creator.SQLScriptAnalyzer#findSQLMeta(java.lang.String)
     */
    @Override
    public Class<?> getColumnClassType(String type, int size, int precision) {
        if (StringUtils.isEmpty(type)) {
            return null;
        }
        type = type.toLowerCase();
        
        if (type.indexOf("char") != -1 || type.equals("varchar")) {
            return String.class;
        }else if (type.equals("date") || type.equals("datetime")) {
            return Date.class;
        }else if (type.equals("double")) {
            return double.class;
        }else if (type.equals("float")) {
            return float.class;
        }else if (type.indexOf("int") != -1) {
            return int.class;
        }else if (type.indexOf("bigint") != -1) {
            return long.class;
        }else if (type.indexOf("integer") != -1) {
            return int.class;
        }else if (type.indexOf("tinyint") != -1) {
            return short.class;
        }else if (type.indexOf("decimal") != -1) {
            return double.class;
        }else if (type.indexOf("smallint") != -1) {
            return short.class;
        }else if (type.indexOf("timestamp") != -1) {
            return Date.class;
        }else {
        	logger.info("Not support yet ["+ type +"]");
            throw new RuntimeException("Not support yet ["+ type +"]");
        }
    }

	/*
	 * (non-Javadoc)
	 * @see com.ivy.coder.parser.SQLScriptParser#fillCriteria(java.lang.String)
	 */
	public String fillCriteria(String sql) {
		if (StringUtils.isEmpty(sql)) {
			return "";
		}
		
		if (sql.indexOf("limit") != -1) {
			sql = sql.replaceAll("limit\\s+(\\?\\s*,)?\\s*\\?", "limit 1");
		}
		return sql.replaceAll("\\?", "''");
	}
	

	public static void main(String[] args) {
		String sql = "select * from tt_bill_job where created_tm=  ? limit ?";
		
		System.out.println(new MySQLScriptParser().fillCriteria(sql));
	}
}
