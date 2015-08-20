package mano.service.http;

import java.io.File;
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
import mano.DateTime;
import mano.caching.CacheProvider;
import mano.caching.LruCacheProvider;
import mano.net.http.HttpContext;
import mano.net.http.HttpMethod;
import mano.net.http.HttpModule;
import mano.net.http.HttpStatus;
import mano.net.http.HttpUtil;
import mano.util.Utility;
import mano.web.ActionFilter;
import mano.web.ActionHandler;
import mano.web.ActionResult;
import mano.web.CookieParam;
import mano.web.Filter;
import mano.web.FilterGroup;
import mano.web.Named;
import mano.web.PathParam;
import mano.web.RequestParam;
import mano.web.SessionParam;
import mano.web.UrlCached;
import mano.web.UrlMapping;
import mano.web.ViewContext;
import mano.web.ViewEngine;
import mano.web.ViewResult;
import mano.web.WebApplication;

/**
 *
 * @author jun
 */
public class UrlRoutingModule implements HttpModule {

    private class JarScanner {

        public void scan(URL url) {
            String protocol = url.getProtocol().toLowerCase();
            if ("file".equals(protocol)) {
                try {
                    this.scanFile(new File(URLDecoder.decode(url.getFile(), "UTF-8")));
                } catch (UnsupportedEncodingException ex) {
                    if (app.getLogger().isDebugEnabled()) {
                        app.getLogger().debug(ex);
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
                if (app.getLogger().isDebugEnabled()) {
                    app.getLogger().debug("URL:" + url.toString(), ex);
                }
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
                        if (app.getLogger().isDebugEnabled()) {
                            app.getLogger().debug(ex);
                        }
                    }
                }
            }
        }

        //http://blog.csdn.net/mousebaby808/article/details/31788325
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
                    if (app.getLogger().isDebugEnabled()) {
                        app.getLogger().debug(ex);
                    }
                }
            } else if (dir.isFile() && dir.getName().toLowerCase().endsWith(".class")) {
                //如果是java类文件 去掉后面的.class 只留下类名  
                String className = dir.getName().substring(0, dir.getName().length() - 6);
                try {
                    resolveRoute(app.getLoader().loadClass(className));
                } catch (Throwable ex) {
                    if (app.getLogger().isDebugEnabled()) {
                        app.getLogger().debug(ex);
                    }
                }
            } else if (dir.isDirectory()) {
                dir.listFiles((file) -> {
                    scanFile(file);
                    return false;
                });
            }
        }
    }

    private class Route {

        Class<? extends ActionHandler> clazz;
        //boolean isPOJO;
        String patten;
        Method call;
        String module;
        String controller;
        String action;
        long lastAccssed;
        HttpMethod httpMethod;
        Map<Integer, GetValue> paramsMapping;
        ActionFilter[] filters;
        UrlCached urlCachedMapping;

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

                group = clazz.getAnnotation(FilterGroup.class);
                if (group != null && group.value() != null && group.value().length > 0) {
                    for (Filter f : group.value()) {
                        if (!tmp.contains(f.value())) {
                            tmp.add(f.value());
                        }
                    }
                } else {
                    Filter[] tmps = clazz.getAnnotationsByType(Filter.class);
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
                    if (app.getLogger().isDebugEnabled()) {
                        app.getLogger().debug("patten error:" + patten, ex);
                    }
                    return false;
                }
            }
            matcher = test.matcher(tryPath);
            return matcher.matches();
        }

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

    private class UrlCacheEntry {

        long lastAccssed;
        String etag;
    }

    private interface GetValue {

        Object value(Route r, HttpContext ctx);
    }

    private ViewEngine viewEngine;
    private Set<Route> routeTable = new LinkedHashSet<>();
    private WebApplication app;
    private HashMap<Class<?>, ActionFilter> actionFilters = new HashMap<>();
    private int cacheTimeout = 1000 * 60 * 10;
    private CacheProvider<Route> routeCache = new LruCacheProvider<>(100);
    private CacheProvider<UrlCacheEntry> urlCache = new LruCacheProvider<>(100);
    private final Pattern pattern = Pattern.compile("\\{\\s*(\\w+)\\s*\\}");
    private ArrayList<Class<?>> htypes = new ArrayList<>();

    //http://www.cnblogs.com/Ghost-Draw-Sign/articles/1428174.html
    @Override
    public void init(WebApplication app, Properties params) {
        this.app = app;
        JarScanner scanner = new JarScanner();
//        if (java.lang.MANO_WEB_MACRO.DEBUG) {
//            app.getLogger().info("scanning action handlers...");
//        }

        app.getActionHandlers().forEach(tp -> {
            try {
                resolveRoute(tp);
            } catch (Throwable ex) {
                if (app.getLogger().isDebugEnabled()) {
                    app.getLogger().debug(ex);
                }
            }
        });

        URL[] urls = app.getActionHandlerJarUrls();
        if (urls != null) {
            for (URL url : urls) {
                try {
                    scanner.scan(url);
                } catch (Throwable ex) {
                    if (app.getLogger().isDebugEnabled()) {
                        app.getLogger().debug("scanning jar:" + url, ex);
                    }
                }
            }
        }
        htypes = null;
        try {
            HashMap evn=new HashMap();
            viewEngine = (ViewEngine) app.getLoader().newInstance(params.getProperty("view.engine"));
            viewEngine.setTempdir(Utility.toPath(app.getApplicationPath(), "WEB-INF/tmp").toString());
            viewEngine.setViewdir(Utility.toPath(app.getApplicationPath(), "views").toString());
            evn.put("otc.path",Utility.toPath(app.getApplicationPath(), "bin/otc").toString());
            viewEngine.init(evn);
        } catch (Throwable ex) {
            if (app.getLogger().isDebugEnabled()) {
                app.getLogger().debug("failed to initialization module:", ex);
            }
        }
    }

    public void resolveRoute(Class<?> clazz) {
        if (clazz == null || !ActionHandler.class.isAssignableFrom(clazz)) {
            return;
        }

        //String module;
        String controller;
        String action;
        StringBuilder sb = new StringBuilder();
//        Module tmpModule = clazz.getAnnotation(Module.class);
//        if (tmpModule != null) {
//            module = tmpModule.value();
//        } else {
//            module = "";
//        }
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
                    if (app.getLogger().isDebugEnabled()) {
                        app.getLogger().debug(sb, ex);
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
            //route.module = module;

            route.urlCachedMapping = method.getAnnotation(UrlCached.class);

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
            route = routeCache.get(key);
            if (route == null) {

                for (Route r : routeTable) {
//                    if (r.patten.startsWith("res") || r.patten.startsWith("^/res") || r.patten.startsWith("^/res")) {
//                        int x = 0;
//                    }
                    //System.out.println("here:"+r.patten);
                    if (r.test(context, tryPath)) {
                        route = r;
                        routeCache.set(key, r, cacheTimeout, true, null);
                        break;
                    }
                }
            }
        } catch (Throwable ex) {
            if (app.getLogger().isDebugEnabled()) {
                app.getLogger().debug("matching route error", ex);
            }
            return false;
        }

        if (route == null) {
//            if (app.getLogger().isDebugEnabled()) {
//                app.getLogger().debug("no matching: " + " url:" + tryPath);
//            }
            return false;
        }

        //GET URL SESSION TIME 
        if (HttpMethod.GET == context.getRequest().getMethod() && route.urlCachedMapping != null) {
            String oldEtag = null;
            String etag = HttpUtil.getETag(key);

            long since;
            DateTime now = DateTime.now();
            try {
                since = DateTime.parseTime(context.getRequest().headers().get("If-Modified-Since").value(), DateTime.FORMAT_GMT);
            } catch (Exception e) {
                since = 0;
            }
            try {
                oldEtag = context.getRequest().headers().get("If-None-Match").value().trim();
            } catch (Exception e) {
                oldEtag = null;
            }
            //System.out.println("oe:" + etag + " et:" + oldEtag);
            UrlCacheEntry centry = urlCache.get(etag);
            if (centry != null && centry.lastAccssed == since && etag.equalsIgnoreCase(oldEtag)) {
                //System.out.println("lllllllllllllll");
                if ((route.urlCachedMapping.timeout() == -1 && now.getTime() - since >= 1000 * 60 * 2)
                        || (route.urlCachedMapping.timeout() > 0 && now.getTime() - since >= route.urlCachedMapping.timeout()))//两分钟
                {
                    //System.out.println("bbbbbbbbbbbb");
                } else if ((!"".equals(route.urlCachedMapping.sessionKey())
                        && null != route.urlCachedMapping.sessionKey())
                        && context.getSession() != null
                        && context.getSession().get(route.urlCachedMapping.sessionKey()) != null) {
                    //System.out.println("mmmmmmmmm");
                } else {
                    //System.out.println("kkkkkkkkkkkkkkkkkk");
                    context.getResponse().status(HttpStatus.NotModified.getStatus());
                    return true;
                }
            }
            if (centry == null) {
                centry = new UrlCacheEntry();
                urlCache.set(etag, centry, 1000 * 60 * 2, true, null);
            }
            //System.out.println("zzzzzzzz");
            centry.lastAccssed = DateTime.parseTime(now.toGMTString(), DateTime.FORMAT_GMT);
            context.getResponse().setHeader("ETag", etag);
            context.getResponse().setHeader("Last-Modified", now.toGMTString());
        }

        Object[] params = new Object[route.call.getParameterCount()];
        try {
            route.test(context, tryPath);
            for (int i = 0; i < params.length; i++) {
                params[i] = route.paramsMapping.get(i).value(route, context);
            }
        } catch (Throwable ex) {
            if (app.getLogger().isDebugEnabled()) {
                app.getLogger().debug("Failed to convert parameters: " + route.patten, ex);
            }
            return false;
        } finally {
            route.matcher = null;
        }
        ViewContext vc;
        ActionHandler obj = app.getLoader().newInstance(route.clazz);
        vc = viewEngine.createContext(context);
        vc.setEngine(viewEngine);
        vc.setController(route.controller);
        vc.setAction(route.action);
        obj.init(vc);
        for (ActionFilter filter : route.getActionFilters()) {
            filter.onActionExecuting(vc);
        }
        try{
            route.call.invoke(obj, params);
        }catch(InvocationTargetException ex){
            if(ex.getTargetException()!=null && ex.getTargetException() instanceof Exception){
                throw (Exception)ex.getTargetException();
            }
            throw ex;
        }
        for (ActionFilter filter : route.getActionFilters()) {
            filter.onActionExecuted(vc);
        }
        if (!context.getRequest().isConnected()) {
            throw new java.nio.channels.ClosedChannelException();
        }
        ActionResult result = vc.getResult();
        if (result == null) {
            return true;
        }

        result.execute(vc);
        return true;
    }

    @Override
    public void dispose() {
        viewEngine = null;
        app = null;
        routeTable = null;
        routeCache = null;
        actionFilters = null;
    }
}
