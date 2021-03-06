/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpUtil {

    public static void queryStringToMap(String query, Map<String, String> result) {
        queryStringToMap(query,result,Charset.forName("UTF-8"));
    }

    public static void queryStringToMap(String query, Map<String, String> result, Charset encoding) {

        query = query == null ? "" : query.trim();
        if (query.startsWith("?")) {
            query = query.substring(1);
        }
        if ("".equals(query)) {
            return;
        }

        String key;
        String value;
        int index;
        for (String s : query.split("&")) {
            index = s.indexOf('=');
            value = null;
            if (index > 0) {
                key = (s.substring(0, index) + "").trim();
                value = (s.substring(index + 1) + "").trim();
            } else {
                key = s.trim();
            }

            if (key != null && !"".equals(key)) {

                try {
                    value = URLDecoder.decode(value, encoding.name());//TODO:编码识别GB18030
                } catch (UnsupportedEncodingException ex) {

                }
                if (result.containsKey(key)) {
                    String old = result.get(key);
                    result.put(key, (old == null ? "" : old) + "," + (value == null ? "" : value));
                } else {
                    result.put(key, value);
                }
            }
        }
    }
}
