/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder.entity;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-17
 */
public class FieldMeta {

    private String name;
    private String columnName;
    private String simpleColumnName;
    private Class<?> type;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Class<?> getType() {
        return type;
    }
    public void setType(Class<?> type) {
        this.type = type;
    }
    public String getSimpleColumnName() {
        return simpleColumnName;
    }
    public void setSimpleColumnName(String simpleColumnName) {
        this.simpleColumnName = simpleColumnName;
    }
    public String getColumnName() {
        return columnName;
    }
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    @Override
    public String toString() {
        return "FieldMeta [name=" + name
                + ", columnName="
                + columnName
                + ", simpleColumnName="
                + simpleColumnName
                + ", type="
                + type
                + "]";
    }
}
