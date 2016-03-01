/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder.parser;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
public class OracleSQLScriptParser extends SQLScriptParser {
	
	private static final Logger logger = Logger.getLogger(OracleSQLScriptParser.class);

    /* (non-Javadoc)
     * @see com.sf.code.creator.SQLScriptAnalyzer#findTable(java.lang.String)
     */
    @Override
    public Table findTable(String tableName) {
        Table table = new Table();
        table.setTableName(tableName.toUpperCase());
        
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = SQLUtils.getConnection();
            DatabaseMetaData ddmd = connection.getMetaData();
            rs = ddmd.getColumns(null, null, table.getTableName(), "%");
            
            boolean b = false;
            while (rs.next()) {
                if (!b) {
                    table.setDbName(rs.getString("TABLE_SCHEM"));
                }
                /*System.out.print(rs.getString("TABLE_SCHEM") + "\t");
                System.out.print(rs.getString("TABLE_NAME") + "\t");
                System.out.print(StringUtils.rightPad(rs.getString("COLUMN_NAME"), 30, ' ') + "\t");
                System.out.print(StringUtils.rightPad(rs.getString("TYPE_NAME"), 8, ' ') + "\t");
                System.out.print(rs.getString("DATA_TYPE") + "\t");
                System.out.print( rs.getString("COLUMN_SIZE") + "\t");
                System.out.print(rs.getString("DECIMAL_DIGITS") + "\t");
                System.out.print(rs.getString("SQL_DATA_TYPE") + "\t");
                System.out.print(rs.getString("IS_NULLABLE") + "\t");
                System.out.println("");*/
                
                String columnName = rs.getString("COLUMN_NAME");
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                
                TableColumn column = new TableColumn();
                column.setName(columnName);
                column.setFullName(columnName);
                column.setType(typeName);
                column.setLength(columnSize);
                column.setDecimalDigits(decimalDigits);
                
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
        if (type.indexOf("CHAR") != -1 || type.equals("CLOB")) {
            return String.class;
        }else if (type.equals("DATE")) {
            return Date.class;
        }else if (type.equals("NUMBER")) {
            if (precision == 0) {
                if (size >= 19) {
                    return long.class;
                }else {
                    return int.class;
                }
            }else {
                return double.class;
            }
        }else if (type.equals("FLOAT")) {
            return double.class;
        }else {
        	logger.error("Not support yet ["+ type +"]");
            throw new RuntimeException("Not support yet ["+ type +"]");
        }
    }

	@Override
	public String fillCriteria(String sql) {
		return sql.replaceAll("\\?", "''");
	}
}
