/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.demo;

import com.ivy.coder.Coder4Entity;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年8月1日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class Coder4EntityDemo {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Coder4Entity coder4Entity = new Coder4Entity();
        String result = coder4Entity.createPoCode("TTBillJob", "tt_bil_job");
        System.out.println(result);
    }

}
