/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.io;

import java.nio.charset.Charset;

/**
 *
 * @author sixmoon
 */
public class CharsetUtil {

    public static final Charset UTF8;

    static {
        UTF8 = Charset.forName("utf-8");
    }
}
