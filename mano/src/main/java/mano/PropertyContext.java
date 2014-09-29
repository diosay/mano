/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

import java.util.Properties;

/**
 * 封装包含属性的上下文。
 *
 * @author jun <jun@diosay.com>
 */
public class PropertyContext {

    private Properties properties;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties props) {
        properties = props;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String def) {
        return properties.getProperty(key, def);
    }
}
