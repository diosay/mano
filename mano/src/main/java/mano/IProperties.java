/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano;

/**
 *
 * @author johnwhang
 */
public interface IProperties {
    String getProperty(String key);
    String getProperty(String key, boolean resolve);
    void setProperty(String key,String value);
    boolean containsProperty(String key);
    boolean containsProperty(String key, boolean resolve);
}
