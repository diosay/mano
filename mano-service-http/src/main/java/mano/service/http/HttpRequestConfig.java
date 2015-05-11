/*
 * Copyright (C) 2014-2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.service.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sixmoon
 */
public class HttpRequestConfig {

    public final List<String> documents = new ArrayList<>();
    public final List<String> ignoredSegments = new ArrayList<>();
    public final Map<String, String> errorPages = new HashMap<>();
    public final List<String> dependency = new ArrayList<>();
    public final Map<String, mano.net.http.HttpModuleSettings> modules = new java.util.LinkedHashMap<>();
    public String controller;
    public String action;
    public boolean disabledEntityBody;
    public long maxEntityBodySize;
    public String errorMode="off";
    public String errorDefaultPage="";
    
    
    
    
}
