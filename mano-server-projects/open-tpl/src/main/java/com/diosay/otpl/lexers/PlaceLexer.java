/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.lexers;

import com.diosay.otpl.Document;
import com.diosay.otpl.Lexer;
import com.diosay.otpl.Node;
import com.diosay.otpl.Span;
import com.diosay.otpl.StringUtil;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.opcodes.CallBlock;
import java.util.ArrayList;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class PlaceLexer extends com.diosay.otpl.Compiler implements Lexer {

    @Override
    public void compile(Node node, ArrayList<OpCode> list) {
        CharSequence source = node.getSource();

        if (source == null) {
            throw new UnsupportedOperationException("语法错误：未设置名称");
        }
        int index = 0;
        index = StringUtil.trimLeftWhitespace(source, index, source.length());

        if (source.charAt(index) != ':') {
            throw new UnsupportedOperationException("语法错误：未知字符");
        }
        index++;
        int index2 = StringUtil.findIdentifier(source, index, source.length());

        if (index2 < 0) {
            throw new UnsupportedOperationException("语法错误：未设置名称");
        }
        String name = source.subSequence(index, index2).toString();
        CallBlock cb=new CallBlock();
        cb.setName(name);
        index = StringUtil.trimLeftWhitespace(source, index2, source.length());
        if (index < source.length()) {

            index2 = StringUtil.findIdentifier(source, index, source.length());
            if (index2 < 0) {
                throw new UnsupportedOperationException("语法错误：未知字符2");
            }
            String id = source.subSequence(index, index2).toString();
            if("true".equalsIgnoreCase(id) || "t".equalsIgnoreCase(id)){
                cb.required(true);
            }else if("false".equalsIgnoreCase(id) || "f".equalsIgnoreCase(id)){
                cb.required(true);
            }
            else{
                throw new UnsupportedOperationException("语法错误：不能识别的标识符:"+id);
            }
        }
        index = StringUtil.trimLeftWhitespace(source, index2, source.length());
        if (index != source.length()) {
            throw new UnsupportedOperationException("语法错误：未知字符2");
        }
        list.add(cb);
    }

    @Override
    public String getToken() {
        return "place";
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public void parse(Document dom, CharSequence source, int start, int end, int line) {
        Span node = new Span(this.getToken());
        node.setCompiler(this);
        node.setLineNumber(line);
        node.setSource(source, start, end);
        dom.append(node);
    }

}
