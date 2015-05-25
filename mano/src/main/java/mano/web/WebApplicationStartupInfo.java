/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import mano.logging.Log;
import mano.net.http.HttpModuleSettings;
import mano.net.http.HttpServer;
import mano.runtime.RuntimeClassLoader;
import mano.runtime.Service;
import mano.util.NameValueCollection;
import mano.util.Utility;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class WebApplicationStartupInfo {

    public Service service;
    public RuntimeClassLoader serviceLoader;
    public NameValueCollection<HttpModuleSettings> modules = new NameValueCollection<>();
    public String name;
    public String host;
    public String rootdir;
    public String type;
    public String path;
    public ArrayList<String> documents = new ArrayList<>();
    public ArrayList<String> dependency = new ArrayList<>();
    public ArrayList<String> dependencyExt = new ArrayList<>();
    public NameValueCollection<String> exports = new NameValueCollection<>();
    public Properties settings = new Properties();
    public ArrayList<String> ignoreds = new ArrayList<>();
    public String serverPath;
    public WebApplication app;
    private HttpServer server;
    public String version = "ManoServer/1.1";
    private Pattern hostreg;
    public boolean disabledEntityBody;
    public long maxEntityBodySize;
    public long maxPostFileSize;
    public String controller="home";
    public String action="index";
    
    //public final List<String> documents = new ArrayList<>();
    //public final List<String> ignoredSegments = new ArrayList<>();
    public final Map<String, String> errorPages = new HashMap<>();
    //public final List<String> dependency = new ArrayList<>();
    //public final Map<String, mano.net.http.HttpModuleSettings> modules = new java.util.LinkedHashMap<>();
    //public String controller;
    //public String action;
    //public boolean disabledEntityBody;
    //public long maxEntityBodySize;
    public String errorMode="off";
    public String errorDefaultPage="";

    public boolean matchHost(String hostname) {
        if (hostreg == null) {
            hostreg = Pattern.compile("^" + host.replace("*", "[\\w\\-_\\.]+") + "$");
        }
        if("*".equals(host)){
            return true;
        }
        return hostreg.matcher(hostname).matches();
    }

    /**
     * 获取应用实例
     * @return 
     */
    public synchronized WebApplication getInstance() {
        if (app != null) {
            return app;
        }
        if (service == null) {
            return null;
        }
        try {
            ArrayList<String> files = new ArrayList<>();
            files.add(getServerInstance().mapPath("WEB-INF/lib"));
            
            for (String file : dependencyExt) {

                if (file == null || "".equals(file)) {
                    continue;
                }

                if (file.startsWith("~/") || file.startsWith("~\\")) {
                    files.add(Utility.toPath(serverPath, file.substring(1)).toString());
                } else {
                    files.add(file);
                }
            }

            for (String file : dependency) {

                if (file == null || "".equals(file)) {
                    continue;
                }

                if (file.startsWith("~/") || file.startsWith("~\\")) {
                    files.add(Utility.toPath(serverPath, file.substring(1)).toString());
                } else {
                    files.add(file);
                }
            }
            
            //TODO:应用日志
            //, new URL[0], service.getContext()
            RuntimeClassLoader pcl=(RuntimeClassLoader)service.getProperty(Service.PROP_CLASS_LOADER, null);
            if(pcl==null){
                throw new java.lang.NullPointerException("指定服务未设置类加载器。");
            }
            RuntimeClassLoader loader = new RuntimeClassLoader(pcl);
            
            loader.addJars(files.toArray(new String[0]));
            
//            for(String ss:files){
//                System.out.println("APP CLASS PATH :"+ss);
//            }
            
            loader.addClassesPath(getServerInstance().mapPath("WEB-INF/classes"));
//            this.exports.entrySet().iterator().forEachRemaining(item -> {
//                try {
//                    loader.registerExport(item.getKey(), item.getValue());
//                } catch (ClassNotFoundException ex) {
//                    loader.getLogger().warn(null, ex);
//                }
//            });
            
            
            app = (WebApplication) loader.newInstance(this.type);
            if (app != null) {
                Method init = WebApplication.class.getDeclaredMethod("init", WebApplicationStartupInfo.class, RuntimeClassLoader.class);
                init.setAccessible(true);
                init.invoke(app, this, loader);
                return app;
            }
        } catch (InvocationTargetException ex) {
            Log.TRACE.debug("WebApplicationStartupInfo.getInstance", ex.getTargetException() == null ? ex : ex.getTargetException());
        } catch (Throwable ex) {
            Log.TRACE.debug("WebApplicationStartupInfo.getInstance", ex);
        }
        return null;
    }

    public synchronized HttpServer getServerInstance() {
        if (server == null && service != null) {

            String _basedir = this.rootdir;//应用程序路径，可以是相对于服务器程序根的相对路径

            if (_basedir.startsWith("~/") || _basedir.startsWith("~\\")) {
                _basedir = Utility.toPath(this.serverPath, _basedir.substring(1)).toString();//转换成绝对路径
            }

            final String realbasedir = _basedir;
            final String virtualPath = this.path;//
            server = new HttpServer() {

                @Override
                public String getBaseDirectory() {
                    return realbasedir;
                }

                @Override
                public String getVirtualPath() {
                    return virtualPath;
                }

                @Override
                public String mapPath(String... vpaths) {
                    return Paths.get(realbasedir, vpaths).toString();
                }

                @Override
                public String getVersion() {
                    return version;
                }

            };
        }
        return server;
    }

}
