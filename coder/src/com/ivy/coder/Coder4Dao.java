/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.ivy.coder.dao.ExeCmd;
import com.ivy.coder.entity.CodeTmpl;
import com.ivy.coder.entity.CodeTmpls;
import com.ivy.coder.entity.FieldMeta;
import com.ivy.coder.entity.SQLMeta;
import com.ivy.coder.parser.MySQLScriptParser;
import com.ivy.coder.parser.OracleSQLScriptParser;
import com.ivy.coder.parser.SQLScriptParser;
import com.ivy.coder.utils.FileUtils;
import com.ivy.coder.utils.StringUtils;
import com.ivy.coder.utils.SysProps;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-17
 */
public class Coder4Dao extends CoderHelper {
	
	private static final Logger logger = Logger.getLogger(Coder4Dao.class);
	
	private Coder4Entity coder4Entity = new Coder4Entity();
	
	private SQLScriptParser scriptParser = null;
	
	public Coder4Dao() {
		if (SysProps.getDb_type().equals(SysProps.DbType.mysql.name())) {
			scriptParser = new MySQLScriptParser();
		} else if (SysProps.getDb_type().equals(SysProps.DbType.oracle.name())) {
			scriptParser = new OracleSQLScriptParser();
		}
	}
	
	/**
	 * 根据模板编写查询代码
	 * @param sql
	 * @param inputArgs
	 * @param tmpl
	 * @return
	 */
	public String createQueryCode(String sql, String vo, List<String> inputArgs, String tmpl) {
		String s_inputArgs = null;
		AtomicInteger atomicInteger = new AtomicInteger();
		if (inputArgs != null && inputArgs.size() > 0) {
			s_inputArgs = Arrays.toString(inputArgs.stream().map(e -> {
				String entityName = null;
				if (PrimitiveType.contains(e)) {
					entityName = "arg" + atomicInteger.incrementAndGet();
				} else {
					entityName = Character.toLowerCase(e.charAt(0)) + e.substring(1);
				}
				return e + " " + entityName;
			}).toArray());
			
			s_inputArgs = s_inputArgs.substring(1, s_inputArgs.length()-1);
		}
		
		return createQueryCode(sql, vo, s_inputArgs, tmpl);
	}
	
	/**
	 * 根据模板编写查询代码
	 * @param sql
	 * @param inputArgs  CustParam custParm, int a, int b
	 * @param tmpl
	 * @return
	 */
	public String createQueryCode(String sql, String vo, String s_inputArgs, String tmpl) {
		if (!CodeTmpls.getInstance().existCodeTmpl(tmpl)) {
			throw new RuntimeException("code tmpl ["+ tmpl +"] is not exist!!!");
		}
		Params params = new Params();
		params.put(Params.KEY_SQL, sql);
		params.put(Params.KEY_INPUT_ARGS, s_inputArgs);
		
		if (!StringUtils.isEmpty(s_inputArgs)) {
			String[] a_inputArgs = s_inputArgs.split("\\s*,\\s*");
			List<String> argsName = new ArrayList<String>(a_inputArgs.length);
			List<String> inputArgs = Arrays.stream(a_inputArgs).map(s -> {
				String[] split = s.split("\\s+");
				String argName = null;
				if (split.length > 1) {
					argName = split[1];
				} else {
					argName = "";
				}
				argsName.add(argName);
				
				return split[0];
			}).collect(Collectors.toList());
			List<Class<?>> inputArgsClass = loadClasses(inputArgs);
			params.put(Params.KEY_INPUT_ARG_CLASS, inputArgsClass);
			
			if (inputArgsClass != null) {
				StringBuffer s_inputArgsValue = new StringBuffer();
				
				AtomicInteger atomicInteger = new AtomicInteger();
				inputArgsClass.forEach(clazz -> {
					String inputArgName = argsName.get(atomicInteger.getAndIncrement());
					
					s_inputArgsValue.append("\"").append(inputArgName).append("=\" + ");
					if (clazz.isPrimitive()) {
						s_inputArgsValue.append(inputArgName).append("+ \";\"").append("+");
					} else {
						s_inputArgsValue.append(inputArgName).append(".toString() + \";\"").append("+");
					}
				});
				
				
				if (s_inputArgsValue.length() > 0) {
					s_inputArgsValue.deleteCharAt(s_inputArgsValue.length() - 1);
				}
				params.put(Params.KEY_INPUT_ARGS_VALUE, s_inputArgsValue);
			}
		}
		
		String code = coder4Entity.createJdoCode(vo, sql);
		File voJavaFile = new File(SysProps.getProject_src() 
								+ SysProps.getProject_pck_jdo().replaceAll(Pattern.quote("."), "/"),
								vo + ".java");
		logger.info("create vo java src file ["+ voJavaFile.getAbsolutePath() +"]");
		FileUtils.write(voJavaFile.getAbsolutePath(), code);
		
		params.put(Params.KEY_ENTITY_CLASS_NAME, vo);
		
		return createQueryCode(params, tmpl);
	}
	
	/**
	 * 根据模板编写查询代码
	 * @param params
	 * @param tmpl
	 * @return
	 */
	private String createQueryCode(Params params, String tmpl) {
		if (params == null) {
            logger.warn("input param is null");
			return "";
        }
		
		String sql = (String) params.get(Params.KEY_SQL);
		if (StringUtils.isEmpty(sql)) {
			logger.warn("input param sql is null");
            return "";
        }
		
		CodeTmpl codeTmpl = CodeTmpls.getInstance().getCodeTmpl(tmpl);
		if (codeTmpl == null) {
			logger.warn("tmpl ["+ tmpl +"] not found!!!!!");
			return "";
		}
		
		String entityClassName = (String) params.get(Params.KEY_ENTITY_CLASS_NAME);
		if (StringUtils.isEmpty(entityClassName)) {
			logger.warn("input param vo class name is null");
            return "";
        }
		String entityName = Character.toLowerCase(entityClassName.charAt(0)) + entityClassName.substring(1);
		params.put(Params.KEY_ENTITY_NAME, entityName);
		
		SQLMeta sqlMeta = scriptParser.parseSql(sql);
		List<FieldMeta> fieldMetas = scriptParser.getFieldMetas(sqlMeta.getColumns());
		params.put(Params.KEY_FIELD_META, fieldMetas);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
		try (
			 StringReader sr = new StringReader(codeTmpl.getCode());
			 BufferedReader br = new BufferedReader(sr);
		    ) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (ExeCmd.isCmd(line)) {
					line = ExeCmd.exe(line, params);
				} else {
					line = fillParams(line, params);
				}
				pw.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		} 
				
		pw.flush();
        return baos.toString();
	}
	
	/**
	 * 针对Value Object生成update代码
	 * @param vo
	 * @param po
	 * @param tmpl
	 * @return
	 */
	public String createUpdateCode(String vo, String po, String tmpl) {
		if (!CodeTmpls.getInstance().existCodeTmpl(tmpl)) {
			throw new RuntimeException("code tmpl ["+ tmpl +"] is not exist!!!");
		}
		Params params = new Params();
		
		Class<?> voClass = loadClass(vo);
		Class<?> poClass = loadClass(po);
		if (voClass == null || poClass == null) {
			throw new RuntimeException("value object class and persist object class must supply!");
		}
		
		params.put(Params.KEY_INPUT_ARG_CLASS, voClass);
		params.put(Params.KEY_PO_CLASS, poClass);
		
		StringBuffer s_inputArg = new StringBuffer();
		String inputArgName = Character.toLowerCase(vo.charAt(0)) + vo.substring(1);
		s_inputArg.append(vo)
				.append(" ")
				.append(inputArgName);
		params.put(Params.KEY_INPUT_ARGS, s_inputArg);
		
		StringBuffer s_inputArgsValue = new StringBuffer();
		s_inputArgsValue.append("\"").append(inputArgName).append("=\" + ");
		if (voClass.isPrimitive()) {
			s_inputArgsValue.append(inputArgName).append("+ \";\"");
		} else {
			s_inputArgsValue.append(inputArgName).append(".toString() + \";\"");
		}
		params.put(Params.KEY_INPUT_ARGS_VALUE, s_inputArgsValue);
		
		CodeTmpl codeTmpl = CodeTmpls.getInstance().getCodeTmpl(tmpl);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
		try (
			 StringReader sr = new StringReader(codeTmpl.getCode());
			 BufferedReader br = new BufferedReader(sr);
		    ) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (ExeCmd.isCmd(line)) {
					line = ExeCmd.exe(line, params);
				} else {
					line = fillParams(line, params);
				}
				pw.println(line);
			}
		} catch (Exception e) {
			logger.error("", e);
		} 
				
		pw.flush();
        return baos.toString();
	}
	
	/**
	 * 代码写入到文件
	 * @param className
	 * @param content
	 */
	public void writeToFile(String className, String code) {
		File daoPath = new File(SysProps.getProject_src() + SysProps.getProject_pck_dao().replaceAll(Pattern.quote("."), "/"));
		File daoImplPath = new File(SysProps.getProject_src() + SysProps.getProject_pck_dao_impl().replaceAll(Pattern.quote("."), "/"));
		if (!daoPath.exists()) {
			daoPath.mkdirs();
		}
		if (!daoImplPath.exists()) {
			daoImplPath.mkdirs();
		}
		
		Params params = new Params();
		params.put(Params.KEY_ENTITY_CLASS_NAME, className);
		params.put(Params.KEY_ENTITY_CLASS_NAME_INTE, className.substring(0, className.length()-4));
		
		File daoInterfaceFile = new File(daoPath, params.get(Params.KEY_ENTITY_CLASS_NAME_INTE) + ".java");
		File daoImplFile = new File(daoImplPath, className + ".java");
		if (!daoInterfaceFile.exists()) {
			CodeTmpl codeTmpl = CodeTmpls.getInstance().getCodeTmpl("class_interface");
			String codeTmplContent = codeTmpl.getCode();
			
			CodeTmpl implCodeTmpl = CodeTmpls.getInstance().getCodeTmpl("class_implement");
			String implCodeTmplContent = implCodeTmpl.getCode();
			try (
					PrintWriter pw = new PrintWriter(daoInterfaceFile);
					BufferedReader br = new BufferedReader(new StringReader(codeTmplContent.trim()));
					
					PrintWriter implPw = new PrintWriter(daoImplFile);
					BufferedReader implBr = new BufferedReader(new StringReader(implCodeTmplContent.trim()));
				) {
				String line = null;
				params.put(Params.KEY_PCK, SysProps.getProject_pck_dao());
				while ((line = br.readLine()) != null) {
					line = fillParams(line, params);
					pw.println(line);
					
					if (line != null && line.indexOf("public") != -1) {
						pw.println();
						pw.println("\t" + findMethodDefine(code));
					}
				}
				pw.flush();
				
				params.put(Params.KEY_PCK, SysProps.getProject_pck_dao_impl());
				while ((line = implBr.readLine()) != null) {
					line = fillParams(line, params);
					
					if (line != null && line.trim().equals("}")) {
						implPw.println(code);
					}
					implPw.println(line);
				}
				implPw.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			
			FileUtils.appendToEnd(daoInterfaceFile.getAbsolutePath(), "\t" + findMethodDefine(code));
			FileUtils.appendToEnd(daoImplFile.getAbsolutePath(), "\t" + code);
		}
	}
}



























