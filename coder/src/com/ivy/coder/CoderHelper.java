/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ivy.coder.utils.CustomClassLoader;
import com.ivy.coder.utils.StringUtils;


/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-17
 */
public class CoderHelper {
	
	private final static Logger logger = Logger.getLogger(CoderHelper.class);
   
    public static final String TAB = "\t";
    public static final String LINE_FEED = "\n";
    
    public static final String PARAM = "$F{}";
    public static final String PARAM_REGEX = "\\$F\\{.+?\\}";
    public static final Pattern PARAM_PATTERN = Pattern.compile(PARAM_REGEX);
    
    public static final Pattern METHOD_DEFINE_PATTERN = Pattern.compile("^\\s*p.*?\\s+.*?\\(.*?\\)\\s*\\{?$");
    
    /**
     * 获取字符串中所有参数
     * @param str
     * @return
     */
    public Set<String> findParams(String str) {
        Matcher matcher = PARAM_PATTERN.matcher(str);
        Set<String> params = new HashSet<String>();
        while (matcher.find()) {
            String param = matcher.group();
            params.add(param);
        }
        return params;
    }
    
    /**
     * 字符串是否参数
     * @param str
     * @return
     */
    public boolean isParam(String str) {
        Matcher matcher = PARAM_PATTERN.matcher(str);
        boolean isMatch = matcher.matches();
        return isMatch;
    }
    
    /**
     * 抽取参数名称
     * @param param
     * @return
     */
    public String extractParamName(String param) {
        if (isParam(param)) {
            return param.trim().substring(3, param.length()-1);
        }else {
            return param;
        }
    }
    
    /**
     * 对模板中变量填充值
     * @param line
     * @param params
     * @return
     */
    public String fillParams(String line, Map<String, Object> params) {
        if (line == null || (line = line.trim()).equals("")) {
            return line;
        }
        
        Set<String> paramNames = findParams(line);
        for (String paramName : paramNames) {
            String _paramName = paramName.substring(3, paramName.length() - 1);
            Object value = getParamValue(_paramName, params);
            if (value == null) {
                line = line.replaceAll(Pattern.quote(paramName), "");
            }else {
                line = line.replaceAll(Pattern.quote(paramName), value.toString());
            }
        }
        
        return line;
    }
    
    /**
     * 根据变量名在Map中获取值
     * @param paramName
     * @param params
     * @return
     */
    public Object getParamValue(String paramName, Map<String, Object> params) {
        if (StringUtils.isEmpty(paramName) || params == null || params.isEmpty()) {
            return null;
        }
        
        try {
            if (paramName.indexOf(".") == -1) {    //obj
                return params.get(paramName);
            }
            String[] param = paramName.split("\\.");    //obj.field
            Object object = params.get(param[0]);
            if (object == null) {
                return null;                   //obj is not assigned
            }else {
                Class<?> clazz = object.getClass();
                Field field = clazz.getDeclaredField(param[1]);
                field.setAccessible(true);
                
                return field.get(object);
            }
        } catch (IllegalArgumentException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (SecurityException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("", e);
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 根据指定类名加载类
     * @param inputArgs
     * @return
     */
    public Class<?> loadClass(String inputArg) {
		if (StringUtils.isEmpty(inputArg)) {
			return null;
		}
		
		Class<?> clazz = null;
		if (inputArg.indexOf(".") != -1) {
			clazz = CustomClassLoader.getInstance().loadClass(inputArg);
			if (clazz == null) {
				clazz = CustomClassLoader.getInstance().loadClassBySimpleName(inputArg);
			}
		} else {
			clazz = PrimitiveType.getPrimitiveType(inputArg);
			if (clazz == null) {
				clazz = CustomClassLoader.getInstance().loadClassBySimpleName(inputArg);
				if (clazz == null) {
					clazz = CustomClassLoader.getInstance().loadClass(inputArg);
				}
			}
		}
		
		if (clazz == null) {
			try {
				clazz = Thread.currentThread().getContextClassLoader().loadClass(inputArg);
			} catch (ClassNotFoundException e) {
				logger.error("", e);
				e.printStackTrace();
			}
		}
		
		if (clazz == null) {
			logger.error("do not find class type ["+ inputArg +"]");
			throw new RuntimeException("do not find class type ["+ inputArg +"]");
		}
		logger.info("["+ inputArg +"] ["+ clazz +"]");
		
		return clazz;
	}
    
    /**
     * 根据指定类名加载类
     * @param inputArgs
     * @return
     */
    public List<Class<?>> loadClasses(List<String> inputArgs) {
		if (inputArgs != null && inputArgs.size() > 0) {
			List<Class<?>> inputArgsClass = new ArrayList<Class<?>>(inputArgs.size());
			for (String inputArg : inputArgs) {
				Class<?> clazz = loadClass(inputArg);
				inputArgsClass.add(clazz);
			}
			return inputArgsClass;
		}
		
		return null;
	}
    
    /**
     * 获取到方法代码段方法定义
     * @param code
     * @return
     */
    @SuppressWarnings("resource")
	public String findMethodDefine(String code) {
    	if (StringUtils.isEmpty(code)) {
			return "";
		}
    	
    	try(BufferedReader br = new BufferedReader(new StringReader(code));) {
    		String line = null;
    		while ((line = br.readLine()) != null) {
				if (METHOD_DEFINE_PATTERN.matcher(line).matches()) {
					line = line.trim();
					if (line.endsWith("{")) {
						line = line.substring(0, line.length()-1);
					}
					line += ";";
					return line;
				}
			}
    	} catch (IOException e) {
    		logger.error("", e);
    		e.printStackTrace();
		}
    	return "";
    }
    
    
    public static void main(String[] args) {
    	Class<?> clazz;
		try {
			clazz = Thread.currentThread().getContextClassLoader().loadClass("java.lang.Integer");
			System.out.println(clazz);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
}
