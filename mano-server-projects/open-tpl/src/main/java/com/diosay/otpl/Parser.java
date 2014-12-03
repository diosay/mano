/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;

import com.diosay.otpl.lexers.BodyLexer;
import com.diosay.otpl.lexers.BreakLexer;
import com.diosay.otpl.lexers.ContinueLexer;
import com.diosay.otpl.lexers.EachLexer;
import com.diosay.otpl.lexers.ElifLexer;
import com.diosay.otpl.lexers.ElseLexer;
import com.diosay.otpl.lexers.ForLexer;
import com.diosay.otpl.lexers.IfLexer;
import com.diosay.otpl.lexers.IncludeLexer;
import com.diosay.otpl.lexers.LayoutLexer;
import com.diosay.otpl.lexers.PlaceLexer;
import com.diosay.otpl.lexers.PrintLexer;
import com.diosay.otpl.lexers.SectionLexer;
import com.diosay.otpl.lexers.TextLexer;
import com.diosay.otpl.lexers.VariableLexer;
import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.opcodes.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 实现OTPL解析器与DOM编译器
 *
 * @author jun <jun@diosay.com>
 */
public class Parser extends Compiler {

    int line;
    Document document;
    boolean inLiteral = false;
    boolean inComment = false;

    /**
     * 打开文件，并尝试移除UTF BOM。
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static BufferedReader open(String file) throws FileNotFoundException, IOException {
        FileInputStream input = new FileInputStream(file);
        String encoding = "UTF-8";
        byte bom[] = new byte[4];

        int removed;
        try {
            input.read(bom, 0, bom.length);
        } catch (Throwable ex) {
            //ignored
        }

        if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00)
                && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            removed = 4;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)
                && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
            encoding = "UTF-32LE";
            removed = 4;
        } else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB)
                && (bom[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            removed = 3;
        } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            removed = 2;
        } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            removed = 2;
        } else {
            removed = 0;
        }
        input.close();
        input = new FileInputStream(file);
        if (removed > 0) {
            input.read(bom, 0, removed);
        }
        return new BufferedReader(new InputStreamReader(input, encoding));
    }

    /**
     * 解析一个OTPL流,并构建一个 Document。
     *
     * @param input 输入流。
     * @param basedir 用于查找源文件的根目录。
     * @param encoding 用于解析源文件的编码。
     * @param sourceFile 全限定路径源文件，可空。
     * @return
     */
    public static Document parsexxxxx(BufferedReader reader, String basedir, String sourceFile) throws Exception {

        String line;
        StringBuilder sb;
        Parser parser = new Parser();
        parser.line = 0; //reset
        parser.document = new Document(parser, sourceFile);
        parser.regsisterLexer(new IfLexer());
        int index;
        int end;
        int[] arr;
        while ((line = reader.readLine()) != null) {
            parser.line++;
            sb = new StringBuilder(line);
            sb.append('\r').append('\n');
            end = sb.length();
            index = 0;

            do {

                if ((arr = StringUtil.findSegment(sb, "{{", "}}", index, end)) != null) {

                } else if ((arr = StringUtil.findSegment(sb, "<!--{{", "}}-->", index, end)) != null) {

                } else {
                    break;
                }

                if (arr[0] - index - 2 != 0) {
                    parser.onText(sb, index, arr[0] - 2);
                }
                parser.onMarkup(sb, arr[0], arr[1]);
                index = arr[1] + 2;

            } while (true);

            if (index < end) {
                parser.onText(sb, index, end);
            }
        }
        return null;
    }

    /**
     * 解析一个OTPL流,并构建一个 Document。
     *
     * @param input 输入流。
     * @param basedir 用于查找源文件的根目录。
     * @param encoding 用于解析源文件的编码。
     * @param sourceFile 全限定路径源文件，可空。
     * @return
     */
    public Document parse(BufferedReader reader, String sourceFile) throws Exception {

        String line;
        StringBuilder sb;
        this.line = 0; //reset
        this.document = new Document(this, sourceFile);
        this.regsisterLexer(new IfLexer());
        this.regsisterLexer(new ElifLexer());
        this.regsisterLexer(new ElseLexer());
        this.regsisterLexer(new EachLexer());
        this.regsisterLexer(new PrintLexer());
        this.regsisterLexer(new TextLexer());
        this.regsisterLexer(new BodyLexer());
        this.regsisterLexer(new LayoutLexer());
        this.regsisterLexer(new SectionLexer());
        this.regsisterLexer(new PlaceLexer());
        this.regsisterLexer(new ForLexer());
        this.regsisterLexer(new VariableLexer());
        this.regsisterLexer(new BreakLexer());
        this.regsisterLexer(new ContinueLexer());
        this.regsisterLexer(new IncludeLexer());
        int index;
        int end;
        int[] arr;
        while ((line = reader.readLine()) != null) {
            this.line++;
            sb = new StringBuilder(line);
            sb.append('\r').append('\n');
            end = sb.length();
            index = 0;

            do {

                if ((arr = StringUtil.findSegment(sb, "{{", "}}", index, end)) != null) {
                    boolean lft = false;
                    boolean rgt = false;
                    int tmp = 0;
                    int tmp2 = 0;
                    for (int i = arr[0] - 3; i >= index; i--) {
                        if (StringUtil.isWhitespace(sb.charAt(i))) {
                            continue;
                        } else if (i - 3 >= index && sb.charAt(i - 3) == '<' && sb.charAt(i - 2) == '!' && sb.charAt(i - 1) == '-' && sb.charAt(i) == '-') {
                            //<!--
                            lft = true;
                            tmp = i - 3;

                            for (; tmp >= index; tmp--) {
                                if (!StringUtil.isWhitespace(sb.charAt(tmp))) {
                                    break;
                                }
                            }
                            break;
                        } else {
                            break;
                        }
                    }

                    if (lft) {
                        for (int i = arr[1] + 2; i < end; i++) {
                            if (StringUtil.isWhitespace(sb.charAt(i))) {
                                continue;
                            } else if (i + 2 < end && sb.charAt(i) == '-' && sb.charAt(i + 1) == '-' && sb.charAt(i + 2) == '>') {
                                //-->
                                rgt = true;
                                tmp2 = i + 2;

                                for (; tmp2 < end; tmp2++) {
                                    if (!(StringUtil.isWhitespace(sb.charAt(tmp2)) || sb.charAt(tmp2) == '\r' || sb.charAt(tmp2) == '\n')) {
                                        break;
                                    }
                                }
                                break;
                            } else {
                                break;
                            }
                        }
                    }

                    if (lft && rgt) {
                        if (tmp - index > 0) {
                            this.onText(sb, index, tmp);
                        }
                        this.onMarkup(sb, arr[0], arr[1]);
                        index = tmp2 + 1;
                    } else {
                        if (arr[0] - index - 2 != 0) {
                            this.onText(sb, index, arr[0] - 2);
                        }
                        this.onMarkup(sb, arr[0], arr[1]);
                        index = arr[1] + 2;
                    }
                } else {
                    break;
                }

            } while (true);

            if (index < end) {
                this.onText(sb, index, end);
            }
        }
        return this.document;
    }

    /**
     * 当发现文本时。
     */
    protected void onText(CharSequence source, int start, int end) {
        if(inComment){
            return;
        }
        Text text = new Text(this.document, this.line);
        text.setCompiler((Compiler) lexers.get("text"));
        text.setSource(source, start, end);
        this.document.append(text);
        //System.out.println(source.subSequence(index, end).toString());
    }

    protected void reportError(String msg) {
        throw new mano.InvalidOperationException("行 " + this.line + " 有语法错误：" + msg);
    }

    /**
     * 注册一个词条。
     */
    public void regsisterLexer(Lexer lexer) {
        lexers.put(lexer.getToken(), lexer);
    }

    private HashMap<String, Lexer> lexers = new HashMap<>();

    /**
     * 当解析到标记时。
     */
    protected void onMarkup(CharSequence source, int start, int end) {
        start = StringUtil.trimLeftWhitespace(source, start, end);//移除开始的线性空白
        int tmp;
        if ((tmp = StringUtil.findKeyword(source, "/literal", start, end)) != -1
                && !StringUtil.isAlphanumeric(source, tmp + 1, end)) { //原样输出结束
            if (!inComment) {
                if (!inLiteral) {
                    reportError("错误的原样结束标签");
                    inLiteral = true; //修正错误继续解析
                }
                inLiteral = false;
            }

        } else if (inLiteral) {
            this.onText(source, start - 2, end + 2);
        } else if ((tmp = StringUtil.findKeyword(source, "*/", start, end)) != -1) { //块注释结束
            this.inComment = false;
        } else if ((tmp = StringUtil.findKeyword(source, "/*", start, end)) != -1) {//块注释开始
            this.inComment = true;
        } else if (inComment || (tmp = StringUtil.findKeyword(source, "//", start, end)) != -1) { //当前为注释中
            //ignored
        } else if ((tmp = StringUtil.findKeyword(source, "literal", start, end)) != -1
                && !StringUtil.isAlphanumeric(source, tmp + 1, end)) { //原样输出开始
            inLiteral = true;
        } else {
            final int tmpstart = start;
            final int tmpend = end;
            if (!lexers.values().stream().anyMatch(lexer -> {
                if (!lexer.isBlock()) {
                    return false;
                }
                int tmp2;
                if ((tmp2 = StringUtil.findKeyword(source, "/" + lexer.getToken(), tmpstart, tmpend)) != -1
                        && StringUtil.isEnd(source, tmp2 + 1, tmpend)) {
                    document.append(document.createEndBlock(source.subSequence(tmpstart + 1, tmp2).toString(), line));
                    //System.out.println("find end=====" + source.subSequence(tmpstart, tmp2));
                    return true;
                }
                return false;
            })) {
                tmp = StringUtil.findIdentifier(source, start, end);
                if (tmp != -1 && this.tryLex(source.subSequence(start, tmp).toString(), source, tmp, end)) {
                    //nothing
                } else { //打印
                    Lexer lexer = lexers.get("print");
                    if (lexer == null) {
                        reportError("print 词条未定义");
                    }
                    lexer.parse(document, source, start, end, line);
                    //System.out.println(source.subSequence(start, end));
                }
            }
        }
    }

    protected boolean tryLex(String token, CharSequence source, int start, int end) {
        if (lexers.values().stream().anyMatch(lexer -> {

            if (!lexer.getToken().equalsIgnoreCase(token)) {
                return false;
            }
            lexer.parse(document, source, start, end, line);
            return true;
        })) {
            return true;
        }
        //reportError("词条 " + token + " 未定义");
        //System.out.println("TOKEN====" + token);
        return false;
    }

    /**
     * 编译dom
     *
     * @param node
     * @param list
     */
    @Override
    public void compile(Node node, ArrayList<OpCode> list) {

        Document dom = (Document) node;

        //TODO
        for (Node sub : dom.blocks) {
            sub.getCompiler().compile(sub, list);
        }

        list.add(new EndHeader());

        if (dom.layout != null) {
            dom.layout.getCompiler().compile(dom.layout, list);
        }

        for (Node sub : dom.children) {
            sub.getCompiler().compile(sub, list);
        }
    }
}
