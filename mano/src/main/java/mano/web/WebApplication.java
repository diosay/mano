/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mano.PropertyContext;
import mano.net.http.HttpContext;
import mano.net.http.HttpException;
import mano.net.http.HttpModule;
import mano.net.http.HttpModuleSettings;
import mano.net.http.HttpServer;
import mano.net.http.HttpStatus;
import mano.runtime.RuntimeClassLoader;
import mano.security.Identity;
import mano.security.Principal;
import mano.util.Utility;

/**
 * 定义 Web 应用程序中的所有应用程序对象通用的方法、属性和事件。
 *
 * @author jun <jun@diosay.com>
 */
public class WebApplication extends PropertyContext {

    private Set<HttpModule> modules;
    private RuntimeClassLoader loader;
    private WebApplicationStartupInfo startupInfo;
    private final Map<String, Object> items = new ConcurrentHashMap<>();
    private mano.logging.Logger log;

    public RuntimeClassLoader getLoader() {
        return loader;
    }

    public mano.logging.Logger getLogger() {
        return log;
    }

    /**
     * 初始化应用程序。
     *
     * @param info
     * @param appLoader
     */
    final void init(WebApplicationStartupInfo info, RuntimeClassLoader appLoader) {
        log = new mano.logging.Log(info.name);
        startupInfo = info;
        loader = appLoader;
        this.setProperties(info.settings);
        this.onInit();
        modules = new LinkedHashSet<>();
        for (HttpModuleSettings settings : info.modules.values()) {
            try {
                HttpModule mod = (HttpModule) loader.newInstance(settings.type);
                if (mod != null) {
                    mod.init(this, settings.settings);
                    modules.add(mod);
                }
            } catch (Throwable ex) {
                getLogger().warn("Failed to initialize the HTTP module:", ex);
            }
        }

    }

    final void destory() {
        onDestory();
        startupInfo.app = null;
        if (modules != null) {
            for (HttpModule module : modules) {
                try {
                    module.dispose();
                } catch (Exception ingored) {
                }
            }
            modules.clear();
        }
        items.clear();
        loader = null;
    }

    /**
     * 获取应用程序根目录。
     *
     * @return
     */
    public final String getApplicationPath() {
        return this.startupInfo.getServerInstance().getBaseDirectory();
    }

    public final Map<String, Object> items() {
        return items;
    }

    /**
     * 获取一个用于在应用程序各 HttpContext 之间交互的对象。
     *
     * @param name
     * @return
     */
    public final Object get(String name) {
        if (items.containsKey(name)) {
            return items.get(name);
        }
        return null;
    }

    /**
     * 设置一个用于在应用程序各 HttpContext 之间交互的对象。
     *
     * @param name
     * @param value
     */
    public final void set(String name, Object value) {
        items.put(name, value);
    }

    /**
     * 任何实现HTTP Service 都须要调用该方法。
     * <p>
     * 基础代码，除非你清楚的知道你要做什么，否则不建议在用户代码中调用该方法。
     *
     * @param context
     */
    public void processRequest(HttpContext context) {
        boolean processed = false;
        try {

            ArrayList<String> paths = new ArrayList<>();
            String path = context.getRequest().url().getPath();
            paths.add(Utility.toPath(context.getServer().getVirtualPath(), path).toString());
            if (true) {
                for (String s : startupInfo.documents) {
                    paths.add(Utility.toPath(context.getServer().getVirtualPath(), path, s).toString());
                }
                paths.add(Utility.toPath(context.getServer().getVirtualPath(), path, startupInfo.controller, startupInfo.action).toString());
                paths.add(Utility.toPath(context.getServer().getVirtualPath(), path, startupInfo.action).toString());
            }
            for (String p : paths) {
                p = p.replace('\\', '/');
                if (!p.startsWith("/")) {
                    p = "/" + p;
                }
                for (HttpModule module : modules) {
                    if (module.handle(context, p)) {
                        processed = true;
                        break;
                    }

                }
                if (processed) {
                    break;
                }
            }
        } catch (Exception ex) {
            processed = true;
            this.onError(context, ex);
        }
        if (!processed) {
            this.onError(context, new HttpException(HttpStatus.NotFound, "404 Not Found"));
        }

        if (!context.isCompleted()) {
            //System.out.println("help response end:"+Thread.currentThread().getName());
            context.getResponse().end();
        }
    }

    protected void onInit() {
    }

    protected void onError(HttpContext context, Throwable t) {
        HttpException err;
        if (t instanceof HttpException) {
            err = (HttpException) t;
        } else {
            err = new HttpException(HttpStatus.InternalServerError, t);
        }

        ErrorPageGenerator gen = new ErrorPageGenerator();
        gen.status = err.getHttpStatus();
        if (gen.genError && gen.status == HttpStatus.InternalServerError) {
            gen.message = err.getCause() != null ? err.getCause().getMessage() : err.getMessage();
            if (gen.message == null || "".equals(gen.message)) {
                gen.message = err.getMessage();
            }
        } else {
            gen.genError = false;
            //gen.message = "该页面遇到某些问题，不能正常访问，请检查输入项确保请求地址或其它是合法的并重试。<br>如需了解更多细节请联系管理员。";
            gen.message = "Problem accessing <b>" + context.getRequest().rawUrl() + "</b>. <br><b>Try the following:</b>:<ul><li>Check your spelling is correct and try again.</li><li>Contact to administrator and report this problem.</li></ul>";
        }
        try {
            context.getResponse().setHeader("Connection", "close");
            context.getResponse().status(gen.status.getStatus());
        } catch (Exception e) {
            //ignored
        }

        context.getResponse().write(gen.gen(err.getCause()));
        context.getResponse().end();
    }

    protected void onDestory() {

    }

    protected static final class ServerStartupArgs {

        public String serverDirectory;
        public String webappDirectory;
        public String libDirectory;
        public ClassLoader loader;
    }

    protected static final ServerStartupArgs createStartupArgs() {
        return new ServerStartupArgs();
    }

    public final HttpServer getServer() {
        return startupInfo.getServerInstance();
    }

    /**
     * 获取路由控制器包路径。
     *
     * @return
     */
    public URL[] getActionHandlerJarUrls() {
        return this.getLoader().getURLs();
    }

    public String[] getActionNamespaces() {
        return null;
    }

    private ArrayList<Class<? extends ActionHandler>> handlers = new ArrayList<>();

    public ArrayList<Class<? extends ActionHandler>> getActionHandlers() {
        return handlers;
    }

    public void regisiterHandlers(Class<? extends ActionHandler> type) {
        handlers.add(type);
    }

    private class ErrorPageGenerator {

        //public String title;
        public String message;
        //public String caption;
        public boolean genError = true;
        public mano.net.http.HttpStatus status = mano.net.http.HttpStatus.InternalServerError;

        private void genCause(StringBuilder sb, Throwable ex) {
            if (ex == null) {
                return;
            }
            sb.append("<b>Caused by：</b><pre>");
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                pw.println();
                ex.printStackTrace(pw);
            }
            sb.append(sw.toString());
            sb.append("</pre>");
            genCause(sb, ex.getCause());
        }

        public StringBuilder gen(Throwable ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html><head>");
            sb.append("<title>HTTP ").append(status.getStatus()).append(" ERROR</title>");
            sb.append("<style type=\"text/css\">h3{background: #525E76;color: #fff;margin: 0;padding: 5px;}div{min-height:50px; font-size: 12px; line-height: 24px;}pre{background: #FFFFCB;padding: 5px; margin: 0;font-size: 12px;}</style>");
            sb.append("</head><body>");
            sb.append("<h3>HTTP Status ").append(status.getStatus()).append(" - ").append(status.getDescription()).append("</h3>");
            sb.append("<hr/>");

            sb.append("<div>").append(message).append("</div>");

            if (genError && ex != null) {
                sb.append("<b>Exception：</b>");
                sb.append("<pre>");
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    pw.println();
                    ex.printStackTrace(pw);
                }
                sb.append(sw.toString());
                sb.append("</pre>");
                genCause(sb, ex.getCause());
            }

            sb.append("<hr>");
            sb.append("<h3 style=\"font-size: 12px;\">DIOSAY MANO(HTTP Server)/1.4.0-BETA</h3>");
            sb.append("</body></html>");
            return sb;
        }

    }

    private static class AnonymousPrincipal implements Principal {
        
        Identity id = new Identity() {

            @Override
            public Serializable value() {
                return "Anonymous";
            }

            @Override
            public String getAuthenticationType() {
                return "Not authenticated";
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

        };

        @Override
        public Identity getIdentity() {
            return id;
        }

        @Override
        public boolean isInRole(String role) {
            return false;
        }
    }

    /**
     * 获取匿名认证主体。
     */
    protected static final Principal anonymousPrincipal=new AnonymousPrincipal();
    
    /**
     * 获取与当前应用关联的认证用户。
     * @param context
     * @return 
     */
    public Principal getUser(HttpContext context){
        return anonymousPrincipal;
    }
    
}
