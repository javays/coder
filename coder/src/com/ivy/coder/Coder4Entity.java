/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ivy.coder.entity.FieldMeta;
import com.ivy.coder.entity.SQLMeta;
import com.ivy.coder.entity.Table;
import com.ivy.coder.entity.TableColumn;
import com.ivy.coder.parser.MySQLScriptParser;
import com.ivy.coder.parser.OracleSQLScriptParser;
import com.ivy.coder.parser.SQLScriptParser;
import com.ivy.coder.utils.StringUtils;
import com.ivy.coder.utils.SysProps;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-17
 */
public class Coder4Entity extends CoderHelper {
	
	private SQLScriptParser scriptParser = null;
	
	public Coder4Entity() {
		if (SysProps.getDb_type().equals(SysProps.DbType.mysql.name())) {
			scriptParser = new MySQLScriptParser();
		} else if (SysProps.getDb_type().equals(SysProps.DbType.oracle.name())) {
			scriptParser = new OracleSQLScriptParser();
		}
	}
	
	/**
	 * 创建JDO代码
	 * @param pck
	 * @param jdoName
	 * @param sql
	 * @return
	 */
	public String createJdoCode(String jdoName, String sql) {
		SQLMeta sqlMeta = scriptParser.parseSql(sql);
		List<FieldMeta> fieldMetas = scriptParser.getFieldMetas(sqlMeta.getColumns());
		return createJdoCode(jdoName, fieldMetas);
	}
	
	/**
	 * 创建JDO代码
	 * @param pck
	 * @param jdoName
	 * @param fieldMetas
	 * @return
	 */
	public String createJdoCode(String jdoName, List<FieldMeta> fieldMetas) {
		if (fieldMetas == null || fieldMetas.size() == 0) {
            return "";
        }
        
        StringBuffer headerBuffer = new StringBuffer();
        Set<String> importClass = new HashSet<String>();
        StringBuffer fieldBuffer = new StringBuffer();
        StringBuffer getterSetterBuffer = new StringBuffer();
        
        headerBuffer.append("package ").append(SysProps.getProject_pck_jdo()).append(";\n\n");
        
        for (FieldMeta fieldMeta : fieldMetas) {
            String fieldName = fieldMeta.getName();
            Class<?> fieldType = fieldMeta.getType();
            
            String simpleClassName = fieldType.getSimpleName();
            String className = fieldType.getName();
            if (!className.startsWith("java.lang.") && 
                    !(simpleClassName.charAt(0) >= 97 && simpleClassName.charAt(0) <= 122)) {
            	importClass.add(className);
            }
            
            String fieldCode = "\tprivate " + simpleClassName + " " + fieldName + ";\n\n";
            fieldBuffer.append(fieldCode);
            
            String getter = "\tpublic " + simpleClassName 
                        + " get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) + "() {\n"
                        + "\t\treturn " + fieldName + ";\n"
                        + "\t}\n";
            String setter = "\tpublic void" + " set" 
                        + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) 
                        + "("+ simpleClassName + " " + fieldName +") {\n"
                    + "\t\tthis." + fieldName + "=" + fieldName + ";\n"
                    + "\t}\n";
            
            getterSetterBuffer.append(getter).append(setter);
        }
        
        importClass.forEach(e -> {
        	headerBuffer.append("import " + e + ";\n");
        });
        headerBuffer.append("\n").append("public class " + jdoName + " {\n\n");
        
        return headerBuffer + fieldBuffer.toString() + "\n" + getterSetterBuffer.toString() + "}";
	}
	
	/**
	 * 创建PO代码
	 * @param pck
	 * @param jdoName
	 * @param sql
	 * @return
	 */
	public String createPoCode(String entityName, String tableName) {
		Table table = scriptParser.findTable(tableName);
		return createPoCode(entityName, table);
	}

	/**
	 * 创建PO代码
	 * @param pck
	 * @param entityName
	 * @param fieldMetas
	 * @return
	 */
    public String createPoCode(String entityName, Table table) {
        if (table == null) {
            return "";
        }
        
        StringBuffer headerBuffer = new StringBuffer("package ").append(SysProps.getProject_pck_entity()).append(";\n\n");
        Set<String> importClass = new HashSet<String>();
        StringBuffer fieldBuffer = new StringBuffer();
        StringBuffer getterSetterBuffer = new StringBuffer();
        
        List<FieldMeta> fieldMetas = scriptParser.getFieldMetas(table.getColumns());
        for (int i=0; i<fieldMetas.size(); i++) {
        	FieldMeta fieldMeta = fieldMetas.get(i);
            String fieldName = fieldMeta.getName();
            Class<?> fieldType = fieldMeta.getType();
            
            String simpleClassName = fieldType.getSimpleName();
            String className = fieldType.getName();
            if (!className.startsWith("java.lang.") && 
                    !(simpleClassName.charAt(0) >= 97 && simpleClassName.charAt(0) <= 122)) {
            	importClass.add(className);
            }
            
            String fieldCode = "";
            
            TableColumn tableColumn = table.getColumns().get(i);
            if ("PRI".equals(tableColumn.getColumnKey())) {
            	fieldCode += "\t@Id\n";
			}
            if ("auto_increment".equals(tableColumn.getExtra())) {
            	fieldCode += "\t@GeneratedValue(strategy=GenerationType.AUTO)\n";
			}
            
            fieldCode += "\t@Column(name=\"" + fieldMeta.getSimpleColumnName() + "\")\n";
            fieldCode += "\tprivate " + simpleClassName + " " + fieldName + ";\n\n";
            fieldBuffer.append(fieldCode);
            
            String getter = "\tpublic " + simpleClassName 
                        + " get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) + "() {\n"
                        + "\t\treturn " + fieldName + ";\n"
                        + "\t}\n";
            String setter = "\tpublic void" + " set" 
                        + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) 
                        + "("+ simpleClassName + " " + fieldName +") {\n"
                    + "\t\tthis." + fieldName + "=" + fieldName + ";\n"
                    + "\t}\n";
               
            
            getterSetterBuffer.append(getter).append(setter);
        }
        
        importClass.add("javax.persistence.Column");
        importClass.add("javax.persistence.Entity");
        importClass.add("javax.persistence.GeneratedValue");
        importClass.add("javax.persistence.GenerationType");
        importClass.add("javax.persistence.Id");
        importClass.add("javax.persistence.Table");
        
        importClass.forEach(e -> {
        	headerBuffer.append("import " + e + ";\n");
        });
        headerBuffer.append("\n@Entity")
        .append("\n@Table(name = \""+ table.getTableName() +"\")")
        .append("\npublic class " + entityName + " {\n\n");
        
        return headerBuffer + fieldBuffer.toString() + "\n" + getterSetterBuffer.toString() + "}";
    }
    
    public static String createField(String str) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        String[] fields = str.split("\\s+");
        String field = "";
        pw.print("<waybill");
        for (String string : fields) {
            field += string.trim();
            if (field.startsWith("\"") && !field.endsWith("\"")) {
                continue;
            }
            if (field.startsWith("\"") && field.endsWith("\"")) {
                field = field.substring(1, field.length() -1);
            }
            String fieldName = Translator.translate(field);
            fieldName = StringUtils.getFieldName(fieldName, ' ');
            fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
//            System.out.printf("private %s %s;     //%s \n", type, fieldName, field);
            pw.printf(" %s='%s'", fieldName, fieldName);
            field = "";
        }
        pw.println("/>");
        pw.flush();
        
        return baos.toString();
    }
    
    public static void main(String[] args) {
        String string = 
                "序号\t日期\t订单号码  运单号码  寄件地区  寄件公司名称   对方地区   对方客户名称   \"计费重量\n" +
                        "(公斤) \"  付款方式  \"费用\n" + 
                        "（元）\"  单票折扣  应收费用  产品类型  费用类型  订单类型  订单来源  始发地(省名)   寄件人联系手机号  寄件人联系座机号  寄件地址   收件员工号   客户网络代码  客户网络名称  客户发货仓库  托寄物名称  托寄物数量   声明价值   件数  \"轻抛尺寸\n" + 
                        "（长×宽×高）\"  目的地省名  目的地城市名  客户区域代码  邮政编码  收件人   收件人联系手机号  收件人联系座机号  收件地址   派件员工号   实际重量  签回单单号   回单签收人  签收人   签收时间   退件名称  退回件关联原运单号   退件签收人  备注";

        System.out.println(createField(string));
        System.out.println(new Coder4Entity().createPoCode("TTBilPolicyWaybill", "tt_bil_policy_waybill"));
    }
    
}
