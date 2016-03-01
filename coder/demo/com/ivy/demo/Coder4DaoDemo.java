package com.ivy.demo;

import java.util.ArrayList;
import java.util.List;

import com.ivy.coder.Coder4Dao;

public class Coder4DaoDemo {
	
	private static String demoCreateQueryCode() {
		Coder4Dao coder4Dao = new Coder4Dao();
		
		String sql = 
		        "select j.term_start_tm,\n" +
		                "\t\t\t       j.term_end_tm,\n" + 
		                "\t\t\t       j.status\n" + 
		                "  \t\t\tfrom tt_bil_job j\n" + 
		                "\t\t   where j.created_tm = ?\n" + 
		                " \t\t\tgroup by j.term_start_tm,\n" + 
		                " \t\t\t\t\t j.term_end_tm,\n" + 
		                " \t\t\t\t\t j.status";



    	/*List<String> inputArgs = new ArrayList<String>();
    	inputArgs.add("CustParam");
    	inputArgs.add("int");
    	inputArgs.add("int");*/
    	String result = coder4Dao.createQueryCode(sql, "JobPriodStatus", "", "query_jdbcTemplate_multiResult");
    	
    	/*String result = coder4Dao.createQueryCode(sql, "TMBilSystemConfig", "", 
    			"query_session_multiResult");*/
    	
    	return result;
	}
	
	private static void demoWriteToFile() {
		Coder4Dao coder4Dao = new Coder4Dao();
		
		String code = demoCreateQueryCode();
		coder4Dao.writeToFile("TLBilScatDayDetailsDao", code);
		
	}
	
	private static void demoCreateUpdateCode() {
		Coder4Dao coder4Dao = new Coder4Dao();
		
    	String result = coder4Dao.createUpdateCode("TTBillJob", "TTBillJob", "update_jdbcTemplate");
    	System.out.println(result);
	}
	
	
	
	public static void main(String[] args) {
        /*ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        
		try {
			Class<?> clazz = classLoader.loadClass("com.ivy.coder.utils.SQLUtil");
			System.out.println(clazz);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();、
		}*/
    	
		String result = demoCreateQueryCode();     //生成查询代码
		
//		String result = demoWriteToFile();
		
//		demoCreateUpdateCode();    //生成更新代码
		
		System.out.println(result);
		
    }
}
