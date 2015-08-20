/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;

/**
 * 表示一个语法词条。
 *
 * @author jun <jun@diosay.com>
 */
public interface Lexer {

    /**
     * 获取标识
     *
     * @return
     */
    String getToken();

    /**
     * 是否是块语法
     *
     * @return
     */
    boolean isBlock();

    /**
     * 解析
     *
     * @param source
     * @param start
     * @param end
     * @param line
     */
    void parse(Document dom,CharSequence source, int start, int end, int line);

}
