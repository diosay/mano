/*
 * Copyright (C) 2015 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.bootstrap;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;
import mano.ContextClassLoader;
import mano.DateTime;
import mano.service.Service;
import mano.service.ServiceContainer;
import mano.util.Utility;
import mano.util.logging.ILogger;
import mano.util.logging.Log;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author johnwhang
 */
public final class Bootstrap {

    class ServiceContainerImpl implements ServiceContainer {

        java.util.LinkedList<Service> services = new java.util.LinkedList<>();

        @Override
        public void add(Service service) {
            if (service == null) {
                throw new NullPointerException("service");
            }
            if (services.contains(service)) {
                throw new IllegalStateException("The same service already exists in the container." + service);
            }
            services.add(service);
        }

        @Override
        public void remove(Service service) {
            if (service == null) {
                throw new NullPointerException("service");
            }
            services.remove(service);
        }

        @Override
        public ServiceContainer run() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        void runService(Service service) {
            mano.util.ThreadPool.execute(service);
        }

        void stop(Service service) {
            service.stop();
        }

        @Override
        public void await() {
            Service service;
            while (true) {
                if (services.isEmpty()) {
                    break;
                }
                //启动未运行的服务
                Iterator<Service> iter = services.iterator();
                while (iter.hasNext()) {
                    service = iter.next();
                    if (!service.isRunning()) {
                        runService(service);
                    }
                    //iter.remove();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    break;
                }
            }
            //结束所有服务
            Iterator<Service> iter = services.iterator();
            while (iter.hasNext()) {
                stop(iter.next());
                iter.remove();
            }
        }

    }
    
    static class TempLogger implements ILogger {

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public boolean isFatalEnabled() {
            return true;
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public void debug(CharSequence message, Throwable... thrown) {
            log(message, thrown);
        }

        @Override
        public void debug(Throwable thrown) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void info(CharSequence message, Throwable... thrown) {
            log(message, thrown);
        }

        @Override
        public void info(Throwable thrown) {
            log("", thrown);
        }

        @Override
        public void warn(CharSequence message, Throwable... thrown) {
            log(message, thrown);
        }

        @Override
        public void warn(Throwable thrown) {
            log("", thrown);
        }

        @Override
        public void error(CharSequence message, Throwable... thrown) {
            log(message, thrown);
        }

        @Override
        public void error(Throwable thrown) {
            log("", thrown);
        }

        @Override
        public void fatal(CharSequence message, Throwable... thrown) {
            log(message, thrown);
        }

        @Override
        public void fatal(Throwable thrown) {
            log("", thrown);
        }

        @Override
        public void trace(CharSequence message, Throwable... causes) {
            log(message, causes);
        }

        @Override
        public void trace(Throwable cause) {
            log("", cause);
        }
    }


    ContextClassLoader baseLoader;
    ServiceContainer container = new ServiceContainerImpl();

    /**
     * 启动项配置
     * @param path
     * @throws XmlException 
     */
    void configure(String path,String serviceConfigDir) throws XmlException {
        baseLoader = new ContextClassLoader(new TempLogger());
        baseLoader.setProperties(System.getProperties());
        Node node, root, tmp;
        NamedNodeMap attrs;
        NodeList nodes;
        String key, value;
        StringBuilder sb;
        XmlHelper helper = XmlHelper.load(path);
        root = helper.selectNode("/configuration");
        if (root == null) {
            throw new NoSuchElementException("Not define [configuration] root node.");
        }
        nodes = helper.selectNodes(root, "settings/property");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                tmp = node.getAttributes().getNamedItem("name");
                if (tmp == null) {
                    throw new NoSuchElementException("Found property node,But not define [name] attribute.");
                }
                key = tmp.getNodeValue();
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
                baseLoader.setProperty(key, Utility.replaceMarkup(new StringBuilder(value), key, baseLoader).toString());
            }
        }

        nodes = helper.selectNodes(root, "dependency/path");
        if (nodes != null) {
            //List<String> list=new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                tmp = node.getAttributes().getNamedItem("value");
                if (tmp == null) {
                    throw new NoSuchElementException("dependency/path not define [value] attribute.");
                }
                value = tmp.getNodeValue();
                if (value == null || "".equals(value)) {
                    throw new NoSuchElementException("Path value cannot be empty");
                }

                baseLoader.register(Utility.replaceMarkup(new StringBuilder(value), null, baseLoader).toString());
            }
        }
        baseLoader.setLogger(Log.get("root"));

        File serviceDir = new File(serviceConfigDir);
        if (serviceDir.exists() && serviceDir.isDirectory()) {
            for (File file : serviceDir.listFiles((f, n) -> {
                System.out.println("" + n);
                return n.toLowerCase().endsWith(".xml");
            })) {
                try {
                    configureService(file);
                } catch (Exception ex) {
                    if (baseLoader.getLogger().isErrorEnabled()) {
                        baseLoader.getLogger().error("Configuration XML file:" + file, ex);
                    }
                }
            }
        }

    }

    /**
     * 读取配置实例化服务
     */
    void configureService(File file) throws Exception {
        XmlHelper helper = XmlHelper.load(file);
        Node node, root, tmp;
        NamedNodeMap attrs;
        NodeList nodes;
        String key, value;
        StringBuilder sb;

        ContextClassLoader loader = new ContextClassLoader(baseLoader);
        root = helper.selectNode("/service");
        if (root == null) {
            throw new NoSuchElementException("Not define [service] root node.");
        }
        nodes = helper.selectNodes(root, "settings/property");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                tmp = node.getAttributes().getNamedItem("name");
                if (tmp == null) {
                    throw new NoSuchElementException("Found property node,But not define [name] attribute.");
                }
                key = tmp.getNodeValue();
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

                loader.setProperty(key, Utility.replaceMarkup(new StringBuilder(value), key, loader).toString());
            }
        }

        nodes = helper.selectNodes(root, "dependency/path");
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                tmp = node.getAttributes().getNamedItem("value");
                if (tmp == null) {
                    throw new NoSuchElementException("dependency/path not define [value] attribute.");
                }
                value = tmp.getNodeValue();
                if (value == null || "".equals(value)) {
                    throw new NoSuchElementException("Path value cannot be empty");
                }

                loader.register(Utility.replaceMarkup(new StringBuilder(value), null, loader).toString());
            }
        }
        loader.setProperty(Service.SERVICE_CONFIG_KEY, file.getCanonicalPath());
        Class<?> clazz = loader.loadClass(loader.getProperty("service.class"));
        if (Service.class.isAssignableFrom(clazz)) {
            Service s = Service.class.cast(loader.newInstance(clazz));
            assert s != null;
            s.init(loader, container);
        } else {
            throw new ClassCastException("Configuration service.class must be a subclass of Service." + loader.getProperty("service.class"));
        }
    }
    
    

    private static void log(CharSequence s, Throwable... err) {
        System.out.println(DateTime.now() + " " + s);
        if (err != null && err.length > 0) {
            for (Throwable e : err) {
                e.printStackTrace(System.out);
            }
        }
    }

    public static void main(String[] args) {
        //ByteBufferAllocator b=new ByteBufferAllocator();
        try {
            if (args == null || args.length == 0) {

                Bootstrap boot = new Bootstrap();
                //TODO:
                String manoConfig="E:\\repositories\\java\\mano\\mano-server-projects\\mano-server\\src\\main\\resources\\conf\\mano.xml";
                String serviceConfig="E:\\repositories\\java\\mano\\mano-server-projects\\mano-server\\src\\main\\resources\\conf\\service";
                //mano.util.Utility.toPath(System.getProperty("mano.dir"), "/conf/service").toFile();
                boot.configure(manoConfig,serviceConfig);
                boot.container.await();
                System.out.println("done");
                System.exit(0);
            } else {

            }
        } catch (XmlException ex) {
            log("configure error", ex);
        }
    }
}
