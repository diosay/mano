/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web.runtime;

import com.diosay.mano.io.AbstractChannelGroup;
import com.diosay.mano.io.ChannelBuffer;
import com.diosay.mano.io.Listener;
import com.diosay.mano.service.Service;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import mano.ContextClassLoader;
import mano.Mano;
import mano.http.HttpModuleSettings;
import mano.service.ServiceManager;
import mano.util.NameValueCollection;
import mano.util.Pool;
import mano.util.Utility;
import mano.util.logging.Logger;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import mano.web.WebApplicationStartupInfo;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * HTTP服务。
 *
 * @author jun <jun@diosay.com>
 */
public class HttpService extends AbstractChannelGroup implements Service {

    private Properties properties = new Properties();
    private int maxConnections;
    private int bufferSize;
    ContextClassLoader loader;
    private Logger logger;
    private String name;
    Pool<ChannelBuffer> bufferPool;
    WebApplicationStartupInfo machine;
    NameValueCollection<WebApplicationStartupInfo> appInfos;
    private NameValueCollection<ConnectionInfo> infos;

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void run() {
        this.start();
    }

    @Override
    public void init() throws Exception {
        configure();
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(this.getExecutorService());
        for (ConnectionInfo conn : infos.values()) {
            if (conn.disabled) {
                continue;
            }
            this.regsister(new HttpListener())
                    .init(group, conn.address, 32);
            
        }
        this.setHandler(HttpProtocol11.class);
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

    private void configure() throws Exception {

        loader = ServiceManager.getInstance().getLoader();
        logger = loader.getLogger();

        //预处理配置值。
        if (this.getProperties().containsKey("config_file")) {
            this.getProperties().setProperty("config_file", Utility.getAndReplaceMarkup("config_file", this.getProperties(), Mano.getProperties(), System.getProperties()));
        } else {
            throw new java.lang.IllegalArgumentException("未设置配置文件。");
        }

        if (this.getProperties().containsKey("webapp.config_path")) {
            this.getProperties().setProperty("webapp.config_path", Utility.getAndReplaceMarkup("webapp.config_path", this.getProperties(), Mano.getProperties(), System.getProperties()));
        } else {
            this.getProperties().setProperty("webapp.config_path", Utility.toPath(Mano.getProperty("server.dir"), "conf/apps").toString());
        }

        if (this.getProperties().containsKey("service_name")) {
            this.name = this.getProperties().getProperty("service_name");
        } else {
            this.name = this.getClass().getName();
        }

        String s;

        //缓冲区池
        if (this.getProperties().containsKey("buffer_size")) {
            s = this.getProperties().getProperty("buffer_size");
            if (s != null && !"".equals(s.trim())) {
                bufferSize = (int) parseSize(s.trim());
            }
        }

        if (bufferSize == 0) {
            bufferSize = 1024 * 8;
        }

        bufferPool = new Pool<>(() -> {
            return new ChannelBuffer(bufferSize);
        }, 8);

        //最大连接数
        s = this.getProperties().getProperty("max_connections");
        if (s != null && !"".equals(s.trim())) {
            maxConnections = Integer.parseUnsignedInt(s.trim());
        } else {
            maxConnections = 1024;
        }

        //服务配置
        s = this.getProperties().getProperty("config_file");
        File cfile = new File(s);
        if (!cfile.exists() || !cfile.isFile()) {
            throw new FileNotFoundException("Configuration file not found:" + s);
        }

        XmlHelper helper = XmlHelper.load(cfile.toString());
        Node node, attr, root = helper.selectNode("/configuration/http.service");

        //获取机器配置
        machine = new WebApplicationStartupInfo();
        this.parseApplication(machine, helper, helper.selectNode(root, "machine"));

        //web应用的配置文件地址
        s = this.getProperties().getProperty("webapp.config_path");
        cfile = new File(s);
        appInfos = new NameValueCollection<>();
        infos = new NameValueCollection<>();
        if (cfile.exists() && cfile.isDirectory()) {
            cfile.listFiles((File child) -> {
                if (child.getName().toLowerCase().endsWith(".xml")) {
                    try {
                        loadApp(child.toString(), false);
                    } catch (Throwable ex) {
                        logger.error(ex);
                    }
                }
                return false;
            });
        }

        //mano服务器测试专用
        s = Mano.getProperty("manoserver.testing.test_webapp.config_file");
        try {
            if (s != null && !"".equals(s)) {
                loadApp(s, true);
            }
        } catch (Throwable ex) {
            //
        }

        //解析连接地址
        this.getProperties().entrySet().stream().forEach(item -> {
            parseConns(item.getKey().toString(), item.getValue());
        });

    }

    private void parseConns(String name, Object value) {
        String[] arr = Utility.split(name, ":", true);
        if (arr.length > 2) {
            if ("connection".equalsIgnoreCase(arr[0])) {
                ConnectionInfo info;
                if (infos.containsKey(arr[1])) {
                    info = infos.get(arr[1]);
                } else {
                    info = new ConnectionInfo();
                    infos.put(arr[1], info);
                }

                if ("address".equalsIgnoreCase(arr[2])) {
                    String addr = value == null ? "" : value.toString().trim();
                    int index = addr.lastIndexOf(":");
                    if (index < 0) {
                        return;
                    }
                    info.address = new InetSocketAddress(addr.substring(0, index), Integer.parseInt(addr.substring(index + 1)));
                } else if ("disabled".equalsIgnoreCase(arr[2])) {
                    info.disabled = "true".equalsIgnoreCase(value == null ? "" : value.toString().trim());
                }
            }
        }
    }

    private void loadApp(String filename, boolean setable) throws XmlException {
        XmlHelper helper = null;
        Node root = null;
        NamedNodeMap attrs = null;
        Node attr = null;
        if (!setable) {
            helper = XmlHelper.load(filename);
            root = helper.selectNode("/application");
            if (root == null) {
                return;
            }
            attrs = root.getAttributes();
            if (attrs == null) {
                return;
            }
            attr = attrs.getNamedItem("path");
            if (attr == null) {
                throw new XmlException("miss attribute [path]");
            }
        }

        WebApplicationStartupInfo info = new WebApplicationStartupInfo();
        if (!setable) {
            info.rootdir = attr.getNodeValue();
        } else {
            info.rootdir = filename;
        }
        //info.service = this;
        //info.serviceLoader = this.getLoader();
        info.modules.putAll(machine.modules);
        info.settings.putAll(machine.settings);
        info.exports.putAll(machine.exports);
        info.documents.addAll(machine.documents);
        info.ignoreds.addAll(machine.ignoreds);
        info.action = machine.action;
        info.controller = machine.controller;
        info.disabledEntityBody = machine.disabledEntityBody;
        info.maxEntityBodySize = machine.maxEntityBodySize;
        info.serverPath = this.getProperties().getProperty("server.dir");
        String s;
        if (!setable) {
            NodeList nodes = helper.selectNodes(root, "dependency");
            if (nodes != null) {
                Node node;

                for (int i = 0; i < nodes.getLength(); i++) {
                    node = nodes.item(i);
                    attr = node.getAttributes().getNamedItem("path");
                    if (attr != null) {
                        s = attr.getNodeValue();
                        if (s != null && !"".equals(s) && !info.dependencyExt.contains(s)) {
                            info.dependencyExt.add(s);

                        }
                    }
                }
            }
        } else {
            //mano服务器测试专用
            s = Mano.getProperty("manoserver.testing.test_webapp.ext_dependency");
            try {
                if (s != null && !"".equals(s)) {
                    info.dependencyExt.add(s);
                }
            } catch (Throwable ex) {
                //
            }
        }

        File file = new File(info.rootdir + "/WEB-INF/mano.web.xml");
        if (!file.exists() || !file.getName().toLowerCase().endsWith(".xml")) {
            throw new XmlException("Nonreadable file:" + file);
        }
        helper = XmlHelper.load(file.toString());
        root = helper.selectNode("/application");
        if (root == null) {
            throw new XmlException("无效的应用配置文件");
        }
        parseApplication(info, helper, root);
        appInfos.put(info.host, info);
    }

    private void parseApplication(WebApplicationStartupInfo info, XmlHelper helper, Node root) throws XmlException {
        NamedNodeMap attrs;
        String s;
        NodeList nodes;
        Node attr;
        //base
        attrs = root.getAttributes();
        attr = attrs.getNamedItem("name");
        if (attr != null) {
            info.name = attr.getNodeValue();
        }
        attr = attrs.getNamedItem("host");
        if (attr != null) {
            info.host = attr.getNodeValue();
        }
        attr = attrs.getNamedItem("class");
        if (attr != null) {
            info.type = attr.getNodeValue();
        }
        attr = attrs.getNamedItem("vpath");
        if (attr != null) {
            info.path = attr.getNodeValue();
        }

        //配置
        nodes = helper.selectNodes(root, "settings/property");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();
                info.settings.setProperty(attrs.getNamedItem("name").getNodeValue(), nodes.item(i).getTextContent());
            }
        }

        //request
        Node node = helper.selectNode(root, "request");
        if (node != null) {
            attrs = node.getAttributes();
            attr = attrs.getNamedItem("action");
            if (attr != null) {
                info.action = attr.getNodeValue();
            }
            attr = attrs.getNamedItem("controller");
            if (attr != null) {
                info.controller = attr.getNodeValue();
            }
            attr = attrs.getNamedItem("maxEntityBodySize");
            if (attr != null) {
                info.maxEntityBodySize = this.parseSize(attr.getNodeValue().trim());
            }
            attr = attrs.getNamedItem("maxEntityBodySize");
            if (attr != null) {
                info.disabledEntityBody = Utility.cast(Boolean.class, attr.getNodeValue().trim());
            }

            //文档
            nodes = helper.selectNodes(node, "document/add");
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    try {
                        attrs = nodes.item(i).getAttributes();
                        attr = attrs.getNamedItem("value");
                        s = attr == null ? "" : attr.getNodeValue().trim().toLowerCase();
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
                        attr = attrs.getNamedItem("value");
                        s = attr == null ? "" : attr.getNodeValue().trim().toLowerCase();
                        if ("".equals(s) || info.ignoreds.contains(s)) {
                            continue;
                        }
                        info.ignoreds.add(s);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        //依赖
        nodes = helper.selectNodes(root, "dependency/path");
        for (int i = 0; i < nodes.getLength(); i++) {
            attrs = nodes.item(i).getAttributes();
            try {
                s = attrs.getNamedItem("value").getNodeValue().trim();
            } catch (Exception ignored) {
                s = "";
            }
            if (!"".equals(s) && !info.dependency.contains(s)) {
                info.dependency.add(s);
            }
        }

        //导出
        nodes = helper.selectNodes(root, "dependency/export");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                attrs = nodes.item(i).getAttributes();
                try {
                    info.exports.put(attrs.getNamedItem("name").getNodeValue().trim(), attrs.getNamedItem("class").getNodeValue().trim());
                } catch (Exception ex) {
                    logger.debug(ex);
                }
            }
        }

        //模块
        HttpModuleSettings module;
        nodes = helper.selectNodes(root, "modules/add");//remove clear
        for (int i = 0; i < nodes.getLength(); i++) {
            attrs = nodes.item(i).getAttributes();
            attr = attrs.getNamedItem("name");
            s = (attr == null) ? "" : attr.getNodeValue().trim();
            if ("".equals(s) || info.modules.containsKey(s)) {
                logger.warn("module exists:" + s);
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

    @Override
    public ChannelBuffer allocate() {
        ChannelBuffer buffer = bufferPool.get();
        buffer.buffer.clear();
        return buffer;
    }

    @Override
    public void free(ChannelBuffer buffer) {
        if (buffer == null || buffer.buffer.capacity() != bufferSize) {
            return;
        }
        bufferPool.put(buffer);
    }

    private class ConnectionInfo {

        public InetSocketAddress address;
        public boolean disabled = false;
    }
    
    
    
    
    
    public static void main(String[] args) throws Exception {
        HttpService service=new HttpService();
        service.getProperties().setProperty("config_file", "E:\\repositories\\java\\mano\\mano-server-projects\\mano-server\\src\\resources\\conf\\server.xml");
        service.getProperties().setProperty("buffer_size", "8k");
        service.getProperties().setProperty("max_connections", "1024");
        service.getProperties().setProperty("webapp.config_path", "");
        service.getProperties().setProperty("max_connections", "1024");
        service.getProperties().setProperty("connection:http:address", "0.0.0.0:9999");
        service.getProperties().setProperty("connection:http:disabled", "false");
        service.init();
        service.run();
        Thread.sleep(1000 * 60 * 20);
    }
    
}
