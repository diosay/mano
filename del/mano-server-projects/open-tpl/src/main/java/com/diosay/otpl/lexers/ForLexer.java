/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl.lexers;

import com.diosay.otpl.Block;
import com.diosay.otpl.Document;
import com.diosay.otpl.Lexer;
import com.diosay.otpl.Node;
import com.diosay.otpl.StringUtil;
import com.diosay.otpl.Token;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.opcodes.*;
import java.util.ArrayList;

/**
 * for循环
 *
 * @author jun <jun@diosay.com>
 */
public class ForLexer extends com.diosay.otpl.Compiler implements Lexer {

    @Override
    public void compile(Node node, ArrayList<OpCode> rlist) {
        if(node.marked){
            return;
        }
        
        // <editor-fold defaultstate="collapsed" desc="IL Code">
        /*
         for(int i=0;i<10;i++){
         break
         }
        
         ldu 10
         ldu 0
         ldu 1
        
         stv max
         stv i 
         stv step
        
         //stackmax 4
         ldv i
         ldv max
         ldu 0
         ldv step
         lt
         brf DOWN
         lt
         brf ELSE
         br BEGIN
         DOWN
         gt
         brf ELSE
         BEGIN
         
         ldv i
         ldv max
         ldu 0
         ldv step
         lt
         brf DOWN
         lt
         brf END
         br RUN
         DOWN
         gt
         brf RETURN
         RUN
         ...
        
         ldv i
         ldv step
         add
         br BEGIN
         ELSE
         ...
         END
         
         */
        // </editor-fold>
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

        if (this.isKeyword(name)) {
            throw new UnsupportedOperationException("语法错误：变量名不能是关键词：" + name);
        }
        ArrayList<Token> tokens = this.scan(source, index2, source.length(), node.getLineNumber());
        this.grammar(tokens);
        if(tokens.size()!=1){
            throw new UnsupportedOperationException("语法错误：for语句参数错误。");
        }
        if(tokens.get(0).size()==0){
            Token token=new Token();
            token.code=",";
            token.type=Token.COMMA;
            tokens.add(token);
            
            token=new Token();
            token.code="0";
            token.type=Token.LONG;
            tokens.add(token);
            
            token=new Token();
            token.code=",";
            token.type=Token.COMMA;
            tokens.add(token);
            
            token=new Token();
            token.code="1";
            token.type=Token.LONG;
            tokens.add(token);
            this.grammar(tokens);
        }else if(tokens.get(0).size()==2){
            Token token=new Token();
            token.code=",";
            token.type=Token.COMMA;
            tokens.add(token);
            
            token=new Token();
            token.code="1";
            token.type=Token.LONG;
            tokens.add(token);
            this.grammar(tokens);
        }else if(tokens.get(0).size()==3){
            
        }else{
            throw new UnsupportedOperationException("语法错误：for语句参数错误。");
        }
        for (Token sub : tokens) {
            this.visit(sub, rlist);
        }
        
        rlist.add(new SourceLineNumber().setValue(node.getLineNumber()));
        
        ArrayList<OpCode> list=new ArrayList<>();
        
        //设置变量
        SetVariable max = new SetVariable().setName(randomName()); //stvar max
        SetVariable var = new SetVariable().setName(name); //stvar i
        SetVariable step = new SetVariable().setName(randomName()); //stvar step
        list.add(step);
        list.add(var);
        list.add(max);
        
        
        //定义标签
        OpCode begin = OpCode.label();
        begin.tag="for";
        begin.mark=randomName();
        OpCode end = OpCode.label();
        end.tag=begin.tag;
        end.mark=begin.mark;
        OpCode lbElse = OpCode.label();
        OpCode lbDown = OpCode.label();
        OpCode lbDown2 = OpCode.label();
        //获取变量
        list.add(new LoadVariable().setName(var.getName()));//ldv i
        list.add(new LoadVariable().setName(max.getName()));//ldv max
        list.add(new LoadVariable().setName(step.getName()));//ldv step
        list.add(new LoadLong().setValue(0));//ldr 0
        
        list.add(new Operator().setOperator(Operator.LE));//lt
        list.add(OpCode.makeBreakTrue(lbDown));//brf DOWN
        list.add(new Operator().setOperator(Operator.LE));//lt
        list.add(OpCode.makeBreakFalse(lbElse));//brf ELSE
        list.add(OpCode.makeBreak(begin));//br BEGIN
        list.add(lbDown);
        list.add(new Operator().setOperator(Operator.GT));//gt
        list.add(OpCode.makeBreakFalse(lbElse));//brf ELSE
        list.add(begin);
        //body
        for(Node sub:((Block)node).children){
            sub.getCompiler().compile(sub, list);
        }
        //加
        list.add(new LoadVariable().setName(var.getName()));//ldv i
        list.add(new LoadVariable().setName(step.getName()));//ldv step
        list.add(new Operator().setOperator(Operator.ADD));//add
        list.add(new SetVariable().setName(var.getName()));//stv i
        //获取变量
        list.add(new LoadVariable().setName(var.getName()));//ldv i
        list.add(new LoadVariable().setName(max.getName()));//ldv max
        list.add(new LoadVariable().setName(step.getName()));//ldv step
        list.add(new LoadLong().setValue(0));//ldr 0

        list.add(new Operator().setOperator(Operator.LE));//lt
        list.add(OpCode.makeBreakTrue(lbDown));//brf DOWN
        list.add(new Operator().setOperator(Operator.LE));//lt
        list.add(OpCode.makeBreakFalse(end));//brf END
        list.add(OpCode.makeBreak(begin));//br BEGIN
        list.add(lbDown2);
        list.add(new Operator().setOperator(Operator.GT));//gt
        list.add(OpCode.makeBreakFalse(end));//brf ELSE
        list.add(OpCode.makeBreak(begin));//br BEGIN
        list.add(lbElse);
        
        //else
        Node next=node.getNext();//is null
        if(next!=null && next.getName().equals("else")){
            next.marked=true;
            next.getCompiler().compile(next, list);
        }
        
        list.add(end);

        this.adjustFor(list, 0, list.size());
        
        rlist.addAll(list);
        
    }

    @Override
    public String getToken() {
        return "for";
    }

    @Override
    public boolean isBlock() {
        return true;
    }

    @Override
    public void parse(Document dom, CharSequence source, int start, int end, int line) {
        Block node = dom.createBlock(this.getToken());
        node.setCloseName("else");
        node.setCompiler(this);
        node.setLineNumber(line);
        node.setSource(source, start, end);
        dom.append(node);
    }

}
