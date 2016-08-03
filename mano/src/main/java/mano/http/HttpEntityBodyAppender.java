/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import java.nio.charset.Charset;

/**
 * 表示一个封闭了用于解码HTTP实体的参数和实体存放的辅助类。
 * @author jun <jun@diosay.com>
 */
public interface HttpEntityBodyAppender {
    
    /**
     * 获取实体字符编码。
     * @return 
     */
    Charset getEncoding();
    
    /**
     * 获取实体内容长度。
     * @return 
     */
    long getContentLength();
    /**
     * 获取请求为 multipart 时的分界符。
     */
    String getBoundary();
    /**
     * 附加一个实体文件到当前请求上下文。
     * @param file 
     */
    void appendPostFile(HttpPostFile file);
    /**
     * 附加一个表单项到当前请求上下文。
     * @param name
     * @param value 
     */
    void appendFormItem(String name, String value);
    /**
     * 通知解码完成。
     */
    void notifyDone();
}
