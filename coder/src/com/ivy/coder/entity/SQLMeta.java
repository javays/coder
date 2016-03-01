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
 * 2014-9-19
 */
public class SQLMeta {

    private List<String> tableNames;
    private List<TableColumn> columns;
    private List<TableColumn> criteria;
    
    public boolean addColumn(TableColumn column) {
        if (columns == null) {
            columns = new ArrayList<TableColumn>();
        }
        return columns.add(column);
    }
    
    public List<String> getTableNames() {
        return tableNames;
    }
    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

	public List<TableColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<TableColumn> columns) {
		this.columns = columns;
	}

	public List<TableColumn> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<TableColumn> criteria) {
		this.criteria = criteria;
	}
}
