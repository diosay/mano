/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.net.http;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import mano.http.HttpMethod;

/**
 *
 * @author johnwhang
 */
public class WebRequest {
    HttpMethod method;
    URL url;
    
    public WebRequest(URL url){
        this.url=url;
    }
    
    public void request() throws UnknownHostException{
        java.net.HttpCookie c;
        InetAddress addr =  InetAddress.getByName("www.baidu.com");
        System.out.println(addr.getHostAddress());
        //淘宝IP地址库
        //http://ip.taobao.com/instructions.php
        
        //纯真IP地址库
        //http://www.cz88.net/ip/index.aspx?ip=14.111.51.83
        
        //其它
        //http://www.ip.cn/index.php?ip=14.111.51.83
    }
    
}
