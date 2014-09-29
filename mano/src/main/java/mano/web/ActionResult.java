/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
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
