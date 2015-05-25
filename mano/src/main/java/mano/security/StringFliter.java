/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.security;

/**
 * 表示一个字符串编码(过虑)器。
 * @author jun
 */
public interface StringFliter {
    /**
     * 对给定字符串进行编码。
     * @param content
     * @return 
     */
    StringBuilder encode(CharSequence content);
}
