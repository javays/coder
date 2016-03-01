package com.ivy.coder.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class CustomClassLoader {
	
	private static final Logger logger = Logger.getLogger(CustomClassLoader.class);
	
	private static CustomClassLoader customClassLoader = new CustomClassLoader();
	
	private URLClassLoader urlClassLoader;
	
	private CustomClassLoader() {
		try {
			URL targetProjectSrc = new URL("file:/" + SysProps.getProject_bin());
			URL[] urls = new URL[]{targetProjectSrc};
			
			List<File> third_libs = SysProps.getThird_libs();
			if (third_libs != null && third_libs.size() > 0) {
				URL[] tmpUrls = new URL[urls.length + third_libs.size()];
				System.arraycopy(urls, 0, tmpUrls, 0, urls.length);
				
				int count = urls.length;
				for (File libFile : third_libs) {
					tmpUrls[count] = new URL("file:/" + libFile.getAbsolutePath());
					count ++;
				}
				
				urls = tmpUrls;
				urlClassLoader = new URLClassLoader(urls);
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
	}
	
	public static CustomClassLoader getInstance() {
		if (customClassLoader == null) {
			customClassLoader = new CustomClassLoader();
		}
		return customClassLoader;
	}

	public Class<?> loadClass(String name) {
		try {
			Class<?> clazz = urlClassLoader.loadClass(name);
			return clazz;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Class<?> loadClassBySimpleName(String name) {
		try {
			File srcRoot = new File(SysProps.getProject_src());
        	List<File> javaFiles = StringUtils.findFiles(srcRoot, name + ".java");
        	if (javaFiles == null || javaFiles.size() == 0) {
        		logger.error("can not find the class ["+ name +"]");
        		return null;
//        		throw new RuntimeException("can not find the class ["+ name +"]");
			}
        	if (javaFiles.size() > 1) {
        		logger.error("can not determine the class ["+ name +"], find result["+ Arrays.toString(javaFiles.toArray()) +"]");
        		return null;
//        		throw new RuntimeException("can not determine the class ["+ name +"], find result["+ Arrays.toString(javaFiles.toArray()) +"]");
			}
        	
        	File javaFile = javaFiles.get(0);
        	
        	String rootAbPath = srcRoot.getAbsolutePath();
        	String lastPiece = rootAbPath.substring(rootAbPath.length() - 10, rootAbPath.length());
        	
        	String javaFileAb = javaFile.getAbsolutePath();
        	javaFileAb = javaFileAb.substring(javaFileAb.lastIndexOf(lastPiece) + 11, javaFileAb.lastIndexOf("."));
        	javaFileAb = javaFileAb.replaceAll(Pattern.quote(File.separator), ".");
        	logger.info("simple class name ["+ name +"], found full path ["+ javaFileAb +"]");
        	
			Class<?> clazz = urlClassLoader.loadClass(javaFileAb);
			return clazz;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
