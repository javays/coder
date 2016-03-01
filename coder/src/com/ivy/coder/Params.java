package com.ivy.coder;

import java.util.HashMap;
import java.util.Map;

public class Params extends HashMap<String, Object> {

private static final long serialVersionUID = 1L;
    
//    public static final String KEY_ENTITY_CLASS = "entity_class";
    public static final String KEY_INPUT_ARG_CLASS = "inputArgClass";
    public static final String KEY_INPUT_ARGS = "inputArgs";
    public static final String KEY_INPUT_ARGS_VALUE = "inputArgsValue";
    public static final String KEY_ENTITY_CLASS_NAME = "className";
    public static final String KEY_ENTITY_CLASS_NAME_INTE = "classNameInterface";
    public static final String KEY_ENTITY_NAME = "entityName";
    public static final String KEY_FIELD_META = "fieldMeta";
    
    public static final String KEY_PCK = "pck";
    
    public static final String KEY_SQL = "sql";

	public static final String KEY_PO_CLASS = "poClass";
    
    public Params() {
        super();
    }
    
    public Params(Map<String, Object> map) {
        this.putAll(map);
    }
    
    @Override
    public String toString() {
        String str = "Params [";
        for (Entry<String, Object> entry : entrySet()) {
            str += entry.getKey() + "=" + entry.getValue() + ",";
        }
        str += "]";
        return str;
    }
}
