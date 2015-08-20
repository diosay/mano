/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package com.diosay.otpl;

/**
 * 字符串工具类。
 *
 * @author jun <jun@diosay.com>
 */
public class StringUtil {

    /**
     * 判断是否是数字。
     *
     * @param c
     * @return
     */
    public static boolean isDigital(char c) {
        return (c >= 48 && c <= 57);
    }

    /**
     * 判断是否是字母
     *
     * @param c
     * @return
     */
    public static boolean isLetter(char c) {
        return (c >= 65 && c <= 90 || c >= 97 && c <= 122);
    }

    /**
     * 判断是否是字母或数字
     *
     * @param c
     * @return
     */
    public static boolean isAlphanumeric(char c) {
        return isDigital(c) || isLetter(c);
    }

    /**
     * 判断索引处是否是字母或数字。
     *
     * @param source
     * @param start
     * @param end
     * @return
     */
    public static boolean isAlphanumeric(CharSequence source, int start, int end) {
        if (start >= end || start >= source.length()) {
            return false;
        }
        return isAlphanumeric(source.charAt(start));
    }

    /**
     * 是否是空白字符
     *
     * @param c
     * @return
     */
    public static boolean isWhitespace(char c) {
        return (c == ' ' || c == '\t');
    }

    /**
     * 是否是空白字符
     *
     * @param c
     * @return
     */
    public static int trimLeftWhitespace(CharSequence source, int start, int end) {
        if (source == null || start < 0 || start >= end || start >= source.length()) {//标准
            return start;
        }
        for (; start < end && start < source.length(); start++) {
            if (!isWhitespace(source.charAt(start))) {
                return start;
            }
        }
        return start;
    }

    public static int findIdentifier(CharSequence source, int start, int end) {
        if (source == null || start < 0 || start > source.length() || !isLetter(source.charAt(start))) {
            return -1;
        }
        int ori = start;
        for (; start < end && start < source.length(); start++) {
            if (!isAlphanumeric(source.charAt(start))) {
                break;
            }
        }
        return ori == start ? -1 : start;
    }

    public static int parseNumber(CharSequence source, int start, int end) {
        if (source == null || start < 0 || start > source.length()) {
            return -1;
        }
        int ori = start;
        for (; start < end && start < source.length(); start++) {
            if (!isDigital(source.charAt(start))) {
                //bounds
                break;
            }
        }
        return ori == start ? -1 : start;
    }

    /**
     * 查找双引号字符串
     *
     * @param source
     * @param start
     * @param end
     * @return
     */
    public static int findString(CharSequence source, int start, int end) {
        if (source == null || start < 0 || start > source.length()) {
            return -1;
        }

        for (; start < end && start < source.length(); start++) {
            if ('\\' == source.charAt(start) && start + 1 < end && '"' == source.charAt(start + 1)) {

            } else if ('"' == source.charAt(start)) {
                return start;
            }
        }
        return -1;
    }

    /**
     * 查找单引号字符串
     *
     * @param source
     * @param start
     * @param end
     * @return
     */
    public static int findStringEx(CharSequence source, int start, int end) {
        if (source == null || start < 0 || start > source.length()) {
            return -1;
        }

        for (; start < end && start < source.length(); start++) {
            if ('\\' == source.charAt(start) && start + 1 < end && '\'' == source.charAt(start + 1)) {

            } else if ('\'' == source.charAt(start)) {
                return start;
            }
        }
        return -1;
    }

    /**
     * 查找一个片段。
     *
     * @param source
     * @param left
     * @param right
     * @param start
     * @param end
     * @param ignores 主要用途是忽略内部字符串。
     * @return 成功返回结束的索引对，否则返回 null.
     */
    public static int[] findSegment(CharSequence source, CharSequence left, CharSequence right, int start, int end) {

        if (source == null || left == null || right == null || start < 0 || end < 0 || end - start > source.length() || left.length() == 0 || right.length() == 0) {
            return null;
        }
        boolean like = left.equals(right);
        int index = -1;
        int matches = 0;
        for (; start < end && start < source.length(); start++) {
            if (source.charAt(start) == '\\') {//转义
                start++;
            } else if (!like && source.charAt(start) == left.charAt(0)) {//确定开始
                boolean tmp = true;
                for (int i = 0; i < left.length(); i++) {
                    if (i + start > end || source.charAt(start + i) != left.charAt(i)) {
                        tmp = false;
                        break;
                    }
                }
                if (tmp) {
                    if (index == -1) {
                        index = start + left.length();
                    }
                    start += left.length();
                    matches++;
                }
            } else if (source.charAt(start) == right.charAt(0)) {//确定结束
                boolean tmp = true;
                for (int i = 0; i < right.length(); i++) {
                    if (i + start > end || source.charAt(start + i) != right.charAt(i)) {
                        tmp = false;
                        break;
                    }
                }
                if (tmp) {
                    if (like && index == -1) {
                        index = start + left.length();
                    }
                    matches--;
                    if (index > 0 && matches == 0) {
                        return new int[]{index, start};
                    }
                    start += right.length();
                }
            }
        }

        return null;
    }

    /**
     * 查找子字符序列的首次出现位置。
     *
     * @param source
     * @param sub
     * @param start
     * @param end
     * @param ignores
     * @return 成功返回结束的索引，否则返回 -1.
     */
    public static int indexOf(CharSequence source, CharSequence sub, int start, int end, char... ignores) {
        if (source == null || sub == null || start < 0 || end < 0 || end - start > source.length() || sub.length() == 0) {
            return -1;
        }
        char c;
        boolean tmp;
        for (; start < end && start < source.length(); start++) {
            c = source.charAt(start);

            if (ignores != null) {
                tmp = false;
                for (int k = 0; k < ignores.length; k++) {//避免字符复制
                    if (ignores[k] == c) {
                        tmp = true;
                        break;
                    }
                }
                if (tmp) {
                    continue;
                }
            }
            if ('\\' == c) {
                //nothing
            } else if (sub.charAt(0) == c) {
                tmp = true;
                for (int k = 1; k < sub.length(); k++) {
                    if (k + start > end || source.charAt(start + k) != sub.charAt(k)) {
                        tmp = false;
                        break;
                    }
                }
                if (tmp) {
                    return start;
                }
            }
        }
        return -1;
    }

    public boolean inCharArray(char c, char... array) {
        if (array == null) {
            return false;
        }
        for (int k = 0; k < array.length; k++) {
            if (array[k] == c) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断start是否是索引的结束。
     *
     * @param c
     * @return
     */
    public static boolean isEnd(CharSequence source, int start, int end) {
        if (start >= end || start >= source.length()) {
            return true;
        }
        return false;
    }

    public static int findKeyword(CharSequence source, CharSequence key, int index, int end) {
        if (key == null || key.length() == 0 || source == null || source.length() == 0) {
            return -1;
        }
        boolean tmp = false;
        for (; index < end && index < source.length(); index++) { //移除空白字符
            if (!isWhitespace(source.charAt(index))) {
                break;
            }
        }

        for (; index < end && index < source.length(); index++) {
            if (source.charAt(index) == key.charAt(0)) {
                tmp = true;
                for (int i = 0; i < key.length(); i++) {
                    if (index + i >= end || source.charAt(index + i) != key.charAt(i)) {
                        tmp = false;
                        break;
                    }
                }
                if (tmp) {
                    break;
                } else {
                    index++;
                }
            } else {
                return -1;
            }
        }
        if (tmp) {
            return index + key.length();
        }
        return -1;
    }

}
