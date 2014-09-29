/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;


/**
 *
 * @author jun <jun@diosay.com>
 */
public interface HttpServer {
    public String getBaseDirectory();
    
    public String getVirtualPath();
    
    
    public String mapPath(String...vpaths);
    
    public String getVersion();
    
}
