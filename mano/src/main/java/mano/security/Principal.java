/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.security;

/**
 * 表示一个独立主体。
 * @author jun
 */
public interface Principal {
    /**
     * @return 返回主体的标识。
     * <p> 实现时禁止返回null。
     */
    Identity getIdentity();
    
    /**
     * 验证当前主体是否在指定角色中。
     * @param role 角色唯一名称(一般在数据库中为标识)。
     * @return 成功则返回 true， 否则 false。
     */
    boolean isInRole(String role);
}
