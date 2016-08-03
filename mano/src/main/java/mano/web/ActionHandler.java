/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.web;

import mano.Disposable;

/**
 * 定义一组 action 并用它们处理客户端请求。
 * @author junhwong
 */
public interface ActionHandler extends Disposable{
    /**
     * 初始化处理程序。
     * @param context 
     */
    void init(ViewContext context);
}
