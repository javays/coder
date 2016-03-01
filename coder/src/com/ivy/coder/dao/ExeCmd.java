package com.ivy.coder.dao;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.log4j.Logger;

import com.ivy.coder.Params;
import com.ivy.coder.entity.FieldMeta;
import com.ivy.coder.parser.SQLScriptParser;
import com.ivy.coder.utils.CustomClassLoader;
import com.ivy.coder.utils.StringUtils;
import com.ivy.coder.utils.SysProps;

public class ExeCmd {
	
	private static final Logger logger = Logger.getLogger(ExeCmd.class);
	
	/**
	 * 判断是否模板指令
	 * @param line
	 * @return
	 */
	public static boolean isCmd(String line) {
		if (StringUtils.isEmpty(line)) {
			return false;
		}
		
		line = line.trim();
		if (line.startsWith("[") && line.endsWith("]")) {
			return true;
		} else {
			return false;
		}
	}

	public static String exe(String line, Params params) {
		line = line.trim();
		line = line.substring(1, line.length()-1).trim();
		
		int index = -1;
		String cmdData = null;
		if ((index = line.indexOf(":")) != -1) {
			cmdData = line.substring(index + 1);
			line = line.substring(0, index);
		}
		Cmd cmd = Cmd.parseCmd(line);
		if (cmd == null) {
			return line;
		}
		
		if (params == null) {
			logger.warn("input param is null");
			return "";
		}
		
		switch (cmd) {
			case validateNull:
				return validateNull(params, cmdData);
			case pstmtSet:
				return pstmtSet(params);
			case fillCriteria:
				return fillCriteria(params);
			case wrapRs:
				return wrapRs(params);
			case updateSql:
				return updateSql(params);
			default:
				logger.error("not supported cmd["+ cmd +"]");
				throw new RuntimeException("not supported cmd["+ cmd +"]");
		}
	}
	
	/**
	 * 生成参数空验证代码
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String validateNull(Params params, String cmdData) {
		Object inputParam = params.get(Params.KEY_INPUT_ARG_CLASS);
		if (inputParam == null) {
			logger.warn("input param is null");
			return "";
		}
		
		if (StringUtils.isEmpty(cmdData)) {
			cmdData = "null";
		}
		
		StringBuffer resultBuffer = new StringBuffer();
		if (inputParam instanceof List<?>) {
			List<Class<?>> inputParams = (List<Class<?>>) inputParam;
			for (int i = 0; i<inputParams.size(); i++) {
				Class<?> clazz = inputParams.get(i);
				if (i == 0 && !clazz.isPrimitive()) {
					resultBuffer.append("if ("+ StringUtils.getEntityName(clazz) + " == null");
				} else if (!clazz.isPrimitive()) {
					resultBuffer.append(" || "+ StringUtils.getEntityName(clazz) + " == null");
				}
				if (i == inputParams.size()-1) {
					resultBuffer.append(") {\n");
					resultBuffer.append("logger.warn(\"input param is null!!!\");\n");
					resultBuffer.append("return "+ cmdData +";\n");
					resultBuffer.append("}\n");
				}
			}
		}else if (inputParam instanceof Class<?>) {
			Class<?> clazz = (Class<?>) inputParam;
			if (!clazz.isPrimitive()) {
				resultBuffer.append("if ("+ StringUtils.getEntityName(clazz) + " == null");
				resultBuffer.append(") {\n");
				resultBuffer.append("logger.warn(\"input param is null!!!\");\n");
				resultBuffer.append("return "+ cmdData +";\n");
				resultBuffer.append("}\n");
			}
		}
		
		return resultBuffer.toString();
	}
	
	/**
	 * 生成pstmt填充代码
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String pstmtSet(Params params) {
		Object inputParam = params.get(Params.KEY_INPUT_ARG_CLASS);
		if (inputParam == null) {
			logger.warn("input param is null");
			return "";
		}
		
		String sql = (String) params.get(Params.KEY_SQL);
		if (StringUtils.isEmpty(sql)) {
			logger.warn("param key sql is null");
			return "";
		}
		List<String> criterias = SQLScriptParser.getCriteriaNames(sql);
		if (criterias == null || criterias.size() == 0) {
			logger.warn("sql not found any criteria ?");
			return "";
		}
		
		List<Class<?>> inputArgs = null;
		if (inputParam instanceof List<?>) {
			inputArgs = (List<Class<?>>) inputParam;
		}else if (inputParam instanceof Class<?>) {
			inputArgs = new ArrayList<Class<?>>(1);
			Class<?> clazz = (Class<?>) inputParam;
			inputArgs.add(clazz);
		}
		
		int count = 1;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
		for (String criteria : criterias) {
			if (criteria.equals("?")) {
				pw.println("pstmt.setObject("+ count +", ?);");   //not recognized
			} else {
				String _fieldName = StringUtils.getFieldName(criteria, SysProps.LINK_CHAR);
				for (Class<?> inputArgClass : inputArgs) {
					Field[] fields = inputArgClass.getDeclaredFields();
					for (Field field : fields) {
						String fieldName = field.getName();
						if (_fieldName.equals(fieldName)) {
							String set = "set";
							
							Class<?> type = field.getType();
							String typeName = type.getName();
							if (type.isPrimitive()) {
								set += Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
							} else if (typeName.startsWith("java.lang")) {
								if (typeName.endsWith("Integer")) {
									set += "Int";
								} else {
									set += typeName.substring(typeName.lastIndexOf(".")+1);
								}
								
							} else if (typeName.startsWith("java.util.Date")) {
								set += "Date";
							} else {
								set += "Object";
							}
							
							String className = inputArgClass.getSimpleName();
							className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
							fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
							pw.println("pstmt."+ set +"("+ count +", "+ className +".get"+ fieldName +"());");   //not recognized
						}
					}
				}
			}
			
			count++;
		}
				
		pw.flush();
        return baos.toString();
	}
	
	/**
	 * 生成jdbcTemplate填充参数代码
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String fillCriteria(Params params) {
		Object inputParam = params.get(Params.KEY_INPUT_ARG_CLASS);
		if (inputParam == null) {
			logger.warn("input param is null");
			return "";
		}
		
		String sql = (String) params.get(Params.KEY_SQL);
		if (StringUtils.isEmpty(sql)) {
			logger.warn("param key sql is null");
			return "";
		}
		List<String> criterias = SQLScriptParser.getCriteriaNames(sql);
		if (criterias == null || criterias.size() == 0) {
			logger.warn("sql not found any criteria ?");
			return "";
		}
		
		List<Class<?>> inputArgs = null;
		if (inputParam instanceof List<?>) {
			inputArgs = (List<Class<?>>) inputParam;
		}else if (inputParam instanceof Class<?>) {
			inputArgs = new ArrayList<Class<?>>(1);
			Class<?> clazz = (Class<?>) inputParam;
			inputArgs.add(clazz);
		}
		
		StringBuffer resultBuffer = new StringBuffer();
		for (String criteria : criterias) {
			if (criteria.equals("?")) {
				resultBuffer.append("?, //not recognized");  //not recognized
			} else {
				String _fieldName = StringUtils.getFieldName(criteria, SysProps.LINK_CHAR);
				for (Class<?> inputArgClass : inputArgs) {
					Field[] fields = inputArgClass.getDeclaredFields();
					for (Field field : fields) {
						String fieldName = field.getName();
						if (_fieldName.equals(fieldName)) {
							String className = inputArgClass.getSimpleName();
							className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
							fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
							resultBuffer.append(className +".get"+ fieldName +"(),");
						}
					}
				}
			}
		}
		
		if (resultBuffer.length() > 0) {
			resultBuffer.deleteCharAt(resultBuffer.length() - 1);
		}
				
        return resultBuffer.toString();
	}

	
	/**
	 * 生成结果集封装代码
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String wrapRs(Params params) {
		List<FieldMeta> fieldMetas =  (List<FieldMeta>) params.get(Params.KEY_FIELD_META);
		
		String entityClassName = (String) params.get(Params.KEY_ENTITY_CLASS_NAME);
		String entityName = (String) params.get(Params.KEY_ENTITY_NAME);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
		
        pw.println(entityClassName + " " + entityName + " = new " + entityClassName +"();");
        
        for (FieldMeta fieldMeta : fieldMetas) {
            String fieldName = fieldMeta.getName();
            String typeName = fieldMeta.getType().getSimpleName();
            if (typeName.charAt(0) >= 97 && typeName.charAt(0) <= 122) {
                typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
            }
            
            pw.println(entityName + ".set"+ 
                    Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) + 
                    "(rs.get"+ typeName + "(\""+ fieldMeta.getColumnName() +"\"));");
        }
        pw.flush();
        return baos.toString();
	}
	
	/**
	 * 生成更新语句代码
	 * @param params
	 * @return
	 */
	private static String updateSql(Params params) {
		Class<?> voClass = (Class<?>) params.get(Params.KEY_INPUT_ARG_CLASS);
		Class<?> poClass = (Class<?>) params.get(Params.KEY_PO_CLASS);
		
		String tableName = null;
		String idColumn = null;
		String idValueCode = null;
		
		Table annotation = (Table)poClass.getAnnotation(Table.class);         
		if(annotation != null){             
			tableName = annotation.name();
		}
		
		if (StringUtils.isEmpty(tableName)) {
			throw new RuntimeException("can not obtain the update table name");
		}
		
		StringBuffer sqlBuffer = new StringBuffer("update ").append(tableName).append(" set ");
		List<String> argList = new ArrayList<String>();
		
		String voEntityName = voClass.getSimpleName();
		voEntityName = Character.toLowerCase(voEntityName.charAt(0)) + voEntityName.substring(1);
		Field[] voFields = voClass.getDeclaredFields();
		Field[] poFields = poClass.getDeclaredFields();
		for (Field voField : voFields) {
			String voFieldName = voField.getName();
			if(voField.getModifiers() != Modifier.PRIVATE) {
				logger.warn(voClass.getName() + " field " + voFieldName + " ignored!");
				continue;
			}
			
			for (Field poField : poFields) {
				String poFieldName = poField.getName();
				if (voFieldName.equals(poFieldName)) {
					Column column = poField.getAnnotation(Column.class);
					String colName = null;
					if (column != null) {
						colName = column.name();
					} else {
						colName = StringUtils.getColumnNameFromField(poFieldName, SysProps.LINK_CHAR);
					}
					
					String methodPrefix = ".get";
					String fieldType = voField.getType().getSimpleName();
					if (fieldType.equals("boolean") || fieldType.endsWith("Boolean")) {
						methodPrefix = ".is";
					}
					
					String _args = voEntityName + methodPrefix + Character.toUpperCase(voFieldName.charAt(0)) + voFieldName.substring(1)+"()";
					
					Id id = poField.getAnnotation(Id.class);
					if (id != null) {
						idColumn = colName;
						idValueCode = _args;
					} else {
						sqlBuffer.append(colName + "=?, ");
						argList.add(_args);
					}
				}
			}
		}
		
		if (StringUtils.isEmpty(idColumn)) {
			throw new RuntimeException("can not determine pri key");
		}
		
		argList.add(idValueCode);
		
		sqlBuffer.deleteCharAt(sqlBuffer.length()-2).append(" where ").append(idColumn).append("=?");
		
		String s_arg = Arrays.toString(argList.toArray());
		if (s_arg.length() > 2) {
			s_arg = s_arg.substring(1, s_arg.length()-1);
		}
		
		StringBuffer resultBuffer = new StringBuffer();
		resultBuffer.append("String sql = \"")
					.append(sqlBuffer)
					.append("\";\n")
					.append("Object[] args = new Object[]{")
					.append(s_arg).append("};");
		
		return resultBuffer.toString();
	}
	

	public static void main(String[] args) {
		Class<?> poClass = CustomClassLoader.getInstance().loadClassBySimpleName("TTBillJob");
		Class<?> voClass =  CustomClassLoader.getInstance().loadClassBySimpleName("TTBillJobVO");
		
		String tableName = null;
		String idColumn = null;
		String idValueCode = null;
		
		Table annotation = (Table)poClass.getAnnotation(Table.class);         
		if(annotation != null){             
			tableName = annotation.name();
		}
		
		if (StringUtils.isEmpty(tableName)) {
			throw new RuntimeException("can not obtain the update table name");
		}
		
		StringBuffer sqlBuffer = new StringBuffer("update ").append(tableName).append(" set ");
		List<String> argList = new ArrayList<String>();
		
		String voEntityName = voClass.getSimpleName();
		voEntityName = Character.toLowerCase(voEntityName.charAt(0)) + voEntityName.substring(1);
		Field[] voFields = voClass.getDeclaredFields();
		Field[] poFields = poClass.getDeclaredFields();
		for (Field voField : voFields) {
			String voFieldName = voField.getName();
			System.out.println(voFieldName + "  " + (voField.getModifiers() == Modifier.PRIVATE));
			for (Field poField : poFields) {
				String poFieldName = poField.getName();
				if (voFieldName.equals(poFieldName)) {
					Column column = poField.getAnnotation(Column.class);
					String colName = null;
					if (column != null) {
						colName = column.name();
					} else {
						colName = StringUtils.getColumnNameFromField(poFieldName, SysProps.LINK_CHAR);
					}
					
					String _args = voEntityName + ".get" + Character.toUpperCase(voFieldName.charAt(0)) + voFieldName.substring(1)+"()";
					
					Id id = poField.getAnnotation(Id.class);
					if (id != null) {
						idColumn = colName;
						idValueCode = _args;
					} else {
						sqlBuffer.append(colName + "=?, ");
						argList.add(_args);
					}
				}
			}
		}
		
		if (StringUtils.isEmpty(idColumn)) {
			throw new RuntimeException("can not determine pri key");
		}
		
		argList.add(idValueCode);
		
		sqlBuffer.deleteCharAt(sqlBuffer.length()-1).append(" where ").append(idColumn).append("=?");
		
		StringBuffer resultBuffer = new StringBuffer();
		resultBuffer.append("String sql = \"")
					.append(sqlBuffer)
					.append("\";\n")
					.append("Object[] args = new Object[]{")
					.append(Arrays.toString(argList.toArray())).append("];");
		
		System.out.println(resultBuffer.toString());
	}
}
