/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import mano.DateTime;

/**
 * 简单日志格式化。
 * @author jun <jun@diosay.com>
 */
public class SimpleFormatter implements Formatter {

    @Override
    public CharSequence format(Entry entry) {

        //2014-12-12 00:00:00 [level] T14 com.Class.main[L14]:message
        //error
        StringBuilder sb = new StringBuilder();
        sb.append(entry.getTime().toString(DateTime.FORMAT_ISO)).append(' ');
        sb.append('[').append(entry.getLevel().name).append(']').append(' ');
        sb.append('T').append(entry.getSourceThreadId()).append(' ');
        if (entry.getSourceClassName() != null && !"".equals(entry.getSourceClassName().trim())) {
            sb.append(entry.getSourceClassName());
        }
        if (entry.getSourceMethodName() != null && !"".equals(entry.getSourceMethodName().trim())) {
            sb.append('.').append(entry.getSourceMethodName());
        }
        sb.append("[L").append(entry.getSourceLineNumber()).append("]:").append(entry.getMessage());

        if (entry.getExceptions() != null) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                pw.println();
                for(Throwable ex:entry.getExceptions()){
                    ex.printStackTrace(pw);
                }
            }
            sb.append(sw.toString());
            sw = null;//help GC
        }
        sb.append(System.lineSeparator());
        return sb;
    }
    
    /**
     * 获取默认实例。
     */
    public static final SimpleFormatter DEFAULT;
    static{
        DEFAULT=new SimpleFormatter();
    }
    
}
