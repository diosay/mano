/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.util.Utility;
import java.io.File;
import java.io.IOException;

/**
 * 表示一个通过 HTTP 协议上传的文件。
 * @author jun <jun@diosay.com>
 */
public class HttpPostFile {

    private String name;
    private String original;
    private String contentType;
    private long size;
    private File tempfile;
    protected HttpPostFile(){}
    HttpPostFile(File f, String field, String original, String type, long len) {
        this.tempfile = f;
        this.name = field;
        this.original = original;
        this.contentType = type;
        this.size = len;
    }

    /**
     * 获取表单字段名称。
     * @return 
     */
    public String getName() {
        return this.name;
    }

    /**
     * 已过时，请调用 getOriginal 方法。
     * @return
     * @deprecated
     */
    @Deprecated
    public String getFilename() {
        return this.original;
    }
    
    /**
     * 获取原文件名。
     * @return 
     */
    public String getOriginal(){
        return this.original;
    }

    /**
     * 获取文件的 MIME 类型。
     * @return 
     */
    public String getType() {
        return this.contentType;
    }

    /**
     * 获取文件的字节大小。
     * @return 
     */
    public long getLength() {
        return this.size;
    }

    /**
     * 获取上传的存话在服务器的临时文件。
     * @return 
     */
    public File getTempfile() {
        return this.tempfile;
    }

    /**
     * 将临时文件另存指定文件。
     * @param filename
     * @throws IOException 
     */
    public void savaAs(String filename) throws IOException {
        Utility.copyFile(tempfile, new File(filename));
    }

    /**
     * 获取该文件的扩展名。
     * @return 
     */
    public String getExtension() {
        if (this.original != null && this.original.lastIndexOf(".") > 0) {
            return this.original.substring(this.original.lastIndexOf("."));
        }
        return "";
    }

    /**
     * 重新终结器，在GC时将删除该临时文件。
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        if (this.tempfile != null) {
            try {
                this.tempfile.delete();
            } catch (Throwable e) {
            }
        }
        this.tempfile = null;
        super.finalize();
    }
}
