/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import mano.ContextClassLoader;
import mano.Mano;
import mano.PropertyContext;
import mano.net.http.HttpContext;
import mano.net.http.HttpException;
import mano.net.http.HttpModule;
import mano.net.http.HttpModuleSettings;
import mano.net.http.HttpServer;
import mano.net.http.HttpStatus;
import mano.util.Utility;
import mano.util.logging.ILogger;
import mano.util.logging.Logger;

/**
 * 定义 Web 应用程序中的所有应用程序对象通用的方法、属性和事件。
 *
 * @author jun <jun@diosay.com>
 */
public class WebApplication extends PropertyContext {

    private Set<HttpModule> modules;
    private ContextClassLoader loader;
    private WebApplicationStartupInfo startupInfo;
    private final Map<String, Object> items = new ConcurrentHashMap<>();

    public ContextClassLoader getLoader() {
        return loader;
    }

    public ILogger getLogger() {
        return loader.getLogger();
    }

    /**
     * 初始化应用程序。
     *
     * @param info
     * @param l
     */
    final void init(WebApplicationStartupInfo info, ContextClassLoader l) {
        startupInfo = info;
        loader = l;
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
            //context.getSession().get("");
            //throw new java.lang.RuntimeException(new HttpException(HttpStatus.NotFound, new java.lang.IllegalMonitorStateException("404 Not Found")));
            this.onError(context, new HttpException(HttpStatus.NotFound, new java.lang.RuntimeException(new HttpException(HttpStatus.NotFound, new java.lang.IllegalMonitorStateException("404 Not Found")))));
        }

        if (!context.isCompleted()) {
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

        ErrorPageGrec gen = new ErrorPageGrec();
        gen.status = err.getHttpStatus();
        if (gen.genError && gen.status == HttpStatus.InternalServerError) {
            gen.message = err.getCause() != null ? err.getCause().getMessage() : err.getMessage();
            if (gen.message == null || "".equals(gen.message)) {
                gen.message = err.getMessage();
            }
        } else {
            gen.genError = false;
            gen.message = "该页面遇到某些问题，不能正常访问，请检查输入项确保请求地址或其它是合法的并重试。<br>如需了解更多细节请联系管理员。";
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

    private void printRoot(StringBuilder sb, Throwable t) {
        if (t == null) {
            return;
        }
        sb.append("<b>root</b><p><pre>");
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            pw.println();
            t.printStackTrace(pw);
        }
        sb.append(sw.toString());
        sb.append("</pre></p>");
        printRoot(sb, t.getCause());
    }

    protected void onError2(HttpContext context, Throwable t) {
        StringBuilder sb = new StringBuilder();
        if (t instanceof HttpException) {
            HttpException ex = (HttpException) t;
            if (!context.getResponse().headerSent()) {
                sb.append("<html><head><title>")
                        .append(ex.getHttpStatus().getStatus())
                        .append(" Error")
                        .append("</title></head><body>");
                context.getResponse().status(ex.getHttpStatus().getStatus(), ex.getHttpStatus().getDescription());
            }
            if (ex.getMessage() != null) {
                sb.append("<b>message</b><u>")
                        .append(ex.getMessage())
                        .append("</u>");
            }
            if (ex.getCause() != null) {
                sb.append("<b>exception</b><p><pre>");
                StringWriter sw = new StringWriter();
                try (PrintWriter pw = new PrintWriter(sw)) {
                    pw.println();
                    ex.getCause().printStackTrace(pw);
                }
                sb.append(sw.toString());
                sb.append("</pre></p>");
                printRoot(sb, ex.getCause().getCause());
            }
            if (!context.getResponse().headerSent()) {
                sb.append("<hr>")
                        .append(context.getServer().getVersion());
            }
        } else {
            if (!context.getResponse().headerSent()) {
                sb.append("<html><head><title>")
                        .append(HttpStatus.InternalServerError.getStatus())
                        .append(" Error")
                        .append("</title></head><body>");
                context.getResponse().status(HttpStatus.InternalServerError.getStatus(), HttpStatus.InternalServerError.getDescription());
            }
            if (t.getMessage() != null) {
                sb.append("<b>message</b><u>")
                        .append(t.getMessage())
                        .append("</u>");
            }
            sb.append("<b>exception</b><p><pre>");
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                pw.println();
                t.printStackTrace(pw);
            }
            sb.append(sw.toString());
            sb.append("</pre></p>");
            printRoot(sb, t.getCause());
            if (!context.getResponse().headerSent()) {
                sb.append("<hr>")
                        .append(context.getServer().getVersion());
            }
        }
        if (!context.getResponse().headerSent()) {

        }
        try {
            context.getResponse().setHeader("Connection", "close");
        } catch (Exception e) {
            //ignored
        }

        try {
            context.getResponse().write(sb.toString());
        } catch (Exception e) {
            //ignored
        }
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

    private ArrayList<Class<? extends ActionHandler>> handlers = new ArrayList<>();

    public ArrayList<Class<? extends ActionHandler>> getActionHandlers() {
        return handlers;
    }

    public void regisiterHandlers(Class<? extends ActionHandler> type) {
        handlers.add(type);
    }

    /**
     * 启动调试服务
     *
     * @param args
     */
    protected static void startDebugServer(ServerStartupArgs args) {
        try {

            Mano.setProperty("manoserver.testing.test_webapp.config_file", args.webappDirectory);
            Mano.setProperty("manoserver.testing.test_webapp.ext_dependency", args.libDirectory);

            ContextClassLoader loader = new ContextClassLoader(null, new URL[0], args.loader);
            loader.register(Utility.toPath(args.serverDirectory, "bin").toString());
            loader.register(Utility.toPath(args.serverDirectory, "lib").toString());
            Class<?> instance = loader.loadClass("com.diosay.mano.server.Bootstrap");

            Method startup = instance.getDeclaredMethod("debugStart", String.class, String.class, ContextClassLoader.class);
            if (startup == null) {

            }
            startup.setAccessible(true);

            startup.invoke(null, Utility.toPath(args.serverDirectory, "conf\\server.xml").toString(), args.serverDirectory, loader);

        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
        }
    }

    class ErrorPageGrec {

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
            sb.append("<h3 style=\"font-size: 12px;\">DIOSAY MANO(HTTP Server)/0.4beta</h3>");
            sb.append("</body></html>");
            return sb;
        }

    }

}
