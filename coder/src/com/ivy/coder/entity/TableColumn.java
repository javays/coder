/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder.entity;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-18
 */
public class TableColumn {

    private String name;
    private String fullName;
    private String type;
    private int length;
    private int decimalDigits;
    private String columnKey;
    private String extra;
    
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public int getDecimalDigits() {
        return decimalDigits;
    }
    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }
	public String getColumnKey() {
		return columnKey;
	}
	public String getExtra() {
		return extra;
	}
	public void setColumnKey(String columnKey) {
		this.columnKey = columnKey;
	}
	public void setExtra(String extra) {
		this.extra = extra;
	}
}
