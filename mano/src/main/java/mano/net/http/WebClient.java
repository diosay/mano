/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.net.http;

import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @deprecated 移除
 * 提供用于将数据发送到由 URI 标识的资源及从这样的资源接收数据的常用方法。
 * @author johnwhang
 */
public class WebClient {
    Charset encoding;
    Map headers;
    Map queryString;
    String credentials;
    
    
    public void createRequest(String url){
        
    }
    
    public static void main(String[] args) throws Exception{
        WebRequest request=new WebRequest(null);
        request.request();
    }
    
    
}
