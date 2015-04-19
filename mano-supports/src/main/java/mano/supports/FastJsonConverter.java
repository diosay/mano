/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.supports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mano.data.json.JsonConverter;
import mano.data.json.JsonException;
import mano.data.json.JsonOption;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * JSON转换程序。
 *
 * @see https://github.com/alibaba/fastjson
 * @author sixmoon
 */
public class FastJsonConverter implements JsonConverter {

    @Override
    public JsonOption getOption(String name) {
        return null;
    }

    @Override
    public String serialize(Object src) throws JsonException {
        try {
            return JSON.toJSONString(src);
        } catch (Throwable ex) {
            throw new JsonException(ex);
        }
    }

    @Override
    public String serialize(Object src, Map<String, Object> options) throws JsonException {
        try {
            List<SerializerFeature> list = new ArrayList();
            for (Object obj : options.values()) {
                if (obj != null && obj instanceof SerializerFeature) {
                    list.add((SerializerFeature) obj);
                }
            }
            return JSON.toJSONString(src, list.toArray(new SerializerFeature[0]));
        } catch (Throwable ex) {
            throw new JsonException(ex);
        }
    }

    @Override
    public Object deserialize(String json) throws JsonException {
        try {
            return JSON.parseObject(json);
        } catch (Throwable ex) {
            throw new JsonException(ex);
        }
    }

    @Override
    public Object deserialize(String json, Map<String, Object> options) throws JsonException {
        try {
            return JSON.parseObject(json);
        } catch (Throwable ex) {
            throw new JsonException(ex);
        }
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz) throws JsonException {
        try {
            return JSON.parseObject(json, clazz);
        } catch (Throwable ex) {
            throw new JsonException(ex);
        }
    }

    @Override
    public <T> T deserialize(String json, Class<T> clazz, Map<String, Object> options) throws JsonException {
        try {
            return JSON.parseObject(json, clazz);
        } catch (Throwable ex) {
            throw new JsonException(ex);
        }
    }
}
