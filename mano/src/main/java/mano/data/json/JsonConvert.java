/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.data.json;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author johnwhang
 */
public class JsonConvert {

    static final String CONFIG_NAME = "mano.data.json.converter.class";
    private static JsonConverter converter;

    static {
        try {
            String str = System.getProperty("CONFIG_NAME");
            if (str != null) {
                setConverter((JsonConverter) JsonConvert.class.getClassLoader().loadClass(str).newInstance());
            }
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * @return the converter
     */
    public static JsonConverter getConverter() {
        return converter;
    }

    /**
     * @param jsonConverter the converter to set
     */
    public static void setConverter(JsonConverter jsonConverter) {
        if (jsonConverter == null) {
            throw new JsonException("jsonConverter is required.");
        }
        converter = jsonConverter;
    }

    private static Map<String, Object> getOptions(String... options) {
        JsonOption tmp;
        Map<String, Object> map = new HashMap<>();
        for (String option : options) {
            tmp = converter.getOption(option);
            if (tmp != null && !map.containsKey(option)) {
                map.entrySet().add(tmp);
            }
        }
        return map;
    }

    public static String serialize(Object src, String... options) throws JsonException {
        if (converter == null) {
            throw new JsonException("jsonConverter is required.");
        }
        return converter.serialize(src, getOptions(options));
    }

    public Object deserialize(String json, String... options) throws JsonException {
        if (converter == null) {
            throw new JsonException("jsonConverter is required.");
        }
        return converter.deserialize(json, getOptions(options));
    }

    public <T> T deserialize(String json, Class<T> clazz, String... options) throws JsonException {
        if (converter == null) {
            throw new JsonException("jsonConverter is required.");
        }
        return converter.deserialize(json, clazz, getOptions(options));
    }

}
