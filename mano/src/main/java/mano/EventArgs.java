/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano;

/**
 * 表示包含事件数据的类的基类。
 * @author junhwong
 */
public interface EventArgs {
    /**
     * 提供要用于没有事件数据的事件的值。
     */
    public static final EventArgs Empty=new EmptyEventArgs();
    
    /**
     * 空参数。 
     */
    class EmptyEventArgs implements EventArgs{
        
    }
}
