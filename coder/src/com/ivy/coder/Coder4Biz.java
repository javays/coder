/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ivy.coder.entity.CodeTmpl;
import com.ivy.coder.entity.CodeTmpls;
import com.ivy.coder.utils.FileUtils;
import com.ivy.coder.utils.SysProps;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-17
 */
public class Coder4Biz extends CoderHelper {
	
	private static final Logger logger = Logger.getLogger(Coder4Biz.class);
	
	private Coder4Dao coder4Dao = new Coder4Dao();
	
	
	/**
	 * 根据模板编写查询代码
	 * @param sql
	 * @param inputArgs
	 * @param tmpl
	 * @return
	 */
	public String createQueryCode(String sql, String vo, List<String> inputArgs, String tmpl) {
		String code = coder4Dao.createQueryCode(sql, vo, inputArgs, tmpl);
		
		
		return null;
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



























