/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import mano.logging.Log;
import mano.logging.Logger;

/**
 * 运行时类加载器。
 *
 * @author jun
 */
public class RuntimeClassLoader extends URLClassLoader {

    private String name;

    public void setName(String s) {
        this.name = s;
    }

    public RuntimeClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public RuntimeClassLoader(ClassLoader parent) {
        this(new URL[0], parent);
    }

    public RuntimeClassLoader() {
        this(new URL[0], RuntimeClassLoader.class.getClassLoader());
    }

    public void addJars(Logger log, boolean subfiles, File... paths) {
        if (log == null || paths == null) {
            throw new NullPointerException();
        }
        for (File file : paths) {
            if (file.exists() && file.isDirectory()) {
                File[] list = file.listFiles(f -> {
                    if (f.isFile() && f.getName().toLowerCase().endsWith(".jar")) {
                        try {
                            super.addURL(f.toURI().toURL());
                        } catch (MalformedURLException ex) {
                            log.trace("Invalid class path(Failed to URL ERROR)", ex);
                        }
                        return false;
                    }
                    return f.isDirectory();
                });

                if (subfiles && list.length > 0) {
                    addJars(log, subfiles, list);
                }

            } else if (file.exists() && file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                try {
                    super.addURL(file.toURI().toURL());
                } catch (MalformedURLException ex) {
                    log.trace("Invalid class path(Failed to URL ERROR)", ex);
                }
            } else {
                log.trace(new FileNotFoundException("Invalid file path:" + file.toString()));
            }
        }
    }

    public void addJars(boolean subfiles, File... paths) {
        addJars(Log.TRACE, subfiles, paths);
    }

    public void addJars(boolean subfiles, String... paths) {
        for (String s : paths) {
            addJars(Log.TRACE, subfiles, new File(s));
        }
    }

    public void addJars(File... paths) {
        addJars(Log.TRACE, false, paths);
    }

    public void addJars(String... paths) {
        for (String s : paths) {
            addJars(Log.TRACE, false, new File(s));
        }
    }

    public void addClassesPath(Logger log, File... paths) {
        if (log == null || paths == null) {
            throw new NullPointerException();
        }
        for (File file : paths) {
            if (file.exists() && file.isDirectory()) {
                try {
                    super.addURL(file.toURI().toURL());
                } catch (MalformedURLException ex) {
                    log.trace("Invalid class path(Failed to URL ERROR)", ex);
                }
            } else {
                log.trace(new FileNotFoundException("Invalid class path:" + file.toString()));
            }
        }
    }

    public void addClassesPath(File... paths) {
        addClassesPath(Log.TRACE, paths);
    }

    public void addClassesPath(String... paths) {
        for (String s : paths) {
            addClassesPath(Log.TRACE, new File(s));
        }
    }

    public void AddUrl(URL url) {
        super.addURL(url);
    }

    public void AddUrl(String url) throws URISyntaxException, MalformedURLException {
        AddUrl(new URI(url).toURL());
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
                Log.TRACE.trace(e.getTargetException());
            } catch (Throwable e) {
                Log.TRACE.trace(e);
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

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException ex) {
            throw new ClassNotFoundException(this.toString() + "(" + ex.getMessage() + ")", ex);
        }
    }

    @Override
    public String toString() {
        return this.name == null || "".equals(this.name) ? super.toString() : this.name;
    }

}
