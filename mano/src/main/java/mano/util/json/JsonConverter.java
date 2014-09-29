/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.json;

/**
 *
 * @author jun <jun@diosay.com>
 */
public interface JsonConverter {
    
    String serialize(Object src) throws JsonException,IllegalArgumentException;

    <T> T deserialize(Class<T> clazz,String json) throws JsonException,IllegalArgumentException;
}
