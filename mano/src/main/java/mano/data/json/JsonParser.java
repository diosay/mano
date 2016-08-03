/*
 * Copyright (C) 2014 The MANO Project. All rights reserved.
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.data.json;

/**
 *
 * @author junhwong
 */
public class JsonParser {

    protected int findPairString(CharSequence source, char open, char close, int index, int length) {
        length += index;
        int find = 1;
        for (; index < source.length() && index < length; index++) {
            if (source.charAt(index) == open) {
                find++;
            } else if (source.charAt(index) == close) {
                find--;
            }
            if (find == 0) {
                return index;
            }
        }
        return -1;
    }
    
    protected void findKey(CharSequence source){
        int index=0;
        
        //去掉空白字符
        for(;index<source.length();index++){
            if(source.charAt(index)==' ' || source.charAt(index)=='\r'|| source.charAt(index)=='\n'|| source.charAt(index)=='\t' || source.charAt(index)=='\f' || source.charAt(index)=='\b'){
                break;
            }
        }
        
        for(;index<source.length();index++){
            if(source.charAt(index)=='\'' || source.charAt(index)=='"'){
                //getString
                break;
            }
            
        }
        
    }

    public void parse(CharSequence json) {
        json = "{abc:val}";
        int found;
        CharSequence sub;
        for (int i = 0; i < json.length(); i++) {
            if (i == '{') {
                found=findPairString(json, '{', ']', i + 1, json.length());
                if(found<0){
                    throw new JsonException("JSON格式错误，字符 [ 不匹配。");
                }
                sub=json.subSequence(i+1, found);
                i=found;
                
            }
        }

    }

}
