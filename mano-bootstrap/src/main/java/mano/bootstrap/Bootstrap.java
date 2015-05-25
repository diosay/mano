/*
 * Copyright (C) 2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import mano.logging.Handler;
import mano.logging.Log;
import mano.runtime.Service;
import mano.runtime.ServiceContainer;
import mano.util.ThreadPool;
import mano.util.Utility;
import mano.logging.LogService;
import mano.runtime.RuntimeClassLoader;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author johnwhang
 */
public final class Bootstrap extends mano.runtime.Bootstrap {

    java.util.ArrayList<Service> services = new java.util.ArrayList<>();
    Log log;
    RuntimeClassLoader rootLoader = new RuntimeClassLoader();

    @Override
    protected void doStart(String[] args) throws Exception {
        //-b set mano.dir
        //-m management port
        //-v show version
        //-h show help
        //-? show help
        if (args == null || args.length == 0) {
            init(Utility.toPath(System.getProperty("user.dir"), "../").toFile());
        } else if (args.length == 1 && "-develop".equalsIgnoreCase(args[0])) {
            init(Utility.toPath("E:\\repositories\\java\\mano\\mano-bootstrap\\src\\main\\resources").toFile());
        } else if (args.length == 2 && "-base".equalsIgnoreCase(args[0])) {
            init(Utility.toPath(args[1]).toFile());
        } else {
            throw new java.lang.RuntimeException("参数不正确。");
        }

//        Thread logThread = new Thread(new LogService());
//        logThread.setDaemon(true);
//        logThread.setPriority(Thread.MIN_PRIORITY);
//        logThread.start();
        new LogService().run();
        Thread.sleep(500);

        configure();

        ServiceContainer.sizeChanged().add((size, e) -> {
            if (size <= 0) {
                stop();
            }
        });

        if (services.isEmpty()) {
            log.trace("No running any service. exit mano ...");
            stop();
        } else {
            services.forEach(svc -> {
                ThreadPool.execute(svc);
            });
        }

//        mano.logging.Log ll = new mano.logging.Log("default");
//        ll.setHandler(new mano.logging.FileHandler("D:\\tmp"));
//        ll.info("Server started success.", new java.lang.IllegalAccessException("禁止访问"));
//        ll.info("Ssdggd.", new java.lang.IllegalAccessException("禁止棒球城下访问"));
//        todo:
        log.info("Server started success.");

        //container.await();
    }

    @Override
    protected void doStop() throws Exception {
        Thread.sleep(1000);
        System.exit(0);
    }

    void init(File base) throws FileNotFoundException {
        if (base != null && base.exists() && base.isDirectory()) {
            System.setProperty("mano.dir", base.toPath().toString());
        } else {
            throw new java.io.FileNotFoundException("Invalid base path. mano.dir:" + base);
        }
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
                value = Utility.replaceMarkup(new StringBuilder(value), key, props).toString();
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
                coll.put(key, Utility.replaceMarkup(new StringBuilder(value), key, props).toString());
            }
        }
    }

    /**
     * 启动项配置 recursive
     *
     * @param path
     * @throws XmlException
     */
    void configure() throws Exception {
        Node node, root, tmp, attr, node2;
        NamedNodeMap attrs;
        NodeList nodes;
        String key, value;
        StringBuilder sb;
        XmlHelper helper = XmlHelper.load(Utility.toPath(System.getProperty("mano.dir"), "conf/mano.xml").toString());
        root = helper.selectNode("/configuration");
        if (root == null) {
            throw new NoSuchElementException("Not define [configuration] node.");
        }

        //属性
        this.configureProp(helper, root, System.getProperties(), System.getProperties());

        //依赖
        this.configureDependency(helper, root, rootLoader, System.getProperties());

        //日志
        node = helper.selectNode(root, "logging");
        if (node != null) {
            tmp = helper.selectNode(node, "category");
            if (tmp != null) {
                value = tmp.getTextContent() == null ? "" : tmp.getTextContent().trim();
                if ("".equals(value)) {
                    throw new NoSuchElementException("logging category value cannot be empty.");
                } else {
                    log = new Log(value);
                }
            } else {
                log = new Log("default");
            }
            tmp = helper.selectNode(node, "handler");
            if (tmp != null) {
                attr = tmp.getAttributes().getNamedItem("class");
                if (attr == null) {
                    throw new NoSuchElementException("logging/handler not define [class] attribute.");
                }
                value = attr.getNodeValue();
                Handler handler = Handler.class.cast(rootLoader.newInstance(rootLoader.loadClass(value)));
                handler.setProperty("ref.loader", value);
                nodes = helper.selectNodes(tmp, "property");
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        node2 = nodes.item(i);
                        attr = node2.getAttributes().getNamedItem("name");
                        if (attr == null) {
                            throw new NoSuchElementException("Found property node,But not define [name] attribute.");
                        }
                        key = attr.getNodeValue();
                        if (key == null || "".equals(key)) {
                            throw new NoSuchElementException("Property name cannot be empty");
                        }
                        value = node2.getNodeValue();
                        if (value == null) {
                            value = node.getTextContent();
                        }
                        if (value == null || "".equals(value)) {
                            throw new NoSuchElementException("Property value cannot be empty");
                        }
                        value = Utility.replaceMarkup(new StringBuilder(value), key, System.getProperties()).toString();
                        handler.setProperty(key, value);
                    }
                    log.setHandler(handler);
                }
            }

        } else {
            log = new Log("default");
        }

        try {
            File serviceDir = Utility.toPath(System.getProperty("mano.service.config.dir")).toFile();
            if (serviceDir.exists() && serviceDir.isDirectory()) {
                for (File file : serviceDir.listFiles((f, n) -> {
//                    System.out.println("" + n);
                    return n.toLowerCase().endsWith(".xml");
                })) {
                    try {
                        configureService(file);
                    } catch (Exception ex) {
                        log.trace("Configuration XML file:" + file, ex);
                    }
                }
            } else {
                throw new NoSuchElementException("Service config dir not exists." + serviceDir);
            }
        } catch (Throwable t) {
            log.trace("Failed to config services. path:" + System.getProperty("mano.service.config.dir"), t);
        }

    }

    void configureService(File file) throws Exception {
        XmlHelper helper = XmlHelper.load(file);
        Node node, root, tmp, attr;
        NamedNodeMap attrs;
        NodeList nodes;
        String key, value;
        StringBuilder sb;

        RuntimeClassLoader loader = new RuntimeClassLoader(rootLoader);

        root = helper.selectNode("/service");
        if (root == null) {
            throw new NoSuchElementException("Not define [service] node.");
        }
        attr = root.getAttributes().getNamedItem("class");
        if (attr == null || attr.getNodeValue() == null) {
            throw new NoSuchElementException("Found property node,But not define [class] attribute.");
        }

        //属性
        Map<String, String> prop = new HashMap<>();
        this.configureProp(helper, root, prop, prop, System.getProperties());
        loader.setName(prop.getOrDefault("server.name", "service class loader:" + file.getName()));
        //依赖
        this.configureDependency(helper, root, loader, prop, System.getProperties());

        Service service;
        Class<?> clazz = loader.loadClass(attr.getNodeValue().trim());
        if (Service.class.isAssignableFrom(clazz)) {
            service = Service.class.cast(loader.newInstance(clazz));
            assert service != null;
            service.setProperty(Service.PROP_CONFIG_FILE, file);
            service.setProperty(Service.PROP_CLASS_LOADER, loader);
        } else {
            throw new ClassCastException("Configuration service.class must be a subclass of Service. class:" + attr.getNodeValue());
        }

        for (Map.Entry item : prop.entrySet()) {
            service.setProperty(item.getKey().toString(), item.getValue());
        }

        services.add(service);
    }

    public static void main(String[] args) {
        Bootstrap boot = new Bootstrap();
        boot.start(args);
    }
}
