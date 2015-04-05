/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.data.json;

import java.util.Map;

/**
 * 表示一个JSON格式的数据转换程序。
 * @author junhwong
 */
public interface JsonConverter {
    
    /**
     * 根据名称获取定义的选项。
     * @param name
     * @return 
     */
    JsonOption getOption(String name);
    
    /**
     * 将一个对象转换为JSON字符串。
     * @param src
     * @return
     * @throws JsonException 
     */
    String serialize(Object src) throws JsonException;
    
    /**
     * 将一个对象转换为JSON字符串。
     * @param src
     * @param options
     * @return
     * @throws JsonException 
     */
    String serialize(Object src,Map<String,Object> options) throws JsonException;

    /**
     * 将JSON对象字符串转换为JAVA对象。
     * @param json
     * @return
     * @throws JsonException 
     */
    Object deserialize(String json) throws JsonException;
    
    /**
     * 将JSON对象字符串转换为JAVA对象。
     * @param json
     * @param options
     * @return
     * @throws JsonException 
     */
    Object deserialize(String json,Map<String,Object> options) throws JsonException;
    
    /**
     * 将JSON对象字符串转换为指定类型的JAVA对象。
     * @param <T>
     * @param json
     * @param clazz
     * @return
     * @throws JsonException 
     */
    <T> T deserialize(String json,Class<T> clazz) throws JsonException;
    
    /**
     * 将JSON对象字符串转换为指定类型的JAVA对象。
     * @param <T>
     * @param json
     * @param clazz
     * @param options
     * @return
     * @throws JsonException 
     */
    <T> T deserialize(String json,Class<T> clazz,Map<String,Object> options) throws JsonException;
    
}
