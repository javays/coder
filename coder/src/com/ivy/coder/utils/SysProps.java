/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;


/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-17
 */
public class SysProps {
	
	public static enum DbType {
		oracle, mysql;
	}
	
	public static final char LINK_CHAR = '_';
    
    private static String jdbc_url = null;
    private static String jdbc_class = null;
    private static String db_user = null;
    private static String db_pass = null;
    private static String db_type = null;
    
    private static String project_name = null;
    private static String project_src = null;
    private static String project_bin = null;
    private static String project_lib_root = null;
    private static List<File> third_libs = null;
    
    private static String project_sql_script = null;
    
    
    private static String project_pck_dao = null;
    private static String project_pck_dao_impl = null;
    
    private static String project_pck_entity = null;
    private static String project_pck_jdo = null;

    static{
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("sys.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
            
            jdbc_url = properties.getProperty("jdbc.url");
            jdbc_class = properties.getProperty("jdbc.class");
            db_user = properties.getProperty("db.user");
            db_pass = properties.getProperty("db.pass");
            db_type = properties.getProperty("db.type");
            
            project_name = properties.getProperty("project.name");
            project_src = properties.getProperty("project.src");
            project_bin = properties.getProperty("project.bin");
            project_lib_root = properties.getProperty("project.lib.root");
            project_sql_script = properties.getProperty("project.sql.script");
            
            project_pck_dao = properties.getProperty("project.pck.dao");
            project_pck_dao_impl = properties.getProperty("project.pck.dao.impl");
            project_pck_entity = properties.getProperty("project.pck.entity");
            project_pck_jdo = properties.getProperty("project.pck.jdo");
            
            if (!StringUtils.isEmpty(project_lib_root)) {
            	File libRoot = new File(project_lib_root);
            	third_libs = StringUtils.findFiles(libRoot, ".jar");
			}
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getJdbc_url() {
        return jdbc_url;
    }

    public static String getJdbc_class() {
        return jdbc_class;
    }

    public static String getDb_user() {
        return db_user;
    }

    public static String getDb_pass() {
        return db_pass;
    }

    public static String getProject_src() {
        return project_src;
    }

    public static String getProject_name() {
        return project_name;
    }

    public static String getProject_sql_script() {
        return project_sql_script;
    }

	public static String getProject_pck_dao() {
		return project_pck_dao;
	}

	public static String getProject_pck_dao_impl() {
		return project_pck_dao_impl;
	}

	public static String getProject_pck_entity() {
		return project_pck_entity;
	}

	public static String getDb_type() {
		return db_type;
	}

	public static String getProject_bin() {
		return project_bin;
	}

	public static String getProject_pck_jdo() {
		return project_pck_jdo;
	}
	
	public static String getProject_lib_root() {
		return project_lib_root;
	}

	public static List<File> getThird_libs() {
		return third_libs;
	}
}
