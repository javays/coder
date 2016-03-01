/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-18
 */
public class Table {

    private String dbName;
    private String tableName;
    private List<TableColumn> columns;
    
    public boolean addColumn(TableColumn column) {
        if (columns == null) {
            columns = new ArrayList<TableColumn>();
        }
        return columns.add(column);
    }
    
    public String getDbName() {
        return dbName;
    }
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

	public List<TableColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<TableColumn> columns) {
		this.columns = columns;
	}

}
