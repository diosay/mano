/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import mano.util.logging.ILogger;

/**
 * 上下文类加载器
 *
 * @author jun <jun@diosay.com>
 */
public class ContextClassLoader extends URLClassLoader implements IProperties {

    private ILogger logger;
    private ContextClassLoader parent;

    private ContextClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public ContextClassLoader(ILogger logger, URL[] urls, ClassLoader parent) {
        this(urls, parent);
        if (logger == null) {
            throw new IllegalArgumentException("logger");
        }
        this.logger = logger;
    }

    public ContextClassLoader(ILogger logger) {
        this(logger, new URL[0], ContextClassLoader.class.getClassLoader());
    }

    public ContextClassLoader(ContextClassLoader parent) {
        this(new URL[0], parent.getParent());
        this.parent = parent;
    }

    /**
     * 获取上下文日志器。
     */
    public ILogger getLogger() {
        return this.logger != null ? this.logger : (this.parent == null ? null : this.parent.getLogger());
    }

    /**
     * 设置日志器到上下文。
     *
     * @param logger
     */
    public void setLogger(ILogger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("logger");
        }
        this.logger = logger;
    }

    /**
     * 获取父级加载器。
     */
    public ContextClassLoader getParentLoader() {
        return this.parent;
    }

    /**
     * 注册类路径到上下文。
     *
     * @param paths
     */
    public void register(String... paths) {
        if (paths == null || paths.length == 0) {
            return;
        }

        File file;
        final Set<URL> set = new HashSet<>();
        String name;
        for (String path : paths) {
            file = new File(path);
            if (!file.exists()) {
                try {
                    set.add(new URI(path).toURL());
                } catch (Throwable ex) {
                    getLogger().warn("Invalid class path(Not found or Incorrect URL) :" + path);
                }
                continue;
            } else if (!file.isDirectory()) {
                name = file.getName().toLowerCase();
                if (!(name.endsWith(".jar") || name.endsWith(".class"))) {
                    getLogger().warn("Invalid class path(Is not a valid JAR file or Class file):" + path);
                }
                try {
                    set.add(file.toURI().toURL());
                } catch (Throwable ex) {
                    getLogger().warn("Invalid class path(URL ERROR 2)", ex);
                }
            } else {
                file.listFiles((File f) -> {
                    String fname = f.getName().toLowerCase();
                    if (f.isFile() && (fname.endsWith("jar") || fname.endsWith(".class"))) {
                        try {
                            set.add(f.toURI().toURL());
                        } catch (Throwable ex) {
                            getLogger().warn("Invalid class path(URL ERROR 3)", ex);
                        }
                    }
                    return false;
                });
            }
        }

        if (!set.isEmpty()) {
            set.iterator().forEachRemaining(url -> {
                super.addURL(url);
            });
        }
    }

    /**
     * 实例化一个类。
     *
     * @param <T>
     * @param clazz
     * @param args
     * @return
     * @throws ReflectiveOperationException
     */
    public <T> T newInstance(Class<T> clazz, Object... args) throws ReflectiveOperationException {
        int length = args == null ? 0 : args.length;
        if (length > 0) {
            try {
                Class<?>[] types = new Class<?>[length];
                for (int i = 0; i < length; i++) {
                    types[i] = args[i].getClass();
                }
                Constructor<?> ctor = clazz.getConstructor(types);
                if (ctor != null) {
                    return (T) ctor.newInstance(args);
                }
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (MANO_MACRO.DEBUG) {
                    logger.debug(e.getTargetException());
                }
            } catch (Throwable e) {
                if (MANO_MACRO.DEBUG) {
                    logger.debug(e);
                }
            }
        }

        for (Constructor<?> ctor : clazz.getConstructors()) {
            if (ctor.getParameterTypes().length == length) {
                try {
                    return (T) ctor.newInstance(args);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw new ReflectiveOperationException(e.getTargetException());
                } catch (Throwable ex) {
                    throw new ReflectiveOperationException(ex);
                }
            }
        }
        throw new ReflectiveOperationException();
    }

    /**
     * 实例化一个类。
     *
     * @param clazz
     * @param args
     * @return
     * @throws ReflectiveOperationException
     */
    public Object newInstance(String clazz, Object... args) throws ReflectiveOperationException {
        return this.newInstance(this.loadClass(clazz, true), args);
    }

    private Properties properties;// = new Properties();

    /**
     * 获取配置属性。
     */
    public Properties getProperties() {
        return getProperties(false);
    }
    /**
     * 获取配置属性。
     */
    public Properties getProperties(boolean resolve) {
        return properties != null ? properties : (resolve && this.parent != null ? this.parent.getProperties() : null);
    }
    
    public void setProperties(Properties props) {
        properties = props;
    }
    @Override
    public String getProperty(String key) {
        return getProperty(key,false);
    }
    
    @Override
    public String getProperty(String key, boolean resolve) {
        if(properties!=null){
            if(properties.containsKey(key)){
                return properties.getProperty(key);
            }else if(resolve && parent!=null){
                return parent.getProperty(key);
            }
        }else if(resolve && parent!=null){
            return parent.getProperty(key);
        }
        return null;
    }

    @Override
    public synchronized void setProperty(String key, String value) {
        if(properties==null){
            properties = new Properties();
        }
        properties.setProperty(key, value);
    }
    @Override
    public boolean containsProperty(String key) {
        return containsProperty(key,false);
    }
    @Override
    public boolean containsProperty(String key, boolean resolve) {
        
        if(properties!=null){
            if(properties.containsKey(key)){
                return true;
            }else if(resolve && parent!=null){
                return parent.containsProperty(key);
            }
        }else if(resolve && parent!=null){
            return parent.containsProperty(key);
        }
        
        return false;
    }

    private HashMap<String, Class<?>> mappings = new HashMap<>();

    /**
     * @deprecated 
     */
    public void registerExport(String name, String clazz) throws ClassNotFoundException {
        mappings.put(name, this.loadClass(clazz, true));
    }

    /**
     * @deprecated 
     */
    public Object getExport(String name, Object... args) throws ReflectiveOperationException {
        if (mappings.containsKey(name)) {
            return newInstance(mappings.get(name), args);
        }
        return null;
    }

    /**
     * @deprecated 
     */
    public <T> T getExport(Class<T> clazz, String name, Object... args) throws ReflectiveOperationException {
        return (T) getExport(name, args);
    }

    

}
