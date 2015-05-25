/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

/**
 * 表示一个嵌套的 Web 应用模块。
 * @author jun
 */
public abstract class NestedModule extends Module implements ActionResult{
    
    /**
     * 注意：非特定程序请勿调用。
     * @param context 
     */
    @Override
    public final void execute(ViewContext context){
        init(context);
    }
    
    
    
    
    
}
