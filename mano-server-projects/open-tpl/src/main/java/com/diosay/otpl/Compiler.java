/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;

import com.diosay.otpl.runtime.OpCode;
import com.diosay.otpl.runtime.OpcodeType;
import com.diosay.otpl.runtime.opcodes.*;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 编译器
 *
 * @author jun <jun@diosay.com>
 */
public abstract class Compiler {

    public static final ArrayList<String> keywords = new ArrayList<>();

    static {

        keywords.add("false");
        keywords.add("true");
        keywords.add("null");
        keywords.add("nil");
        keywords.add("this");
        keywords.add("str");//$otpl_func_str
    }

    /**
     * 是否是关键词
     *
     * @param id
     * @return
     */
    public boolean isKeyword(String id) {
        for (String s : keywords) {
            if (s.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成一个随机名称。
     *
     * @return
     */
    public String randomName() {
        return Integer.toHexString(UUID.randomUUID().hashCode());
    }

    /**
     * 编译
     *
     * @param node
     */
    public abstract void compile(Node node, ArrayList<OpCode> list);

    /**
     * 获取一个块结束节点的索引
     *
     * @param list
     * @param index
     * @param start
     * @param end
     * @return
     */
    protected int lastOf(ArrayList<Token> list, int index, int start, int end) {

        int ms = 1;
        int result = -1;
        for (int i = index; i < list.size(); i++) {
            if (list.get(i).type == start) {
                ms++;
            } else if (list.get(i).type == end) {
                ms--;
                if (ms == 0) {
                    result = i;
                }
            }
        }
        return result;
    }

    /**
     * 语法分析
     *
     * @param list
     */
    protected void grammar(ArrayList<Token> list) {
        Token token;
        boolean done;

        //第1级：解决小括号(函数或块域)与中括号(索引)。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.OP) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "(";
                    int index = lastOf(list, i + 1, Token.OP, Token.CP);
                    if (index < 0) {
                        throw new mano.InvalidOperationException();
                    }
                    for (int j = i + 1; j < index; j++) {
                        token.add(list.get(j));
                    }
                    list.add(i, token);
                    i++;
                    list.remove(i);//(
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    list.remove(i);//)
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.OB) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "[";
                    int index = lastOf(list, i + 1, Token.OB, Token.CB);
                    if (index < 0) {
                        throw new mano.InvalidOperationException();
                    }
                    for (int j = i + 1; j < index; j++) {
                        token.add(list.get(j));
                    }
                    list.add(i, token);
                    i++;
                    list.remove(i);//[
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    list.remove(i);//]
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第2级：解决对象成员访问。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.DOT) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "dot";
                    token.add(list.get(i - 1));
                    //token.children.add(list.get(i));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//.
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    if (token.get(0).type == Token.LONG && token.get(1).type == Token.LONG) {//小数
                        token.type = Token.REAL;
                        token.code = token.get(0).code.trim() + "." + token.get(1).code.trim();
                        token.clear();
                    } else if (token.get(0).type == Token.LONG || token.get(1).type == Token.LONG) {
                        throw new mano.InvalidOperationException("无效的属性名称。");
                    }
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第3级：合并函数与索引。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.BLK && list.get(i).code.equals("(")) {
                    if (i - 1 >= 0 && (list.get(i - 1).type == Token.ID || list.get(i - 1).code.equals("."))) {

                    } else {
                        continue;
                    }

                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "call";
                    token.add(list.get(i - 1));
                    token.add(list.get(i));
                    token.get(1).code = "params"; //解决函数无限递归
                    list.add(i - 1, token);

                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.BLK && list.get(i).code.equals("[")) {
                    if (i - 1 >= 0 && (list.get(i - 1).type == Token.ID || list.get(i - 1).code.equals("."))) {

                    } else {
                        continue;
                    }

                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "indexer";
                    token.add(list.get(i - 1));
                    token.add(list.get(i));
                    token.get(1).code = "params"; //解决函数无限递归
                    list.add(i - 1, token);

                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第4级：解决非、负运算。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.EM) {//非
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "opp";
                    token.add(list.get(i + 1));
                    list.add(i, token);
                    list.remove(i);//!
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.SUB) {
                    if (i - 1 >= 0 && !(list.get(i - 1).type == Token.ID || list.get(i - 1).type == Token.BLK)) {

                    } else {
                        continue;
                    }
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "neg";//负
                    token.add(list.get(i + 1));

                    list.remove(i);//-
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    list.add(i, token);
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第5级：解决乘、除、模运算。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.MUL) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "mul";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//*
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.DIV) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "div";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);///
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.MOD) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "mod";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//%
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第6级：解决加、减运算。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.ADD) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "add";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//+
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.SUB) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "sub";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//-
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第7级：解决大于、大于等于、小于、小于等于运算。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.GT) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "gt";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//+
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.GTE) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "ge";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//-
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.LT) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "lt";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//-
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.LTE) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "le";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//-
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第8级：解决等于、不等于运算。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.EQ) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "eq";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//+
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.NEQ) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "ne";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//-
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第9级：解决与、或运算。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.AND) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "and";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//+
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.OR) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "or";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//-
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第10级：解决空、三目运算。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.NC) { //??
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "nc";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//??
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                } else if (list.get(i).type == Token.QM) { //?
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "qm";
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    token.add(list.get(i + 3));
                    list.add(i - 1, token);
                    list.remove(i);//?
                    list.remove(i);//:
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第11级：解决赋值运算。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.ASSIGN) {
                    token = new Token();
                    token.line = list.get(i).line;
                    token.type = Token.BLK;
                    token.code = "assign";//=
                    token.add(list.get(i - 1));
                    token.add(list.get(i + 1));
                    list.add(i - 1, token);
                    list.remove(i);//+
                    for (int j = 0; j < token.size(); j++) {
                        list.remove(i);
                    }
                    grammar(token);
                    done = true;
                    break;
                }
            }
        } while (done);

        //第12级：解决参数(列表)运算。
        do {
            done = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).type == Token.COMMA) {
                    if (i - 1 > -1 && list.get(i - 1).type == Token.BLK && "comma".equals(list.get(i - 1).code)) {
                        token = list.get(i - 1);
                        token.add(list.get(i + 1));
                        list.remove(i);
                        list.remove(i);
                        grammar(token);
                    } else {
                        token = new Token();
                        token.line = list.get(i).line;
                        token.type = Token.BLK;
                        token.code = "comma";//,
                        token.add(list.get(i - 1));
                        token.add(list.get(i + 1));
                        list.add(i - 1, token);
                        list.remove(i);
                        list.remove(i);
                        list.remove(i);

                        grammar(token);
                    }
                    done = true;
                    break;
                }
            }
        } while (done);
    }

    /**
     * 词法分析
     *
     * @param source
     * @param start
     * @param end
     * @return
     */
    protected ArrayList<Token> scan(CharSequence source, int start, int end, int line) {
        ArrayList<Token> list = new ArrayList<>();
        Token token;
        char c;
        int index;
        String s = null;
        for (; start < end && start < source.length(); start++) {
            c = source.charAt(start);
            if (StringUtil.isWhitespace(c)) {
                continue;
            } else if (StringUtil.isAlphanumeric(c) || '_' == c) {
                if (StringUtil.isDigital(c) && (index = StringUtil.parseNumber(source, start, end)) > -1) {
                    token = new Token();
                    token.type = Token.LONG;
                    token.code = source.subSequence(start, index).toString();
                    token.line = line;
                    list.add(token);
                    start = index - 1;
                } else if ((index = StringUtil.findIdentifier(source, start, end)) > -1) {
                    token = new Token();
                    token.type = Token.ID;
                    token.code = source.subSequence(start, index).toString();
                    token.line = line;
                    list.add(token);
                    start = index - 1;
                } else {
                    throw new UnsupportedOperationException("非法字符 " + c + " ,col:" + start);
                }
            } else if ('(' == c) {
                token = new Token();
                token.type = Token.OP;
                token.line = line;
                list.add(token);
            } else if (')' == c) {
                token = new Token();
                token.type = Token.CP;
                token.line = line;
                list.add(token);
            } else if ('[' == c) {
                token = new Token();
                token.type = Token.OB;
                token.line = line;
                list.add(token);
            } else if (']' == c) {
                token = new Token();
                token.type = Token.CB;
                token.line = line;
                list.add(token);
            } else if ('+' == c) {
                token = new Token();
                token.type = Token.ADD;
                token.line = line;
                list.add(token);
            } else if ('-' == c) {
                token = new Token();
                token.type = Token.SUB;
                token.line = line;
                list.add(token);
            } else if ('*' == c) {
                token = new Token();
                token.type = Token.MUL;
                token.line = line;
                list.add(token);
            } else if ('/' == c) {
                token = new Token();
                token.type = Token.DIV;
                token.line = line;
                list.add(token);
            } else if ('%' == c) {
                token = new Token();
                token.type = Token.MOD;
                token.line = line;
                list.add(token);
            } else if ('.' == c) {
                token = new Token();
                token.type = Token.DOT;
                token.line = line;
                list.add(token);
            } else if (',' == c) {
                token = new Token();
                token.type = Token.COMMA;
                token.line = line;
                list.add(token);
            } else if (':' == c) {
                token = new Token();
                token.type = Token.COLON;
                token.line = line;
                list.add(token);
            } else if ('|' == c) {
                token = new Token();
                token.type = Token.VL;
                token.line = line;
                list.add(token);
            } else if ('=' == c) {
                token = new Token();
                if (source.charAt(start + 1) == '=') { //++
                    token.type = Token.EQ;
                    start++;
                } else {
                    token.type = Token.ASSIGN;
                }
                token.line = line;
                list.add(token);
            } else if ('?' == c) {
                token = new Token();
                if (source.charAt(start + 1) == '?') { //++
                    token.type = Token.NC;
                    start++;
                } else {
                    token.type = Token.QM;
                }
                token.line = line;
                list.add(token);
            } else if ('!' == c) {
                token = new Token();
                if (source.charAt(start + 1) == '=') { //++
                    token.type = Token.NEQ;
                    start++;
                } else {
                    token.type = Token.EM;
                }
                token.line = line;
                list.add(token);
            } else if ('>' == c) {
                token = new Token();
                if (source.charAt(start + 1) == '=') { //++
                    token.type = Token.GTE;
                    start++;
                } else {
                    token.type = Token.GT;
                }
                token.line = line;
                list.add(token);
            } else if ('<' == c) {
                token = new Token();
                if (source.charAt(start + 1) == '=') { //++
                    token.type = Token.LTE;
                    start++;
                } else {
                    token.type = Token.LT;
                }
                token.line = line;
                list.add(token);
            } else if ('=' == c) {
                token = new Token();
                if (source.charAt(start + 1) == '=') { //++
                    token.type = Token.EQ;
                    start++;
                } else {
                    token.type = Token.ASSIGN;
                }
                token.line = line;
                list.add(token);
            } else if ('"' == c) {
                if ((index = StringUtil.findString(source, start + 1, end)) < 0) {
                    throw new UnsupportedOperationException("非法字符");
                }
                token = new Token();
                token.type = Token.STR;
                token.code = source.subSequence(start + 1, index).toString();
                token.line = line;
                list.add(token);
                start = index;
            } else if ('\'' == c) {
                if ((index = StringUtil.findStringEx(source, start + 1, end)) < 0) {
                    throw new UnsupportedOperationException("非法字符");
                }
                token = new Token();
                token.type = Token.STR;
                token.code = source.subSequence(start + 1, index).toString();
                token.line = line;
                list.add(token);
                start = index;
            } else {
                throw new UnsupportedOperationException("非法字符");
            }
        }
        return list;
    }

    /**
     * 获取参数个数
     *
     * @param token
     * @return
     */
    static int getCount(Token token) {
        if (token.type != Token.BLK || !token.code.equals("comma")) {
            return 1;
        }
        int count = 0;
        for (Token sub : token) {
            if (token.type == Token.BLK && token.code.equals("comma")) {
                count += getCount(token.get(1));
            } else {
                count++;
            }
        }
        return count;
    }

    /**
     * 访问语法树，并生成中间代码
     *
     * @param token
     */
    protected void visit(Token token, ArrayList<OpCode> list) {
        switch (token.type) {
            case Token.ID: {
                if ("null".equalsIgnoreCase(token.code)) {
                    LoadConst code = new LoadConst();
                    code.setValue(LoadConst.NULL);
                    list.add(code);
                } else if ("false".equalsIgnoreCase(token.code)) {
                    LoadConst code = new LoadConst();
                    code.setValue(LoadConst.FALSE);
                    list.add(code);
                }
                if ("true".equalsIgnoreCase(token.code)) {
                    LoadConst code = new LoadConst();
                    code.setValue(LoadConst.TRUE);
                    list.add(code);
                } else {
                    LoadVariable code = new LoadVariable();
                    code.setName(token.code);
                    list.add(code);
                }
                break;
            }
            case Token.LONG: {
                LoadLong code = new LoadLong();
                code.setValue(Long.parseLong(token.code));
                list.add(code);
                break;
            }
            case Token.REAL: {
                LoadReal code = new LoadReal();
                code.setValue(Double.parseDouble(token.code));
                list.add(code);
                break;
            }
            case Token.STR: {
                LoadString code = new LoadString();
                code.setContent(token.code);
                list.add(code);
                break;
            }
            case Token.BLK:
                final String b = token.code;
                switch (b) {
                    case "dot":
                        visit(token.get(0), list);
                        System.out.println("->");//neg

                        if (token.get(1).type == Token.BLK && "call".equals(token.get(1).code)) {
                            if (token.get(1).get(0).type == Token.ID) {
                                int len = getCount(token.get(1).get(1));
                                list.add(new Peek()); //peek
                                list.add(new LoadMember().setName(token.get(1).get(0).code).setArgLength(len));//ldm
                                visit(token.get(1).get(1), list);
                                list.add(new Call().setArgLength(len));
                            } else {
                                visit(token.get(1), list);
                            }
                        } else if (token.get(1).type == Token.BLK && "indexer".equals(token.get(1).code)) {
                            if (token.get(1).get(0).type == Token.ID) {
                                int len = getCount(token.get(1).get(1));
                                visit(token.get(1).get(1), list);
                                list.add(new Callvri().setName("indexer").setArgLength(len + 1));
                            } else {
                                visit(token.get(1), list);
                            }
                        } else if (token.get(1).type == Token.ID) {
                            list.add(new Peek()); //peek
                            list.add(new LoadMember().setName(token.get(1).code).setArgLength(0));//ldm
                            list.add(new Call().setArgLength(0));
                        } else {
                            visit(token.get(1), list);
                        }
                        break;
                    case "neg":
                        visit(token.get(0), list);
                        list.add(new Operator().setOperator(Operator.NEG));
                        break;
                    case "call":
                        if (token.get(0).type != Token.ID) {
                            throw new UnsupportedOperationException("语法错误");
                        }
                        visit(token.get(1), list);//参数
                        list.add(new Callvri().setName(token.get(0).code).setArgLength(getCount(token.get(1).get(0))));
                        break;
                    case "indexer":
                        if (token.get(0).type != Token.ID) {
                            throw new UnsupportedOperationException("语法错误");
                        }
                        int len = getCount(token.get(1));
                        visit(token.get(1), list);//参数
                        //array 
                        //obj list.add(new LoadMember().setName(token.get(0).code).setArgLength(len));
                        list.add(new LoadVariable().setName(token.get(0).code));
                        list.add(new Callvri().setName("indexer").setArgLength(len + 1));

                        break;
                    case "add": {
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.ADD);
                        list.add(code);
                        break;
                    }
                    case "sub": {
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.SUB);
                        list.add(code);
                        break;
                    }
                    case "mul": {
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.MUL);
                        list.add(code);
                        break;
                    }
                    case "div": {
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.DIV);
                        list.add(code);
                        break;
                    }
                    case "mod": {
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.MOD);
                        list.add(code);
                        break;
                    }
                    case "and": {//&&
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.AND);
                        list.add(code);
                        break;
                    }
                    case "or": {//||
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.OR);
                        list.add(code);
                        break;
                    }
                    case "eq": {//=
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.EQ);
                        list.add(code);
                        break;
                    }
                    case "ne": {//!=
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.NE);
                        list.add(code);
                        break;
                    }
                    case "gt": {//>
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.GT);
                        list.add(code);
                        break;
                    }
                    case "ge": {//>=
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.GE);
                        list.add(code);
                        break;
                    }
                    case "lt": {//<
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.LT);
                        list.add(code);
                        break;
                    }
                    case "le": {//<=
                        visit(token.get(0), list);
                        visit(token.get(1), list);
                        Operator code = new Operator();
                        code.setOperator(Operator.LE);
                        list.add(code);
                        break;
                    }
                    case "nc": {//??
                        Nop case1 = new Nop();
                        Nop case2 = new Nop();
                        visit(token.get(0), list);
                        list.add(new Peek());
                        list.add(new LoadNull());
                        list.add(new Operator().setOperator(Operator.NE));//? !=null
                        list.add(OpCode.makeBreakFalse(case1));//if false then left
                        list.add(OpCode.makeBreak(case2));
                        list.add(case1);
                        visit(token.get(1), list);
                        list.add(case2);
                        break;
                    }
                    case "qm": {//?:
                        Nop case1 = new Nop();
                        Nop case2 = new Nop();
                        visit(token.get(0), list);
                        list.add(OpCode.makeBreakFalse(case1));//if false then left
                        visit(token.get(1), list);
                        list.add(OpCode.makeBreak(case2));
                        list.add(case1);
                        visit(token.get(2), list);
                        list.add(case2);
                        break;
                    }
                    default:
                        for (Token sub : token) {
                            visit(sub, list);
                        }
                }
                break;
            default:
                throw new UnsupportedOperationException("语法错误 " + token.type + " " + token.code);
        }
    }

    /**
     * 编译源码
     *
     * @param source
     * @param list
     */
    protected int compile(CharSequence source, ArrayList<OpCode> list, int line) {
        ArrayList<Token> tokens = this.scan(source, 0, source.length(), line);
        this.grammar(tokens);
        for (Token sub : tokens) {
            this.visit(sub, list);
        }
        return tokens.size();
    }

    protected void adjustFor(ArrayList<OpCode> list, int start, int end) {
        int tmp;
        OpCode code;
        BreakDescriptor brk;
        for (; start < end; start++) {
            code = list.get(start);
            if (code.getType().equals(OpcodeType.nop) && "for".equals(code.tag)) { //loop
                tmp = findMark(list, code, start + 1, end);
                if (tmp < 0) {
                    throw new mano.InvalidOperationException("for 未结束");
                }
                adjustFor(list, start + 1, tmp);//嵌套
                for (int i = start + 1; i <= tmp; i++) {
                    if (!(list.get(i) instanceof BreakDescriptor)) {
                        continue;
                    }
                    brk = (BreakDescriptor) list.get(i);
                    if (brk.target != null) {
                        //nothing
                    } else if (brk.blocked) {
                        brk.target = list.get(tmp);
                    } else {
                        brk.target = code;
                    }
                }
                start = tmp;
            }
        }
    }

    protected void adjustEach(ArrayList<OpCode> list, int start, int end) {
        int tmp;
        OpCode code;
        BreakDescriptor brk;
        for (; start < end; start++) {
            code = list.get(start);
            if (code.getType().equals(OpcodeType.nop) && "each".equals(code.tag)) { //loop
                tmp = findMark(list, code, start + 1, end);
                if (tmp < 0) {
                    throw new mano.InvalidOperationException("each 未结束");
                }
                adjustEach(list, start + 1, tmp);//嵌套
                for (int i = start + 1; i <= tmp; i++) {
                    if (!(list.get(i) instanceof BreakDescriptor)) {
                        continue;
                    }
                    brk = (BreakDescriptor) list.get(i);
                    if (brk.target != null) {
                        //nothing
                    } else if (brk.blocked) {
                        brk.target = list.get(tmp);
                    } else {
                        brk.target = code;
                    }
                }
                start = tmp;
            }
        }
    }

    int findMark(ArrayList<OpCode> list, OpCode in, int start, int end) {
        int tmp;
        OpCode code;
        for (; start < list.size(); start++) {
            code = list.get(start);
            if (in.tag.equals(code.tag) && in.mark.equals(code.mark)) {
                return start;
            }
        }
        return -1;
    }
}
