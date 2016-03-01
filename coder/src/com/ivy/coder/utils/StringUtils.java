/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-16
 */
public class StringUtils {
    
    public static boolean isEmpty(String str) {
        if (str == null || str.trim().equals("")) {
            return true;
        }
        return false;
    }

    public static String exist(String str, String regexp) {
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        boolean match = matcher.find();
        if (match) {
            return matcher.group();
        }
        return null;
    }
    
    public static List<String> find(String str, String regexp) {
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        
        List<String> result = new ArrayList<String>();
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
    
    public static String rightPad(String str, int length, char padding) {
        if (str == null) {
            str = "";
        }
        if (length == 0 || str.length() > length) {
            return str;
        }
        for (int i = str.length(); i < length; i++) {
            str += padding;            
        }
        return str;
    }
    
    public static String repeat(String str, int repeat) {
        if (str == null) {
            return "";
        }
        
        String tmp = str;
        for (int i = 0; i < repeat-1; i++) {
            tmp += str;            
        }
        return tmp;
    }
    
    
    public static String getFieldName(String columnName, char linkChar) {
        if (StringUtils.isEmpty(columnName)) {
            return null;
        }
        
        char[] chars = columnName.toCharArray();
        int idx = columnName.indexOf(linkChar);
        while (idx != -1) {
            chars[idx+1] = Character.toUpperCase(chars[idx+1]);
            idx = columnName.indexOf(linkChar, idx+1);
        }
        
        return new String(chars).replaceAll(Pattern.quote(linkChar+""), "");
    }
    
    public static void print(String s) {
		System.out.println(s);
	}
	
	public static String removeDot(String s) {
		if (isEmpty(s)) {
			return "";
		}
		final StringBuffer strBuffer = new StringBuffer();
		Arrays.stream(s.split("\\s+")).forEach(e -> {
			int lastDot = e.lastIndexOf("."); 
			if (lastDot != -1) {
				strBuffer.append(e.substring(lastDot+1, e.length()));
			}else {
				strBuffer.append(e);
			}
			strBuffer.append(" ");
		});
		return strBuffer.deleteCharAt(strBuffer.length()-1).append(";\n").toString();
	}
	
	public static String getEntityName(Class<?> clazz) {
		String simpleName = clazz.getSimpleName();
		return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
	}
	
	/**
	 * 查找目录下所有制定后缀文件
	 * @param dir
	 * @param suffix
	 * @return
	 */
	public static List<File> findFiles(File dir, String suffix) {
		if (dir == null || !dir.exists()) {
			return null;
		}
		
		List<File> targetFiles = new ArrayList<File>();
		File[] files = dir.listFiles(pathname -> {
			return pathname.getAbsolutePath().endsWith(suffix);
		});
		targetFiles.addAll(Arrays.asList(files));
		
		File[] subDirs = dir.listFiles(pathname -> {
			return pathname.isDirectory();
		});
		for (File subDir : subDirs) {
			targetFiles.addAll(findFiles(subDir, suffix));
		}
		return targetFiles;
	}
	
	public static String getColumnNameFromField(String fieldName, char linkChar) {
		if (isEmpty(fieldName)) {
			return "";
		}
		
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < fieldName.length(); i++) {
			char c = fieldName.charAt(i);
			if (c >= 65 && c <= 90) {   //大写字母
				result.append(String.valueOf(linkChar)).append(Character.toLowerCase(c));
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}
}



