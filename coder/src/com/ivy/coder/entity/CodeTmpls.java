/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.coder.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年4月15日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class CodeTmpls {
    
    private static final Logger logger = Logger.getLogger(CodeTmpls.class);
    
    private static CodeTmpls codeTmpls = new CodeTmpls();
    
    private Map<String, CodeTmpl> tmpls = new HashMap<String, CodeTmpl>();
    
    private CodeTmpls() {
        super();
        init();
    }
    
    public static void reload() {
    	codeTmpls.init();
    }
    
    @SuppressWarnings("unchecked")
    private void init() {
        Document document = null;
        InputStream is = null;
        try {
            SAXReader saxReader = new SAXReader();
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("code_tmpl.xml");

            document = saxReader.read(is);
            Element root = document.getRootElement();
            Iterator<Element> rootIter = root.elementIterator();
            while (rootIter.hasNext()) {
            	CodeTmpl codeTmpl = new CodeTmpl();
            	
            	Element tmplNode = rootIter.next();
            	
            	String name = tmplNode.attributeValue("name");
            	codeTmpl.setName(name);
            	
            	String tmpl = tmplNode.element("code").getText();
            	codeTmpl.setCode(tmpl);
                
            	tmpls.put(name, codeTmpl);
            }

            is.close();
            document.clearContent();
        } catch (DocumentException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("", e);
            e.printStackTrace();
        }
    }
    
    public static CodeTmpls getInstance() {
        return codeTmpls;
    }
    
    public CodeTmpl getCodeTmpl(String key) {
    	return tmpls.get(key);
    }
    
    public boolean existCodeTmpl(String key) {
    	return tmpls.containsKey(key);
    }
    
    public static void main(String[] args) {
        getInstance();
    }
}
