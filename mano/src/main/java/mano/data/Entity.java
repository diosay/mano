/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.data;

import java.io.Serializable;

/**
 * 表示一个数据实体。
 * @author jun
 */
public interface Entity extends PersistentObject{
    /**
     * @return 返回实体对象的唯一键。
     */
    Serializable entityKey();
}
