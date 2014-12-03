/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web.modules;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mano.caching.LruCacheProvider;
import mano.http.HttpContext;
import mano.http.HttpMethod;
import mano.http.HttpModule;
import mano.util.Utility;
import mano.web.ActionFilter;
import mano.web.ActionHandler;
import mano.web.ActionResult;
import mano.web.CookieParam;
import mano.web.Filter;
import mano.web.FilterGroup;
import mano.web.Module;
import mano.web.Named;
import mano.web.PathParam;
import mano.web.RequestParam;
import mano.web.SessionParam;
import mano.web.UrlMapping;
import mano.web.ViewContext;
import mano.web.ViewEngine;
import mano.web.ViewResult;
import mano.web.WebApplication;

/**
 * 通过 URL路由并调用 java 类和方法的模块。
 *
 * @author jun <jun@diosay.com>
 */
public class UrlRoutingModule implements HttpModule {

    private class JarScanner {

        public void scan(URL url) {
            String protocol = url.getProtocol().toLowerCase();
            if ("file".equals(protocol)) {
                try {
                    this.scanFile(new File(URLDecoder.decode(url.getFile(), "UTF-8")));
                } catch (UnsupportedEncodingException ex) {
                    if (java.lang.MANO_WEB_MACRO.DEBUG) {
                        app.getLogger().debug(null, ex);
                    }
                }
            } else if ("jar".equals(protocol)) {
                scanJar(url, null);
            }
        }

        public void scanJar(URL url, String libname) {
            JarFile jar;
            try {
                jar = ((JarURLConnection) url.openConnection()).getJarFile();
            } catch (IOException ex) {
                app.getLogger().debug("URL:" + url.toString(), ex);
                return;
            }
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry entry;
            String name;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                name = entry.getName();

                if (!entry.isDirectory() && name.endsWith(".class") && name.indexOf("$") < 1) {//
                    name = (name.substring(0, name.length() - 6)).replace('/', '.');
                    try {
                        resolveRoute(app.getLoader().loadClass(name));
                    } catch (Throwable ex) {
                        if (java.lang.MANO_WEB_MACRO.DEBUG) {
                            app.getLogger().debug(null, ex);
                        }
                    }
                }
            }
        }

        public void scanFile(File dir) {
            //获取此包的目录 建立一个File  
            //File dir = new File(path);
            //如果不存在或者 也不是目录就直接返回  
            if (!dir.exists()) {
                return;
            } else if (dir.isFile() && dir.getName().toLowerCase().endsWith(".jar")) {
                try {
                    if (File.separator.equals("\\")) {
                        scanJar(new URL("jar:file:/" + dir.toString() + "!/"), dir.getName().substring(0, dir.getName().length() - 4));
                    } else {
                        scanJar(new URL("jar:file://" + dir.toString() + "!/"), dir.getName().substring(0, dir.getName().length() - 4));
                    }
                } catch (MalformedURLException ex) {
                    if (java.lang.MANO_WEB_MACRO.DEBUG) {
                        app.getLogger().debug(null, ex);
                    }
                }
            } else if (dir.isFile() && dir.getName().toLowerCase().endsWith(".class")) {
                //如果是java类文件 去掉后面的.class 只留下类名  
                String className = dir.getName().substring(0, dir.getName().length() - 6);
                try {
                    resolveRoute(app.getLoader().loadClass(className));
                } catch (Throwable ex) {
                    if (java.lang.MANO_WEB_MACRO.DEBUG) {
                        app.getLogger().debug(null, ex);
                    }
                }
            } else if (dir.isDirectory()) {
                dir.listFiles((file) -> {
                    scanFile(file);
                    return false;
                });
            }
        }
//
//        final Class<?> foundClass(String type) {
//            try {
//                return app.getLoader().loadClass(type);
//            } catch (Throwable ex) {
//                if (java.lang.MANO_WEB_MACRO.DEBUG) {
//                    app.getLogger().debug(null, ex);
//                }
//            }
//            return null;
//        }

        //final Pattern pattern = Pattern.compile("\\{\\s*(\\w+)\\s*\\}");
//        public void onFoundClass(Class<?> clazz) {
//            if (clazz == null) {
//                return;
//            }
//            //.*/controller/action/(\w*)/(\w*).*
//            //\{(\w+)(\?){0,1}\}
//            //{?name}
//            //default +1000
//            UrlMapping mapping;
//            String url = null;
//            int verb = 0;
//            boolean pojo = false;
//            Annotation[][] ps;
//            String pname;
//            Map<Integer, String> map = new HashMap<>();
//            String part;
//            ArrayList<String> list = new ArrayList<>();
//            final StringBuilder sb = new StringBuilder();
//            Matcher matcher = pattern.matcher(sb);
//            Route route;
//
//            //查找类，获取第1部分URL
//            if (Controller.class.isAssignableFrom(clazz)) {
//                mapping = clazz.getAnnotation(UrlMapping.class);
//                if (mapping != null) {
//                    url = mapping.value();
//                }
//                if (url == null || "".equals(url.trim())) {
//                    url = clazz.getSimpleName().toLowerCase();
//                    if (url.endsWith("controller")) {
//                        url = url.substring(0, url.length() - 10);
//                    }
//                }
//            } else {
//                mapping = clazz.getAnnotation(UrlMapping.class);
//                if (mapping == null) {
//                    return;
//                }
//                try {
//                    clazz.getMethod("setService", ViewContext.class);
//                } catch (Throwable ex) {
//                    return;
//                }
//                url = mapping.value();
//                //verb = mapping.verb();
//                pojo = true;
//                if (url == null || "".equals(url.trim())) {
//                    url = clazz.getSimpleName().toLowerCase();
//                    if (url.endsWith("controller")) {
//                        url = url.substring(0, url.length() - 10);
//                    }
//                }
//            }
//            if (!url.startsWith("/")) {
//                url = "/" + url;
//            }
//            if (!url.endsWith("/")) {
//                url += "/";
//            }
//
//            //查找方法，获取第2部分URL 和签名参数
//            for (Method method : clazz.getDeclaredMethods()) {
//                map.clear();
//                mapping = method.getAnnotation(UrlMapping.class);
//                if (mapping == null) {
//                    continue;
//                }
//
////                if (mapping.verb() > 0) {
////                    verb = mapping.verb(); //重写父级定义
////                }
//                part = mapping.value();
//
//                if (part == null || "".equals(part.trim())) {
//                    part = method.getName();
//                }
//                if (part.startsWith("/")) {
//                    part = part.substring(1);
//                }
//
//                list.clear();
//                sb.setLength(0);
//                sb.append(url);
//                sb.append(part);
//                matcher = pattern.matcher(sb);
//                while (matcher.find()) {
//                    String name = matcher.group(1);
//                    list.add(name);
//                    sb.replace(matcher.start(), matcher.end(), "(?<" + name + ">\\w+)");
//                }
//                //解决最后一个元素不能被替换的BUG
//                matcher = pattern.matcher(sb);
//                while (matcher.find()) {
//                    String name = matcher.group(1);
//                    list.add(name);//{1}{2}
//                    sb.replace(matcher.start(), matcher.end(), "(?<" + name + ">\\w+)");
//                }
//
//                //http://blog.sina.com.cn/s/blog_72827fb10101pl9i.html
//                //http://blog.sina.com.cn/s/blog_72827fb10101pl9j.html
//                if (sb.charAt(sb.length() - 1) != '$') {
//                    if (sb.charAt(sb.length() - 1) == '/') {
//                        sb.append("{0,1}$");
//                    } else {
//                        sb.append("/{0,1}$");
//                    }
//                }
//                if (sb.charAt(0) != '^') {
//                    sb.insert(0, "^");
//                }
//
//                //参数映射集合
//                ps = method.getParameterAnnotations();
//                for (int i = 0; i < ps.length; i++) {
//                    for (int j = 0; j < ps[i].length; j++) {
//                        if (ps[i][j] instanceof PathParam) {
//                            pname = ((PathParam) ps[i][j]).value();
//                        } else {
//                            pname = "";
//                        }
//                        if ("".equals(pname) || !list.contains(pname)) {
//                            continue;
//                        }
//                        if (map.containsKey(i)) {
//                            //map.put(i, map.get(i) + "," + pname);
//                        } else {
//                            map.put(i, pname);
//                        }
//                        break;
//                    }
//                }
//
//                route = new Route();
//                method.setAccessible(true);
//                route.call = method;
//                route.clazz = clazz;
//                //route.paramsMapping.putAll(map);
//                route.patten = sb.toString();
//                route.controller = clazz.getSimpleName().toLowerCase();
//
//                if (route.controller.endsWith("controller")) {
//                    route.controller = route.controller.substring(0, route.controller.length() - 10);
//                }
//
//                route.action = method.getName().toLowerCase();
//                routeTable.add(route);
//            }
//
//            //PathMapping()
//            //Routing(x,0);
//            //clazz.getInterfaces()
//            //System.out.println(clazz);
//        }
    }

    private class Route {

        Class<? extends ActionHandler> clazz;
        //boolean isPOJO;
        String patten;
        Method call;
        String module;
        String controller;
        String action;
        //String setServiceMethod;
        HttpMethod httpMethod;
        Map<Integer, GetValue> paramsMapping;
        ActionFilter[] filters;

        public ActionFilter[] getActionFilters() throws Exception {
            if (filters == null) {
                ArrayList<Class<?>> tmp = new ArrayList<>();
                FilterGroup group = call.getAnnotation(FilterGroup.class);
                if (group != null && group.value() != null && group.value().length > 0) {
                    for (Filter f : group.value()) {
                        if (!tmp.contains(f.value())) {
                            tmp.add(f.value());
                        }
                    }
                } else {
                    Filter[] tmps = call.getAnnotationsByType(Filter.class);
                    if (tmps != null) {
                        for (Filter f : tmps) {
                            if (!tmp.contains(f.value())) {
                                tmp.add(f.value());
                            }
                        }
                    }
                }
                ArrayList<ActionFilter> tmp2 = new ArrayList<>();
                ActionFilter filter;
                for (Class<?> c2 : tmp) {
                    filter = (ActionFilter) app.getLoader().newInstance(c2);
                    tmp2.add(filter);
                }

                filters = tmp2.toArray(new ActionFilter[0]);
            }
            return filters;
        }
        Pattern test = null;
        Matcher matcher;

        public boolean test(HttpContext context, String tryPath) {
            if (httpMethod != HttpMethod.ALL && !context.getRequest().getMethod().equalWith(httpMethod)) {
                return false;
            }
            if (test == null) {
                try {
                    test = Pattern.compile(patten, Pattern.CASE_INSENSITIVE);
                } catch (Throwable ex) {
                    if (java.lang.MANO_WEB_MACRO.DEBUG) {
                        app.getLogger().debug("patten error:" + patten, ex);
                    }
                    return false;
                }
            }
            matcher = test.matcher(tryPath);
            return matcher.matches();
        }
//        Method m;
//
//        private Method getMethod(Class<?> type) throws Exception {
//            try {
//                return type.getDeclaredMethod("setService", ViewContext.class);
//            } catch (NoSuchMethodException ex) {
//                if (type.getSuperclass() != null) {
//                    return getMethod(type.getSuperclass());
//                } else {
//                    throw ex;
//                }
//            } catch (SecurityException ex) {
//                throw ex;
//            }
//        }

//        public void setContext(ActionHandler instance, ViewContext context) throws Exception {
//            ((ActionHandler) instance).init(context);
////            if (m == null) {
////                m = getMethod(clazz);
////                m.setAccessible(true);
////            }
////            m.invoke(instance, context);
//        }
        public Object getPathValue(HttpContext ctx, String name, Class<?> type) {
            return Utility.cast(type, matcher.group(name));
        }

        public Object getSessionValue(HttpContext ctx, String name, Class<?> type) {
            return Utility.cast(type, ctx.getSession().get(name));
        }

        public Object getCookieValue(HttpContext ctx, String name, Class<?> type) {
            return Utility.cast(type, ctx.getRequest().getCookie().get(name));
        }

        //TODO:
        public Object getQueryValue(HttpContext ctx, String name, Class<?> type) {
            if (ctx.getRequest().query().containsKey(name)) {
                return Utility.cast(type, ctx.getRequest().query().get(name));
            }
            return Utility.cast(type, ctx.getRequest().form().get(name));
        }

        public Object getFormValue(HttpContext ctx, String name, Class<?> type) {
            return Utility.cast(type, ctx.getRequest().form().get(name));
        }
    }

    private interface GetValue {

        Object value(Route r, HttpContext ctx);
    }

    private ViewEngine viewEngine;
    private Set<Route> routeTable = new LinkedHashSet<>();
    private WebApplication app;
    private HashMap<Class<?>, ActionFilter> actionFilters = new HashMap<>();
    private LruCacheProvider cache = new LruCacheProvider();
    private final Pattern pattern = Pattern.compile("\\{\\s*(\\w+)\\s*\\}");
    private ArrayList<Class<?>> htypes = new ArrayList<>();

    //http://www.cnblogs.com/Ghost-Draw-Sign/articles/1428174.html
    @Override
    public void init(WebApplication app, Properties params) {
        this.app = app;
        JarScanner scanner = new JarScanner();
        if (java.lang.MANO_WEB_MACRO.DEBUG) {
            app.getLogger().info("scanning action handlers...");
        }

        app.getActionHandlers().forEach(tp -> {
            try {
                resolveRoute(tp);
            } catch (Throwable ex) {
                if (java.lang.MANO_WEB_MACRO.DEBUG) {
                    app.getLogger().info(ex);
                }
            }
        });

        URL[] urls = app.getActionHandlerJarUrls();
        if (urls != null) {
            for (URL url : urls) {
                try {
                    scanner.scan(url);
                } catch (Throwable ex) {
                    if (java.lang.MANO_WEB_MACRO.DEBUG) {
                        app.getLogger().info("scanning jar:" + url, ex);
                    }
                }
            }
        }
        htypes = null;
        try {
            viewEngine = (ViewEngine) app.getLoader().newInstance(params.getProperty("view.engine"));
            viewEngine.setTempdir(Utility.toPath(app.getApplicationPath(), "WEB-INF/tmp").toString());
            viewEngine.setViewdir(Utility.toPath(app.getApplicationPath(), "views").toString());
        } catch (Throwable ex) {
            if (java.lang.MANO_WEB_MACRO.DEBUG) {
                app.getLogger().info("failed to initialization module:", ex);
            }
        }
    }

    public void resolveRoute(Class<?> clazz) {
        if (clazz == null || !ActionHandler.class.isAssignableFrom(clazz)) {
            return;
        }

        String module;
        String controller;
        String action;
        StringBuilder sb = new StringBuilder();
        Module tmpModule = clazz.getAnnotation(Module.class);
        if (tmpModule != null) {
            module = tmpModule.value();
        } else {
            module = "";
        }
        Named named = clazz.getAnnotation(Named.class);
        if (named != null) {
            controller = named.value();
        } else {
            controller = clazz.getSimpleName().toLowerCase();
            if (controller.endsWith("controller")) {
                controller = controller.substring(0, controller.length() - 10);
            }
        }
        UrlMapping mapping = clazz.getAnnotation(UrlMapping.class);
        if (mapping != null) {
            sb.append(mapping.value());
            if (sb.charAt(sb.length() - 1) != '/') {
                sb.append('/');
            }
        } else {
            sb.append(controller).append('/');
        }
        StringBuilder url;
        Matcher matcher;
        ArrayList<String> list = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            mapping = method.getAnnotation(UrlMapping.class);
            if (mapping == null) { //TODO:确认？
                continue;
            }

            named = method.getAnnotation(Named.class);
            if (named != null) {
                action = named.value();
            } else {
                action = method.getName().toLowerCase();
            }
            url = new StringBuilder(sb);
            url.append(mapping.value().charAt(0) == '/' ? mapping.value().substring(1) : mapping.value());
            if (url.charAt(url.length() - 1) != '/') {
                url.append('/');
            }
            matcher = pattern.matcher(url);
            list.clear();
            while (matcher.find()) {
                String name = matcher.group(1);
                list.add(name);//{1}{2}
                try {
                    url.replace(matcher.start(), matcher.end(), "(?<" + name + ">\\w+)");
                } catch (Throwable ex) {
                    if (java.lang.MANO_WEB_MACRO.DEBUG) {
                        app.getLogger().info(sb, ex);
                    }
                }
                //matcher = pattern.matcher(url);
            }
            Map<Integer, GetValue> map = new HashMap<>();
            Class<?>[] types = method.getParameterTypes();
            Annotation[][] ps = method.getParameterAnnotations();
            boolean bk = false;
            for (int i = 0; i < ps.length; i++) {

                for (Annotation item : ps[i]) {
                    if (item instanceof PathParam) {
                        final String name = ((PathParam) item).value();
                        if (!list.contains(name)) {
                            bk = true;
                            break;
                        }
                        final Class<?> ptype = types[i];
                        map.put(i, (r, ctx) -> {
                            return r.getPathValue(ctx, name, ptype);
                        });
                        break;
                    } else if (item instanceof RequestParam) {
                        final String name = ((RequestParam) item).value();
                        final Class<?> ptype = types[i];
                        map.put(i, (r, ctx) -> {
                            return r.getQueryValue(ctx, name, ptype);
                        });
                        break;
                    } else if (item instanceof SessionParam) {
                        final String name = ((SessionParam) item).value();
                        final Class<?> ptype = types[i];
                        map.put(i, (r, ctx) -> {
                            return r.getSessionValue(ctx, name, ptype);
                        });
                        break;
                    } else if (item instanceof CookieParam) {
                        final String name = ((CookieParam) item).value();
                        final Class<?> ptype = types[i];
                        map.put(i, (r, ctx) -> {
                            return r.getCookieValue(ctx, name, ptype);
                        });
                        break;
                    } else {
                        bk = true;
                    }
                }
                if (bk) {
                    break;
                }
            }
            //
            if (bk) {
                return;
            }

            if (url.charAt(url.length() - 1) != '$') {
                if (url.charAt(url.length() - 1) == '/') {
                    url.append("?$");
                } else {
                    url.append("/?$");
                }
            }
            if (url.charAt(0) != '^') {
                if (url.charAt(0) != '/') {
                    url.insert(0, '/');
                }
                url.insert(0, '^');
            }
            method.setAccessible(true);
            Route route = new Route();
            route.paramsMapping = map;
            route.action = action;
            route.call = method;
            route.clazz = (Class<? extends ActionHandler>) clazz;
            route.controller = controller;
            route.patten = url.toString();
            route.httpMethod = mapping.verb();
            route.module = module;
            htypes.add(clazz);
            routeTable.add(route);
        }

    }

    @Override
    public boolean handle(HttpContext context) throws Exception {
        return this.handle(context, context.getRequest().url().getPath());
    }

    @Override
    public boolean handle(HttpContext context, String tryPath) throws Exception {
        if (viewEngine == null) {
            return false;
        } else if (!context.getRequest().isConnected()) {
            return false;
        }

        String key = context.getRequest().getMethod() + ":" + context.getRequest().url().toString();
        Route route = null;
        try {
            if (cache.get(key) == null) {

                for (Route r : routeTable) {
                    if (r.patten.startsWith("res") || r.patten.startsWith("^/res") || r.patten.startsWith("^/res")) {
                        int x = 0;
                    }
                    if (r.test(context, tryPath)) {
                        route = r;
                        cache.set(key, r, 1000 * 60 * 10, true, null);
                        break;
                    }
                }
            } else {
                route = (Route) (cache.get(key).getValue());
            }
        } catch (Throwable ex) {
            if (java.lang.MANO_WEB_MACRO.DEBUG) {
                app.getLogger().info("matching route error", ex);
            }
            return false;
        }

        if (route == null) {
            if (java.lang.MANO_WEB_MACRO.DEBUG) {
                app.getLogger().info("no matching: " + " url:" + tryPath);
            }
            return false;
        }

        Object[] params = new Object[route.call.getParameterCount()];
        try {
            route.test(context, tryPath);
            for (int i = 0; i < params.length; i++) {
                params[i] = route.paramsMapping.get(i).value(route, context);
            }
        } catch (Throwable ex) {
            if (java.lang.MANO_WEB_MACRO.DEBUG) {
                app.getLogger().info("Failed to convert parameters: " + route.patten, ex);
            }
            return false;
        }
        finally{
            route.matcher=null;
        }
        ViewContext vc;
        ActionHandler obj = app.getLoader().newInstance(route.clazz);
        vc = viewEngine.createContext(context);
        vc.setController(route.controller);
        vc.setAction(route.action);
        obj.init(vc);
        for (ActionFilter filter : route.getActionFilters()) {
            filter.onActionExecuting(vc);
        }
        route.call.invoke(obj, params);
        for (ActionFilter filter : route.getActionFilters()) {
            filter.onActionExecuted(vc);
        }
//        try {
//            
//        } catch (Throwable ex) {
//            Throwable err = ex instanceof InvocationTargetException ? ((InvocationTargetException) ex).getTargetException() : ex;
//            if (java.lang.MANO_WEB_MACRO.DEBUG) {
//                app.getLogger().info("failed to execute handler:", err);
//            }
//            context.getResponse().write(err.getClass() + ":" + err.getMessage());
//            return true;
//        }
        if (!context.getRequest().isConnected()) {
            throw new java.nio.channels.ClosedChannelException();
        }
        ActionResult result = vc.getResult();
        if (result == null) {
            return true;
        }
        if (result instanceof ViewResult) {
            ((ViewResult) result).init(viewEngine);
        }

        result.execute(vc);
        return true;
    }

    @Override
    public void dispose() {
        viewEngine = null;
        app = null;
        routeTable = null;
        cache = null;
        actionFilters = null;
    }

}
