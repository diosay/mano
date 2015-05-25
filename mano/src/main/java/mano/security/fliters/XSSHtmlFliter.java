/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.security.fliters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mano.security.StringFliter;

/**
 * 基于白名单的 XSS 攻击过滤类。
 *
 * @author jun
 */
public class XSSHtmlFliter implements StringFliter {

    /**
     * 允许的标记。
     */
    public final List<String> allowTags = new ArrayList<>();

    /**
     * 允许的属性。
     */
    public final List<String> allowAttributes = new ArrayList<>();

    protected Pattern tagSearchPattern;
    protected Pattern attributeSearchPattern;
    protected Pattern blankPlacementPattern;
    public XSSHtmlFliter(){
        for(String s:new String[]{"a", "img", "br", "strong", "b", "code", "pre", "p", "div", "em", "span", "h1", "h2", "h3", "h4", "h5", "h6", "table", "ul", "ol", "tr", "th", "td", "hr", "li", "u"}){
            allowTags.add(s);
        }
        for(String s:new String[]{"title", "src", "href", "id", "class", "style", "width", "height", "alt", "target", "align"}){
            allowAttributes.add(s);
        }
        
        attributeSearchPattern=Pattern.compile("<[^><]+\\s([\\w\\-]+\\s*=)[^\\s]*", Pattern.CASE_INSENSITIVE);
        tagSearchPattern=Pattern.compile("<\\s*([a-zA-Z]+)[^>]*>", Pattern.CASE_INSENSITIVE);
        blankPlacementPattern=Pattern.compile("(<[^><]+)(window\\.|javascript:|js:|vbs:|about:|file:|document\\.|cookie|:\\s*[\\\\]?e[\\\\]?x[\\\\]?p[\\\\]?r[\\\\]?e[\\\\]?s[\\\\]?s[\\\\]?i[\\\\]?o[\\\\]?n|data\\s*:\\s*text\\s*/\\s*html\\s*;)([^><]*)", Pattern.CASE_INSENSITIVE);
    }
    
    @Override
    public StringBuilder encode(CharSequence content) {
        StringBuilder sb;
        if (content == null) {
            return new StringBuilder();
        } else if (content instanceof StringBuilder) {
            sb = (StringBuilder) content;
        } else {
            sb = new StringBuilder(content);
        }

        doEncode(sb);

        return sb;
    }
    private Pattern[] _ceps;

    protected Pattern[] getCharacterEntityPatterns() {
        if (_ceps == null) {
            ArrayList<Pattern> list = new ArrayList<>();
            for (int i = 'a'; i <= 'z'; i++) {
                list.add(Pattern.compile("&#(" + i + ");?", Pattern.CASE_INSENSITIVE));
            }
            for (int i = 'A'; i <= 'Z'; i++) {
                list.add(Pattern.compile("&#(" + i + ");?", Pattern.CASE_INSENSITIVE));
            }
            for (int i = '0'; i <= '9'; i++) {
                list.add(Pattern.compile("&#(" + i + ");?", Pattern.CASE_INSENSITIVE));
            }
            char[] carr = new char[]{'!', '@', '#', '$', '%', '^', '&', '*', '~', '`', '"', ';', ':', '?', '+', '/', '=', '(', ')', '<', '>', '{', '}', '[', ']', '-', '_', '|', '\\', '\''};
            for (int i : carr) {
                list.add(Pattern.compile("&#(" + i + ");?", Pattern.CASE_INSENSITIVE));
            }
            _ceps = list.toArray(new Pattern[0]);
        }
        return _ceps;
    }

    protected void doEncode(StringBuilder sb) {
        Matcher m;
        boolean found;
        
        //字符转义
        do {
            found = false;
            for (Pattern pattern : getCharacterEntityPatterns()) {
                m = pattern.matcher(sb);
                while (m.find()) {
                    found = true;
                    sb.replace(m.start(), m.end(), ((char) Integer.parseInt(m.group(1))) + "");//((char) i) + ""
                    m = pattern.matcher(sb);
                }
            }
        } while (found);

        //移除非白名单标签
        m = tagSearchPattern.matcher(sb);
        String tag;
        while (m.find()) {
            //System.out.println("m0:"+m.group(0));
            //System.out.println("m1:"+m.group(1));
            tag = m.group(1).trim().toLowerCase();
            if (!allowTags.contains(tag)) {
                sb.replace(m.start(), m.end(), "");
                //TODO:处理过滤后的标记结束部分，如：</XXX>
                m = tagSearchPattern.matcher(sb);
            }
        }

        //移除非白名单属性
        m = attributeSearchPattern.matcher(sb);
        while (m.find()) {
            //System.out.println("m0:" + m.group(0));
            //System.out.println("m1:" + m.group(1));
            //System.out.println("m2:"+m.group(2));
            //System.out.println("m3:"+m.group(2));
            tag = m.group(1).replace("=", "").replace(" ", "").trim().toLowerCase();
            //System.out.println("tag:"+tag+">>");
            if (!"-".equalsIgnoreCase(tag) && !allowAttributes.contains(tag)) {
                //System.out.println("repl:"+tag+">>"+allowAttributes.contains("href"));
                sb.replace(m.start(), m.end(), m.group(0).replace(m.group(1), "-="));
                //TODO:处理过滤后的标记结束部分，如：</XXX>
                m = attributeSearchPattern.matcher(sb);
            }
        }

        //移除关键词黑名单
        m = blankPlacementPattern.matcher(sb);
        while (m.find()) {
            //System.out.println("m0:"+m.group(0));
            sb.replace(m.start(), m.end(), m.group(0).replace(m.group(2), ""));
            m = blankPlacementPattern.matcher(sb);
        }

    }

    public static void main(String[] args) {
        XSSHtmlFliter f = new XSSHtmlFliter();
        //f.allowTags.add("a");
        String s = "javasc&#&#49;14;ipt:alert(document.cookie)";
        s = "<a href=javasc&#114;ipt:alert(document.cookie)>click me</a>";
        s="<img style=xss:ex\\pres\\sion(alert(1))>";
        s="<a href=\"d&#97;ta:text/html;base64,PHNjcmlwdD5hbGVydChkb2N1bWVudC5jb29raWUpPC9zY3JpcHQ+\">click me</a>";
        StringBuilder sb = new StringBuilder(s);
        System.out.println(""+sb);
//        Pattern p=Pattern.compile(":\\s*[\\\\]?e[\\\\]?x[\\\\]?p[\\\\]?r[\\\\]?e[\\\\]?s[\\\\]?s[\\\\]?i[\\\\]?o[\\\\]?n", Pattern.CASE_INSENSITIVE);
//        Matcher m=p.matcher(sb);
//        if(m.find()){
//            System.out.println(""+m.group(0));
//        }
        System.out.println("result:" + f.encode(sb));
    }

}
