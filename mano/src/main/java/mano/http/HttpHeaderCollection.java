/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.util.NameValueCollection;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpHeaderCollection extends NameValueCollection<HttpHeader> {

    public void put(HttpHeader header){
        super.put(header.name(), header);
    }
    
}
