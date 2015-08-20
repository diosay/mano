/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.runtime;

import mano.util.Utility;

/**
 * 内部工具
 *
 * @author jun <jun@diosay.com>
 */
public class Util {

    public static boolean toBoolean(Object obj) {
        if (obj == null) {
            return false;
        }
        try {
            return Utility.cast(Boolean.class, obj);
        } catch (Exception ex) {
            try {
                return Utility.cast(Double.class, obj) == 0 ? false : true;
            } catch (Exception ex2) {
                return true;
            }
        }
    }
}
