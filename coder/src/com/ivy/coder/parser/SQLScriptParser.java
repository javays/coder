/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */

package com.ivy.coder.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ivy.coder.entity.FieldMeta;
import com.ivy.coder.entity.SQLMeta;
import com.ivy.coder.entity.Table;
import com.ivy.coder.entity.TableColumn;
import com.ivy.coder.utils.StringUtils;
import com.ivy.coder.utils.SysProps;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-19
 */
public abstract class SQLScriptParser {
	
	private static final Pattern PARAM_EXP = Pattern.compile("(\\s+[a-zA-z_.]+\\s*[=<>]{1,2})?\\s*\\?");

    /**
     * 获取表元数据信息
     * @param tableName 表名
     * @return
     */
    public abstract Table findTable(String tableName);
    
    /**
     * 获取SQL语句元数据信息
     * @param sql 
     * @return
     */
    public abstract SQLMeta parseSql(String sql);
    
    /**
     * 获取代?条件名称
     * @param sql
     * @return
     */
    public static List<String> getCriteriaNames(String sql) {
		if (StringUtils.isEmpty(sql)) {
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		
		Matcher matcher = PARAM_EXP.matcher(sql);
		while (matcher.find()) {
			String find = matcher.group();
			String[] split = find.trim().split("\\s*[=<>]{1,2}\\s*");
			String criteria = split[0];
			criteria = criteria.indexOf(".")>0 ? criteria.substring(criteria.indexOf(".")+1) : criteria;
			result.add(criteria);
		}
		
		return result;
	}
    
    /**
     * 填充SQL，将?填充为相应类型值
     * @param sql
     * @return
     */
    public abstract String fillCriteria(String sql);
    
    /**
     * 获取对应java数据类型
     * @param dbType
     * @param columnSize
     * @param decimalDigits
     * @return
     */
    public abstract Class<?> getColumnClassType(String type, int size, int precision); 
    

    /**
     * 表列对应为java类属性
     * @param tableColumns
     * @return
     */
    public List<FieldMeta> getFieldMetas(List<TableColumn> tableColumns) {
        if (tableColumns == null || tableColumns.size() == 0) {
            return null;
        }
        
        List<FieldMeta> fieldMetas = new ArrayList<FieldMeta>(tableColumns.size());
        for (TableColumn tc : tableColumns) {
            FieldMeta fieldMeta = new FieldMeta();
            fieldMeta.setColumnName(tc.getFullName());
            fieldMeta.setSimpleColumnName(tc.getName());
            
            Class<?> classType = getColumnClassType(tc.getType(),tc.getLength(), tc.getDecimalDigits());
            fieldMeta.setType(classType);
            fieldMeta.setName(StringUtils.getFieldName(tc.getName().toLowerCase(), SysProps.LINK_CHAR));
            
            fieldMetas.add(fieldMeta);
        }
        return fieldMetas;
    }

}
