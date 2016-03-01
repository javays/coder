package com.ivy.coder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PrimitiveType {

	private static final Map<String, Class<?>> map = new HashMap<String, Class<?>>(){
		private static final long serialVersionUID = 1L;

		{
			put("byte", byte.class);
			put("char", char.class);
			put("short", short.class);
			put("int", int.class);
			put("long", long.class);
			put("float", float.class);
			put("double", double.class);
			put("boolean", boolean.class);
			
			put("Date", Date.class);
		}
	};
	
	public static boolean contains(String key) {
		return map.containsKey(key);
	}
	
	public static Class<?> getPrimitiveType(String key) {
		return map.get(key);
	}
}
