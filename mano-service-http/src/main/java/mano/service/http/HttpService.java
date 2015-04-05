/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.service.http;

import java.util.NoSuchElementException;
import mano.io.ChannelHandler;
import mano.io.ChannelListener;
import mano.io.ChannelListenerContext;
import mano.service.Service;
import mano.util.ThreadPool;
import mano.util.xml.XmlException;
import mano.util.xml.XmlHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author sixmoon
 */
public class HttpService extends Service {

    private String serviceName;
    ChannelListenerContext context;

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    protected void onInit() throws Exception {
        serviceName = this.getContext().getProperty("service.name");
        context = new ChannelListenerContext(ThreadPool.getService());
        context.listenerClosedEvent().add((sender, e) -> {
            if (context.size() == 0) {
                stop();
            }
        });

        configure(this.getContext().getProperty(SERVICE_CONFIG_KEY, false));

        System.out.println("HTTP OK");
    }

    void configure(String path) throws XmlException, ClassNotFoundException, ReflectiveOperationException, Exception {
        XmlHelper helper = XmlHelper.load(path);
        Node node, root, node2, node3, tmp;
        NamedNodeMap attrs;
        NodeList nodes, nodes2, nodes3;
        String key, value;
        StringBuilder sb;

        //解析监听配置
        root = helper.selectNode("//listening");
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
                clazz = this.getContext().loadClass(tmp.getNodeValue());
                if (ChannelListener.class.isAssignableFrom(clazz)) {
                    listener = ChannelListener.class.cast(this.getContext().newInstance(clazz));
                } else {
                    throw new ClassCastException("Configuration listener.class must be a subclass of ChannelListener:" + tmp.getNodeValue());
                }
                tmp = node.getAttributes().getNamedItem("address");
                if (tmp == null) {
                    throw new NoSuchElementException("Undefined [address] attribute.");
                }
                listener.bind(tmp.getNodeValue(), 100);
                listener.setContext(context);
                nodes2 = helper.selectNodes(node, "handler");
                if (nodes2 != null) {
                    for (int j = 0; j < nodes2.getLength(); j++) {
                        node2 = nodes2.item(j);
                        tmp = node2.getAttributes().getNamedItem("class");
                        if (tmp == null) {
                            throw new NoSuchElementException("Undefined [class] attribute.");
                        }
                        clazz = this.getContext().loadClass(tmp.getNodeValue());
                        if (ChannelHandler.class.isAssignableFrom(clazz)) {
                            handler = ChannelHandler.class.cast(this.getContext().newInstance(clazz));
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
        root = helper.selectNode("//default");
        if (root == null) {
            throw new NoSuchElementException("Not define [default] node. config file:" + path);
        }
        
        
        

    }

    
    
    
    @Override
    protected void onStart() throws Exception {

        context.getListeners().forEachRemaining(listener -> {
            listener.run();
        });

    }

    @Override
    protected void onStop() throws Exception {
        
    }

}
