/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.coder;

import java.lang.reflect.Field;
import java.util.List;


/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2015-1-26
 */
public class Coder4Html {
    
    public static <T> String tableGenerator(List<T> items) 
            throws IllegalArgumentException, IllegalAccessException {
        
        if (items == null || items.size() == 0) {
            return "<table></table>";
        }
        
        StringBuffer resultBuffer = new StringBuffer();
        
        Class<?> clazz = items.get(0).getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        resultBuffer.append("<table><th>");
        for (Field field : fields) {
            resultBuffer.append("<td>");
            resultBuffer.append(field.getName());
            resultBuffer.append("</td>");
        }
        resultBuffer.append("<\th>");
        
        for (T t : items) {
            resultBuffer.append("<tr>");
            for (Field field : fields) {
                field.setAccessible(Boolean.TRUE);
                resultBuffer.append("<td>");
                resultBuffer.append(field.get(t));
                resultBuffer.append("</td>");
            }
            resultBuffer.append("<\tr>");
        }
        resultBuffer.append("</table>");
        
        return resultBuffer.toString();
    }

}
