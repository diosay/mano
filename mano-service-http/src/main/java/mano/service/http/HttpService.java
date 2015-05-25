/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.service.http;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.NoSuchElementException;
import mano.Action;
import mano.caching.CacheEntry;
import mano.caching.HashCacheProvider;
import mano.net.http.HttpModuleSettings;
import mano.io.ChannelHandler;
import mano.io.ChannelListener;
import mano.io.ChannelListenerContext;
import mano.logging.Log;
import mano.runtime.AbstractService;
import mano.runtime.RuntimeClassLoader;
import mano.runtime.Service;
import mano.util.NameValueCollection;
import mano.util.ScheduleTask;
import mano.util.ThreadPool;
import mano.util.Utility;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import mano.web.HttpSession;
import mano.web.WebApplication;
import mano.web.WebApplicationStartupInfo;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sixmoon
 */
public class HttpService extends AbstractService {

    private String serviceName;
    ChannelListenerContext context;
    NameValueCollection<WebApplicationStartupInfo> appInfos;
    WebApplicationStartupInfo machine;
    HashCacheProvider sessionProvider;
    long checkSessionTime = 0;
    RuntimeClassLoader loader;
    public static final String REQUEST_HANDLER_PROVIDER_KEY = "REQUEST_HANDLER_PROVIDER_KEY";

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Service setProperty(String key, Object value) {
        super.setProperty(key, value);
        if (Service.PROP_CLASS_LOADER.equalsIgnoreCase(key)) {
            loader = (RuntimeClassLoader) value;
        }

        return this;
    }

    public String getPropertyStringValue(String key) {
        Object val = this.getProperty(key, null);
        if (val == null) {
            return null;
        }
        return val.toString().trim();
    }

    @Override
    protected void onInit() throws Exception {
        if (loader == null) {
            throw new NullPointerException("unset class loader , KEY:" + Service.PROP_CLASS_LOADER);
        }
        serviceName = getPropertyStringValue("service.name");
        context = new ChannelListenerContext(ThreadPool.getService());
        context.listenerClosedEvent().add((sender, e) -> {
            if (context.size() == 0) {
                stop();
            }
        });

        machine = new WebApplicationStartupInfo();
        machine.version = "ManoServer/1.4";
        machine.serverPath = System.getProperty("mano.dir");
        appInfos = new NameValueCollection<>();
        configure(getPropertyStringValue(Service.PROP_CONFIG_FILE));
        
        //基于内存的session
        this.sessionProvider = new HashCacheProvider();
        Action<CacheEntry> removeExpiredSession = (entry) -> {
            if (entry.isExpired()) {
                sessionProvider.remove(entry.getKey());
            }
        };
        ScheduleTask.register((ctime) -> {
            if (ctime - checkSessionTime >= 300000) {//                
                checkSessionTime = ctime;
                sessionProvider.forEach(removeExpiredSession);
            }
            return !isRunning();
        });

        
        HttpRequestHandlerAdapter adapter = (ctx, eh) -> {
            context.getExecutor().execute(() -> {
                long begin=System.currentTimeMillis();
                String oname=Thread.currentThread().getName();
                Thread.currentThread().setName("HTTP Request Worker");
                try {
                    String host = ctx.getRequest().headers().get("Host").value();
                    WebApplicationStartupInfo info = null;
                    for (WebApplicationStartupInfo i : appInfos.values()) {
                        if (i.matchHost(host)) {
                            info = i;
                            break;
                        }
                    }
                    if (info == null) {
                        info = appInfos.get("*");
                    }
                    WebApplication app = info == null ? null : info.getInstance();
                    if (app != null) {
                        ctx.server = app.getServer();
                        ctx.app = app;
                        ctx.session = HttpSession.getSession(ctx.request.getCookie().get(HttpSession.COOKIE_KEY), sessionProvider);
                        if (ctx.session.isNewSession()) {
                            ctx.response.getCookie().set(HttpSession.COOKIE_KEY, ctx.session.getSessionId(), 0, "/", null, false, false);
                        }
                        ctx.response.setHeader("Server", ctx.server.getVersion());
                        app.processRequest(ctx);
                    } else {
                        throw new java.lang.IllegalStateException("未找到应用");
                    }
                } catch (Throwable t) {
                    eh.handleError(t);
                }finally{
                    Thread.currentThread().setName(oname);
                }
//                if (Log.TRACE.isTraceEnabled()) {
//                    Log.TRACE.trace("Request("+ctx.getRequestId()+") handing done,total times:" + (System.currentTimeMillis() - begin) + "ms");
//                }
            });
        };
        context.items().put(REQUEST_HANDLER_PROVIDER_KEY, adapter);
    }

    boolean getBoolean(Node node, boolean def) {
        if (node == null) {
            return def;
        }
        try {
            return Boolean.parseBoolean(node.getNodeValue());
        } catch (Throwable t) {
            return def;
        }
    }

    void configureDependency(XmlHelper helper, Node root, RuntimeClassLoader loader, Map... props) throws XmlException {
        Node node, tmp, attr, node2;
        NamedNodeMap attrs;
        NodeList nodes;
        String key, value;
        StringBuilder sb;
        nodes = helper.selectNodes(root, "dependency/path");
        if (nodes != null) {
            //List<String> list=new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                tmp = node.getAttributes().getNamedItem("type");
                if (tmp == null) {
                    throw new NoSuchElementException("dependency/path not define [type] attribute.");
                }
                key = tmp.getNodeValue();
                if (key == null || "".equals(key)) {
                    throw new NoSuchElementException("Path type cannot be empty.");
                }

                value = node.getTextContent();
                if (value == null || "".equals(value)) {
                    continue;
                }
                for (Map prop : props) {
                    try {
                        value = Utility.replaceMarkup(new StringBuilder(value), null, prop).toString();
                        break;
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                if ("jar".equalsIgnoreCase(key)) {
                    loader.addJars(getBoolean(node.getAttributes().getNamedItem("recursive"), false), value);
                } else if ("classes".equalsIgnoreCase(key)) {
                    loader.addJars(value);
                } else if ("url".equalsIgnoreCase(key)) {
                    try {
                        loader.AddUrl(value);
                    } catch (URISyntaxException | MalformedURLException ex) {
                        throw new NoSuchElementException("Path value cannot be to a URL." + ex.getMessage());
                    }
                } else {
                    throw new NoSuchElementException("Path type unsupport:" + key);
                }
            }
        }
    }

    void configureProp(XmlHelper helper, Node root, Map coll, Map... props) throws XmlException {
        Node node, tmp, attr, node2;
        NamedNodeMap attrs;
        NodeList nodes;
        String key, value;
        StringBuilder sb;
        nodes = helper.selectNodes(root, "settings/property");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                attr = node.getAttributes().getNamedItem("name");
                if (attr == null) {
                    throw new NoSuchElementException("Found property node,But not define [name] attribute.");
                }
                key = attr.getNodeValue();
                if (key == null || "".equals(key)) {
                    throw new NoSuchElementException("Property name cannot be empty");
                }
                value = node.getNodeValue();
                if (value == null) {
                    value = node.getTextContent();
                }
                if (value == null || "".equals(value)) {
                    throw new NoSuchElementException("Property value cannot be empty");
                }
                for (Map prop : props) {
                    try {
                        value = Utility.replaceMarkup(new StringBuilder(value), null, prop).toString();
                        break;
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
                coll.put(key, value);
            }
        }
    }

    //@GuardedBy("this")
    void configure(String path) throws XmlException, ClassNotFoundException, ReflectiveOperationException, Exception {
        XmlHelper helper = XmlHelper.load(path);
        Node node, root, node2, node3, tmp;
        NamedNodeMap attrs;
        NodeList nodes, nodes2, nodes3;
        String key, value;
        StringBuilder sb;
        String s;
        //解析监听配置
        root = helper.selectNode("/service/listeners");
        if (root == null) {
            throw new NoSuchElementException("Not define [listening] node. config file:" + path);
        }
        nodes = helper.selectNodes(root, "listener");
        if (nodes != null) {
            ChannelListener listener;
            Class<?> clazz;
            ChannelHandler handler;
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);

                tmp = node.getAttributes().getNamedItem("disabled");
                if (tmp != null && "true".equalsIgnoreCase(tmp.getNodeValue())) {
                    continue;
                }
                tmp = node.getAttributes().getNamedItem("class");
                if (tmp == null) {
                    throw new NoSuchElementException("Undefined [class] attribute.");
                }
                clazz = loader.loadClass(tmp.getNodeValue());
                if (ChannelListener.class.isAssignableFrom(clazz)) {
                    listener = ChannelListener.class.cast(loader.newInstance(clazz));
                } else {
                    throw new ClassCastException("Configuration listener.class must be a subclass of ChannelListener:" + tmp.getNodeValue());
                }
                tmp = node.getAttributes().getNamedItem("address");
                if (tmp == null) {
                    throw new NoSuchElementException("Undefined [address] attribute.");
                }
                s = tmp.getNodeValue();
                tmp = node.getAttributes().getNamedItem("backlog");
                int backlog = 128;
                if (tmp != null) {
                    backlog = Integer.parseInt(tmp.getNodeValue());
                }
                listener.bind(s, backlog);
                listener.setContext(context);
                nodes2 = helper.selectNodes(node, "handler");
                if (nodes2 != null) {
                    for (int j = 0; j < nodes2.getLength(); j++) {
                        node2 = nodes2.item(j);
                        tmp = node2.getAttributes().getNamedItem("class");
                        if (tmp == null) {
                            throw new NoSuchElementException("Undefined [class] attribute.");
                        }
                        clazz = loader.loadClass(tmp.getNodeValue());
                        if (ChannelHandler.class.isAssignableFrom(clazz)) {
                            handler = ChannelHandler.class.cast(loader.newInstance(clazz));
                        } else {
                            throw new ClassCastException("Configuration handler.class must be a subclass of ChannelHandler: " + tmp.getNodeValue());
                        }
                        nodes3 = helper.selectNodes(node2, "property");
                        if (nodes3 != null) {
                            for (int k = 0; k < nodes3.getLength(); k++) {
                                node3 = nodes3.item(k);
                                tmp = node3.getAttributes().getNamedItem("name");
                                if (tmp == null) {
                                    throw new NoSuchElementException("Undefined [name] attribute.");
                                }
                                handler.setProperty(tmp.getNodeValue(), node3.getNodeValue() != null ? node3.getNodeValue() : node3.getTextContent());
                            }
                        }
                        handler.init();
                        listener.addHandler(handler);
                    }
                }
                context.addListener(listener);
            }
        }

        //解析默认应用配置
        root = helper.selectNode("/service/default");
        if (root == null) {
            throw new NoSuchElementException("Not define [default] node. config file:" + path);
        }

        configApp(machine, helper, root, true);//"E:\\repositories\\java\\mano\\mano-bootstrap\\src\\main\\resources\\webapps"
        File configFolder = new File(getPropertyStringValue("app.config.dir"));
        //configFolder = new File("E:\\repositories\\java\\mano\\mano-bootstrap\\src\\main\\resources\\webapps");
        if (configFolder.exists() && configFolder.isDirectory()) {
            File[] xmlFiles = configFolder.listFiles((cfile) -> {
                return cfile.exists() && cfile.isFile() && cfile.getName().toLowerCase().endsWith(".xml");
            });
            File[] warFiles = configFolder.listFiles((cfile) -> {
                return cfile.exists() && cfile.isFile() && cfile.getName().toLowerCase().endsWith(".war");
            });
            for (File cfile : xmlFiles) {
                XmlHelper chelper = XmlHelper.load(cfile.toString());
                root = chelper.selectNode("/application");
                if (root != null) {
                    node = chelper.selectNode(root, "type");
                    String type = node == null ? "" : node.getTextContent().trim().toLowerCase();
                    if ("".equals(type)) {
                        type = "folder";
                    }
                    node = chelper.selectNode(root, "path");
                    String dpath = node == null ? "" : node.getTextContent().trim().toLowerCase();

                    if ("war".equalsIgnoreCase(type)) {
                        node = chelper.selectNode(root, "packageName");
                        String pname = node == null ? "" : node.getTextContent().trim().toLowerCase();
                        node = chelper.selectNode(root, "packageTime");
                        String ptimes = node == null ? "" : node.getTextContent().trim().toLowerCase();
                        long ptime = "".equals(ptimes) ? 0 : Long.parseLong(ptimes);

                    } else {
                        File appcfg = mano.util.Utility.toPath(dpath, "WEB-INF/app.xml").toFile();
                        if (appcfg.exists() && appcfg.isFile()) {
                            XmlHelper appHelper = XmlHelper.load(appcfg.toString());
                            root = appHelper.selectNode("/application");
                            if (root != null) {
                                WebApplicationStartupInfo app = new WebApplicationStartupInfo();
                                app.action = machine.action;
                                app.controller = machine.controller;
                                app.dependency.addAll(machine.dependency);
                                app.dependencyExt.addAll(machine.dependencyExt);
                                app.disabledEntityBody = machine.disabledEntityBody;
                                app.documents.addAll(machine.documents);
                                app.errorDefaultPage = machine.errorDefaultPage;
                                app.errorMode = machine.errorMode;
                                app.errorPages.putAll(machine.errorPages);
                                app.host = machine.host;
                                app.ignoreds.addAll(machine.ignoreds);
                                app.maxEntityBodySize = machine.maxEntityBodySize;
                                app.maxPostFileSize = machine.maxPostFileSize;
                                app.modules.putAll(machine.modules);
                                app.name = machine.name;
                                app.path = machine.path;
                                app.rootdir = dpath;
                                app.serverPath = machine.serverPath;
                                app.type = machine.type;
                                app.version = machine.version;
                                this.configApp(app, appHelper, root, false);
                                if ("".equals(app.name) || appInfos.containsKey(app.name)) {
                                    throw new XmlException("存在多个同名应用：" + app.name);
                                }
                                app.service = this;
                                appInfos.put(app.name, app);
                            } else {

                            }
                        } else {

                        }
                    }
                }
            }
        }
    }

    private void configApp(WebApplicationStartupInfo info, XmlHelper helper, Node root, boolean isMachine) throws XmlException {
        Node node, node2, node3, tmp;
        NamedNodeMap attrs;
        NodeList nodes, nodes2, nodes3;
        String key, value;
        StringBuilder sb;
        String s;

        if (!isMachine) {
            node = helper.selectNode(root, "settings");
            if (node != null) {
                nodes = helper.selectNodes(node, "property");
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        node2 = nodes.item(i);
                        tmp = node2.getAttributes().getNamedItem("name");
                        key = tmp == null ? "" : tmp.getNodeValue().trim();
                        value = node2.getTextContent();
                        if (!"".equals(key) && !"".equals(value)) {
                            info.settings.setProperty(key, value);
                        }
                    }
                }
            }
            info.name = info.settings.getProperty("application.name", "");
            info.host = info.settings.getProperty("application.host", "*");
            info.path = info.settings.getProperty("application.vpath", "/");
            info.type = info.settings.getProperty("application.class", "mano.web.WebAppcation");

            //依赖
//            nodes = helper.selectNodes(root, "dependency/path");
//            for (int i = 0; i < nodes.getLength(); i++) {
//                attrs = nodes.item(i).getAttributes();
//                try {
//                    s = attrs.getNamedItem("value").getNodeValue().trim();
//                } catch (Exception ignored) {
//                    s = "";
//                }
//                //System.out.println("sssssss:"+s);
//                //s = Utility.replaceMarkup(new StringBuilder(s), null, loader).toString();
//                if (!"".equals(s) && !info.dependency.contains(s)) {
//                    info.dependency.add(s);
//                }
//            }
        }

        node = helper.selectNode(root, "request");
        if (node != null) {
            tmp = node.getAttributes().getNamedItem("disabledEntityBody");
            if (tmp != null && "true".equalsIgnoreCase(tmp.getNodeValue())) {
                info.disabledEntityBody = true;
            }
            tmp = node.getAttributes().getNamedItem("maxEntityBodySize");
            if (tmp != null && !"".equalsIgnoreCase(tmp.getNodeValue())) {
                info.maxEntityBodySize = parseSize(tmp.getNodeValue());
            }
            tmp = node.getAttributes().getNamedItem("action");
            if (tmp != null) {
                info.action = tmp.getNodeValue();
            }
            tmp = node.getAttributes().getNamedItem("controller");
            if (tmp != null) {
                info.controller = tmp.getNodeValue();
            }
            //文档
            nodes = helper.selectNodes(node, "document/add");
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    try {
                        attrs = nodes.item(i).getAttributes();
                        tmp = attrs.getNamedItem("value");
                        s = tmp == null ? "" : tmp.getNodeValue().trim().toLowerCase();
                        if ("".equals(s) || info.documents.contains(s)) {
                            continue;
                        }
                        info.documents.add(s);
                    } catch (Exception ignored) {
                    }
                }
            }
            //忽略
            nodes = helper.selectNodes(node, "ignored/add");
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    try {
                        attrs = nodes.item(i).getAttributes();
                        tmp = attrs.getNamedItem("value");
                        s = tmp == null ? "" : tmp.getNodeValue().trim().toLowerCase();
                        if ("".equals(s) || info.ignoreds.contains(s)) {
                            continue;
                        }
                        info.ignoreds.add(s);
                    } catch (Exception ignored) {
                    }
                }
            }
            //错误
            node2 = helper.selectNode(node, "errors");
            if (node2 != null) {
                tmp = node2.getAttributes().getNamedItem("mode");
                if (tmp != null) {
                    if ("off".equalsIgnoreCase(tmp.getNodeValue())) {
                        info.errorMode = "off";
                    } else if ("on".equalsIgnoreCase(tmp.getNodeValue())) {
                        info.errorMode = "on";
                    } else if ("remoteOnly".equalsIgnoreCase(tmp.getNodeValue())) {
                        info.errorMode = "remoteOnly";
                    } else {
                        throw new XmlException("Error mode value [" + tmp.getNodeValue() + "] is undefined.");
                    }
                }
                tmp = node2.getAttributes().getNamedItem("defaultPath");
                if (tmp != null) {
                    info.errorDefaultPage = tmp.getNodeValue();
                }

                nodes = helper.selectNodes(node2, "error");
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        try {
                            attrs = nodes.item(i).getAttributes();
                            tmp = attrs.getNamedItem("code");
                            key = tmp == null ? "" : tmp.getNodeValue().trim().toLowerCase();
                            tmp = attrs.getNamedItem("path");
                            value = tmp == null ? "" : tmp.getNodeValue().trim().toLowerCase();
                            if (!"".equals(key)) {
                                info.errorPages.put(key, value);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            //模块
            HttpModuleSettings module;
            nodes = helper.selectNodes(root, "modules/add");//remove clear
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();
                tmp = attrs.getNamedItem("name");
                s = (tmp == null) ? "" : tmp.getNodeValue().trim();
                if ("".equals(s) || info.modules.containsKey(s)) {
                    continue;
                }

                module = new HttpModuleSettings();
                module.name = s;
                module.type = attrs.getNamedItem("class").getNodeValue();
                NodeList params = helper.selectNodes(nodes.item(i), "property");
                for (int j = 0; j < params.getLength(); j++) {
                    attrs = params.item(j).getAttributes();
                    module.settings.setProperty(attrs.getNamedItem("name").getNodeValue(), params.item(j).getTextContent());
                }
                info.modules.put(s, module);
            }
        }

    }

    private long parseSize(String size) {
        long result;
        if (size.endsWith("M") || size.endsWith("m")) {
            result = Long.parseUnsignedLong(size.substring(0, size.length() - 1)) * (1024 * 1024);
        } else if (size.endsWith("K") || size.endsWith("k")) {
            result = Long.parseUnsignedLong(size.substring(0, size.length() - 1)) * 1024;
        } else if (size.endsWith("B") || size.endsWith("b")) {
            result = Long.parseUnsignedLong(size.substring(0, size.length() - 1));
        } else {
            result = Long.parseUnsignedLong(size);
        }
        return result;
    }

    @Override
    protected void onStart() throws Exception {
        context.getListeners().forEachRemaining(listener -> {
            //listener.run();
            context.getExecutor().execute(listener);
        });
    }

    @Override
    protected void onStop() throws Exception {

        for (WebApplicationStartupInfo info : appInfos.values()) {
            if (info != null && info.app != null) {
                //info.app.destory();
            }
        }

    }

}
