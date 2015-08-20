/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.io.File;
import java.util.Map;
import mano.InvalidOperationException;
import mano.net.http.HttpContext;

/**
 * 定义一个渲染WEB视图的引擎抽象类。
 * @author jun <jun@diosay.com>
 */
public abstract class ViewEngine {

    private String tmpdir;
    private String viewdir;

    /**
     * 设置视图编译时的临时目录。
     * @param path
     * @throws InvalidOperationException 
     */
    public void setTempdir(String path) throws InvalidOperationException {

        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            throw new InvalidOperationException("指定路径不是一个有效目录：" + path);
        } else if (!file.exists() && !file.mkdirs()) {
            throw new InvalidOperationException("指定路径不存在，尝试创建但失败：" + path);
        }
        try {
            if (!file.canWrite()) {
                file.setWritable(true);
            }
            if (!file.canRead()) {
                file.setReadable(true);
            }
        } catch (Exception ex) {
            throw new InvalidOperationException("指定路径不可读或写，尝试设置但失败：" + path, ex);
        }
        tmpdir = file.getAbsolutePath();
    }

    /**
     * 获取视图编译时的临时目录。
     * @return
     * @throws InvalidOperationException 
     */
    public String getTempdir() throws InvalidOperationException {
        if (tmpdir == null) {
            throw new InvalidOperationException("未设置临时目录。");
        }
        return tmpdir;
    }

    /**
     * 设置视图模板的根目录。
     * @param path 
     */
    public void setViewdir(String path) {
        File file = new File(path);
        
        try {
            if (!file.exists() || !file.isDirectory()) {
            throw new InvalidOperationException("指定路径不是一个有效目录：" + path);
        }
            viewdir = file.getAbsolutePath();
            if (!file.canRead()) {
                file.setReadable(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //throw new InvalidOperationException("指定路径不可读，尝试设置但失败：" + path, ex);
        }
        
        viewdir=(viewdir==null || "".equals(viewdir))?path:viewdir;
        System.out.println("DDDD:"+viewdir);
    }

    /**
     * 获取视图模板的根目录。
     * @return 
     */
    public String getViewdir() {
        if (viewdir == null) {
            throw new InvalidOperationException("未设置视图目录。");
        }
        return viewdir;
    }
    
    /**
     * 创建新视图上下文的工厂方法。
     * @param context
     * @return 视图上下文
     */
    public abstract ViewContext createContext(HttpContext context);
    
    /**
     * 解释并执行OTPL视图。
     * @param context 当前上下文.
     */
    public abstract void render(ViewContext context);
    
    public abstract void init(Map<String,Object> evn);

}
