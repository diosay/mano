/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.data.json;

/**
 * 表示一个JSON格式的数据转换程序。
 * @author junhwong
 */
public interface JsonConverter {
    
    /**
     * 将一个对象转换为JSON字符串。
     * @param src
     * @return
     * @throws JsonException 
     */
    String serialize(Object src) throws JsonException;

    /**
     * 将JSON对象字符串转换为JAVA对象。
     * @param <T>
     * @param clazz
     * @param json
     * @return
     * @throws JsonException 
     */
    <T> T deserialize(Class<T> clazz,String json) throws JsonException;
}
