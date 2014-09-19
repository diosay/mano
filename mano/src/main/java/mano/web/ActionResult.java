/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.web;

/**
 * 表示一个执行结果。
 * @author jun <jun@diosay.com>
 */
public interface ActionResult {
    /**
     * 执行
     * @param context 
     */
    void execute(ViewContext context);
}
