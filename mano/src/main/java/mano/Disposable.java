/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

/**
 * 定义一种释放分配的资源的方法。
 * @author junhwong
 */
public interface Disposable {
    
    /**
     * 释放与当前实例关联的资源。
     */
    void dispose();
}
